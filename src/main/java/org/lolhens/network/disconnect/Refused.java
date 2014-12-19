package org.lolhens.network.disconnect;

import org.lolhens.network.ProtocolProvider;

import java.io.IOException;

/**
 * Created by LolHens on 27.11.2014.
 */
public class Refused extends DisconnectReason {
    public Refused(ProtocolProvider<?> protocolProvider, IOException exception) {
        super(protocolProvider, exception, false, "Connection Refused");
    }
}