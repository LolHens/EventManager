package com.dafttech.newnetwork;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractServer<P> extends ProtocolProvider<P> {
    protected final List<AbstractClient<P>> clients = new LinkedList<>();

    public AbstractServer(Class<? extends AbstractProtocol> protocolClazz, BiConsumer<ProtocolProvider<P>, P> receive) {
        super(protocolClazz, receive);
    }

    public final void setProtocol(Class<? extends AbstractProtocol> protocolClazz) {
        super.setProtocol(protocolClazz);
    }

    public void broadcast(P packet) {
        for (AbstractClient<P> client : clients) client.send(packet);
    }

    protected void onAccept(AbstractClient<P> client) {
    }

    @Override
    public void close() throws IOException {
        super.close();
        for (AbstractClient<P> client : clients) client.close();
    }
}
