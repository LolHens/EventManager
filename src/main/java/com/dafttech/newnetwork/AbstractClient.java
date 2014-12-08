package com.dafttech.newnetwork;

import com.dafttech.newnetwork.disconnect.DisconnectReason;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.BiConsumer;

public abstract class AbstractClient<P> extends ProtocolProvider<P> {
    private AbstractProtocol<P> protocol;

    public AbstractClient(Class<? extends AbstractProtocol> protocolClazz, BiConsumer<AbstractClient<P>, P> receiveHandler, BiConsumer<ProtocolProvider<P>, DisconnectReason> disconnectHandler) {
        super(protocolClazz, receiveHandler, disconnectHandler);
    }

    public abstract void connect(SocketAddress socketAddress);

    @Override
    public final void setProtocol(Class<? extends AbstractProtocol> protocolClazz) {
        super.setProtocol(protocolClazz);
        try {
            AbstractProtocol<P> newProtocol = getProtocol().newInstance();
            newProtocol.client = this;
            if (protocol != null) protocol.client = null;
            protocol = newProtocol;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Protocol instantiation failed!", e);
        }
    }

    protected final void onConnect() {
        onConnect(this);
    }

    protected final void receive(P packet) {
        onReceive(this, packet);
    }

    public final void send(P packet) {
        protocol.send(packet);
    }

    protected final void read(ReadableByteChannel in) {
        try {
            protocol.read(in);
        } catch (IOException e) {
            onException(e);
        }
    }

    protected final void write(WritableByteChannel out) {
        try {
            protocol.write(out);
        } catch (IOException e) {
            onException(e);
        }
    }

    protected abstract void setWriteEnabled(boolean value);

    @Override
    protected void onClose() throws IOException {
        super.onClose();
        protocol.onClose();
    }
}
