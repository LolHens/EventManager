package com.dafttech.newnetwork;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractServer<P> extends ProtocolProvider<P> {
    protected final Collection<AbstractClient<P>> clients = Collections.<AbstractClient<P>>synchronizedCollection(new LinkedList<>());

    private Consumer<AbstractClient<P>> acceptHandler = null;

    public AbstractServer(Class<? extends AbstractProtocol> protocolClazz, BiConsumer<AbstractClient<P>, P> receiveHandler) {
        super(protocolClazz, receiveHandler);
    }

    @Override
    public final void setProtocol(Class<? extends AbstractProtocol> protocolClazz) {
        super.setProtocol(protocolClazz);
    }

    public final void setAcceptHandler(Consumer<AbstractClient<P>> acceptHandler) {
        this.acceptHandler = acceptHandler;
    }

    protected final Consumer<AbstractClient<P>> getAcceptHandler() {
        return acceptHandler;
    }

    protected final void onAccept(AbstractClient<P> client) {
        if (acceptHandler != null) acceptHandler.accept(client);
    }

    public void broadcast(P packet) {
        for (AbstractClient<P> client : clients) client.send(packet);
    }

    @Override
    public void close() throws IOException {
        super.close();
        for (AbstractClient<P> client : clients) client.close();
    }
}
