package org.lolhens.network;

/**
 * Created by LolHens on 28.12.2014.
 */
public interface IFactoryClient<P> {
    public AbstractClient<P> newClient(Class<? extends AbstractProtocol<P>> protocol);
}
