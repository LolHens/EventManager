package org.lolhens.network.packet;

/**
 * Created by LolHens on 11.11.2014.
 */
public class SimplePacket {
    public final int channel;
    public final byte[] data;

    public SimplePacket(int channel, byte[] data) {
        this.channel = channel;
        this.data = data;
    }

    public final int size() {
        if (data == null) return 0;
        return data.length;
    }

    @Override
    public String toString() {
        return new String(data);
    }
}
