package org.lolhens.autoselector;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

/**
 * Created by LolHens on 10.11.2014.
 */
public class SelectorManager {
    public static final SelectorManager instance = new SelectorManager();

    private final AutoSelector[] autoSelectors;
    private final ExecutorService executorService;
    private int next = 0;

    public SelectorManager() {
        autoSelectors = new AutoSelector[Runtime.getRuntime().availableProcessors()];
        executorService = Executors.newFixedThreadPool(autoSelectors.length, (runnable) -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    public SelectionKey register(SelectableChannel channel, int ops, Consumer<SelectionKey> consumer) throws IOException {
        if (channel.isRegistered()) {
            SelectionKey selectionKey = null;
            for (AutoSelector autoSelector : autoSelectors) {
                if (autoSelector == null) continue;
                selectionKey = channel.keyFor(autoSelector.selector);
                if (selectionKey != null) break;
            }
            if (selectionKey != null) {
                selectionKey.interestOps(ops);
                selectionKey.attach(consumer);
                selectionKey.selector().wakeup();
                return selectionKey;
            }
        }

        int current = next++;
        if (next >= autoSelectors.length) next = 0;

        if (autoSelectors[current] == null) autoSelectors[current] = new AutoSelector(executorService);
        AutoSelector selectorContainer = autoSelectors[current];

        SelectionKey selectionKey = selectorContainer.register(channel, ops, consumer);
        selectorContainer.selector.wakeup();
        return selectionKey;
    }
}
