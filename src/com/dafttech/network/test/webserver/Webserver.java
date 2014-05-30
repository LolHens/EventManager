package com.dafttech.network.test.webserver;

import java.io.IOException;

import com.dafttech.network.Client;
import com.dafttech.network.Server;
import com.dafttech.network.disconnect.Disconnect;
import com.dafttech.network.packet.RawPacket;
import com.dafttech.network.protocol.RawProtocol;

public class Webserver {
    public static String inHeader = "", outHeader = "", content = "";

    public static void main(String[] args) {
        try {
            new Server<RawPacket>(RawProtocol.class, 80) {
                @Override
                public void receive(Client<RawPacket> client, RawPacket packet) {
                    inHeader = inHeader + new String(packet.data);
                    if (inHeader.endsWith("\r\n\r\n")) {
                        content = "{\"name\":\"nothingspecial\","
                                + "\"displayName\":\" Minecraft: Nothing Special\","
                                + "\"user\":\"LolHens\","
                                + "\"version\":\"1.0\","
                                + "\"url\":\"https://dl.dropboxusercontent.com/u/148704233/Minecraft%20-%20The%20Next%20Generation.zip\","
                                + "\"minecraft\":\"1.6.4\","
                                + "\"logo\":{\"url\":\"http://cdn.technicpack.net/platform/pack-logos/331789.png?1397635247\","
                                + "\"md5\":\"5023e4fcf89d695a820b422a12331f5a\"},"
                                + "\"background\":{\"url\":\"http://cdn.technicpack.net/platform/pack-backgrounds/331789.png?1397635247\","
                                + "\"md5\":\"dc1a3cc155715b4730bd14f8f2e1ecc5\"}," + "\"solder\":\"\"," + "\"forceDir\":false}";
                        content = content.replace("/", "\\/");
                        outHeader = "HTTP/1.0 200 OK\r\nServer: LolHens/1.0 (Windows)\r\nContent-Length: "
                                + content.length()
                                + "\r\nContent-Language: en\r\nContent-Type: text/plain; charset=utf-8\r\nConnection: close\r\n\r\n"
                                + content;
                        try {
                            client.send(new RawPacket(outHeader.getBytes()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // System.out.println(inHeader);
                        // System.out.println(outHeader);
                        inHeader = "";
                    }
                }

                @Override
                public void connect(Client<RawPacket> client) {
                    System.out.println("connect: " + client.getSocket().getRemoteSocketAddress().toString());
                }

                @Override
                public void disconnect(Client<RawPacket> client, Disconnect reason) {
                    System.out.println("disconnect: " + client.getSocket().getRemoteSocketAddress().toString());
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
