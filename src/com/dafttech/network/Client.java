package com.dafttech.network;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.dafttech.network.disconnect.Disconnect;
import com.dafttech.network.disconnect.EOF;
import com.dafttech.network.disconnect.Quit;
import com.dafttech.network.disconnect.Reset;
import com.dafttech.network.disconnect.Timeout;
import com.dafttech.network.disconnect.Unknown;
import com.dafttech.network.packet.IPacket;
import com.dafttech.network.protocol.Protocol;

public class Client<Packet extends IPacket> extends NetworkInterface<Packet> {
    private volatile Socket socket;
    private ClientThread thread;

    protected Client(Class<Protocol<Packet>> protocolClass, Socket socket) {
        super(protocolClass);
        this.socket = socket;
        thread = new ClientThread();
        thread.start();
    }

    public Client(Class<Protocol<Packet>> protocolClass, InetAddress address, int port) throws IOException {
        this(protocolClass, new Socket(address, port));
    }

    public Client(Class<Protocol<Packet>> protocolClass, String host, int port) throws UnknownHostException, IOException {
        this(protocolClass, InetAddress.getByName(host), port);
    }

    public Client(Class<Protocol<Packet>> protocolClass, String host, String port) throws UnknownHostException, IOException {
        this(protocolClass, host, Integer.valueOf(port));
    }

    public Client(Class<Protocol<Packet>> protocolClass, String host) throws UnknownHostException, IOException {
        this(protocolClass, host.split(":")[0], host.split(":")[1]);
    }

    public final Socket getSocket() {
        return socket;
    }

    public final boolean isAlive() {
        return thread.reason == null;
    }

    public final void close() {
        close(new Quit());
    }

    public final void close(Disconnect reason) {
        thread.reason = reason;
    }

    private class ClientThread extends Thread {
        private volatile Disconnect reason = null;

        @Override
        public void run() {
            getProtocol().connect();

            while (Client.this.isAlive()) {
                try {
                    receive(getProtocol().receive());
                } catch (IOException e) {
                    if (e instanceof EOFException) {
                        close(new EOF());
                    } else if (e instanceof SocketException && e.getMessage().equals("Connection reset")) {
                        close(new Reset());
                    } else if (e instanceof SocketTimeoutException) {
                        close(new Timeout());
                    } else {
                        close(new Unknown());
                        e.printStackTrace();
                    }
                }
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            getProtocol().disconnect(reason);
        }
    }

    @Override
    public final int available() throws IOException {
        if (isAlive()) return socket.getInputStream().available();
        return 0;
    }

    @Override
    public final int read() throws IOException {
        if (isAlive()) {
            int result = socket.getInputStream().read();
            if (result == -1) throw new EOFException();
            return result;
        }
        return 0;
    }

    @Override
    public final byte[] read(byte[] array) throws IOException {
        if (isAlive()) {
            int result = socket.getInputStream().read(array);
            if (result == -1) throw new EOFException();
        }
        return array;
    }

    @Override
    public final void write(byte... data) throws IOException {
        if (isAlive()) {
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
        }
    }

    public void connect() {
    }

    public void disconnect(Disconnect reason) {
    }

    @Override
    public void receive(Packet packet) {
    }

    @Override
    public final void send(Packet packet) throws IOException {
        getProtocol().send(packet);
    }
}
