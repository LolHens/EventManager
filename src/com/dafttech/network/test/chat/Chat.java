package com.dafttech.network.test.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.dafttech.filterlist.Blacklist;
import com.dafttech.network.Client;
import com.dafttech.network.NetworkInterface;
import com.dafttech.network.Server;
import com.dafttech.network.disconnect.Disconnect;
import com.dafttech.network.packet.SimplePacket;
import com.dafttech.network.protocol.SimpleProtocol;

public class Chat {
    public static NetworkInterface<SimplePacket> net = null;

    /**
     * @param args
     * @throws IOException
     * @throws NumberFormatException
     */
    public static void main(String[] args) throws NumberFormatException, IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Server? ");
        boolean isServer = Boolean.valueOf(input.readLine());
        System.out.println(isServer);
        System.out.println("Address/Port: ");
        String address = input.readLine();
        if (isServer) {
            net = new Server<SimplePacket>(SimpleProtocol.class, address) {
                @Override
                public void receive(Client<SimplePacket> client, SimplePacket packet) {
                    send(new Blacklist<Client<?>>(client), packet);
                    System.out.println(client.getSocket().getRemoteSocketAddress().toString() + ": " + packet.channel + ": "
                            + packet.toString());
                }

                @Override
                public void disconnect(Client<SimplePacket> client, Disconnect reason) {
                    System.out.println(client.getSocket().getRemoteSocketAddress().toString() + ": Disconnect "
                            + reason.toString());
                }
            };
            System.out.println(net.getServerSocket().getLocalSocketAddress().toString());
            while (true)
                net.send(new SimplePacket(10, input.readLine().getBytes()));
        } else {
            net = new Client<SimplePacket>(SimpleProtocol.class, address) {
                @Override
                public void receive(SimplePacket packet) {
                    System.out.println(net.getSocket().getRemoteSocketAddress().toString() + ": " + packet.channel + ": "
                            + packet.toString());
                }

                @Override
                public void disconnect(Disconnect reason) {
                    System.out.println(getSocket().getRemoteSocketAddress().toString() + ": Disconnect " + reason.toString());
                }
            };
            System.out.println(net.getSocket().getLocalSocketAddress().toString());
            net.send(new SimplePacket(1, "Connected!".getBytes()));
            while (true)
                net.send(new SimplePacket(10, input.readLine().getBytes()));
        }
    }
}
