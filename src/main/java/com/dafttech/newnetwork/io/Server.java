package com.dafttech.newnetwork.io;

import com.dafttech.autoselector.AutoSelector;
import com.dafttech.newnetwork.AbstractServer;
import com.dafttech.newnetwork.packet.Packet;
import com.dafttech.newnetwork.protocol.AbstractProtocol;
import com.dafttech.newnetwork.protocol.ProtocolProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.function.BiConsumer;

public class Server<P extends Packet> extends AbstractServer<P> {
    private final ServerSocketChannel socketChannel;

    public Server(Class<? extends AbstractProtocol> protocolClazz, InetSocketAddress socketAddress, BiConsumer<ProtocolProvider<P>, P> receive) throws IOException {
        super(protocolClazz, receive);

        socketChannel = ServerSocketChannel.open();
        socketChannel.socket().bind(socketAddress);
        socketChannel.configureBlocking(false);

        AutoSelector.instance.register(socketChannel, SelectionKey.OP_ACCEPT, this::accept);
    }

    public Server(Class<? extends AbstractProtocol> protocolClazz, int port, BiConsumer<ProtocolProvider<P>, P> receive) throws IOException {
        this(protocolClazz, new InetSocketAddress(port), receive);
    }

    private void accept(SelectionKey selectionKey) {
        try {
            clients.add(new Client<P>(protocolClazz, socketChannel.accept(), receive));
        } catch (IOException e) {
            ioException(e);
        }
    }

    @Override
    public void close() {
        super.close();

        try {
            socketChannel.close();
        } catch (IOException e) {
            ioException(e);
        }
    }
}
