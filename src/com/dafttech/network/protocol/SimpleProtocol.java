package com.dafttech.network.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dafttech.network.packet.SimplePacket;
import com.dafttech.primitives.Primitive;

public class SimpleProtocol extends Protocol<SimplePacket> {
    @Override
    public final SimplePacket receive_() throws IOException {
        byte[] integer = new byte[4], data;
        int channel, size;
        read(integer);
        channel = Primitive.INT.fromByteArray(integer);
        read(integer);
        size = Primitive.INT.fromByteArray(integer);
        data = new byte[size];
        read(data);
        return new SimplePacket(channel, data);
    }

    @Override
    public final void send_(SimplePacket packet) throws IOException {
        ByteBuffer packetBuffer = ByteBuffer.allocate(8 + packet.data.length);
        packetBuffer.put(Primitive.INT.toByteArray(packet.channel));
        packetBuffer.put(Primitive.INT.toByteArray(packet.data.length));
        packetBuffer.put(packet.data);
        write(packetBuffer.array());
    }
}
