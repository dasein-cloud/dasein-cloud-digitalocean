package org.dasein.cloud.digitalocean.network;

import org.dasein.cloud.digitalocean.DigitalOcean;
import org.dasein.cloud.network.AbstractNetworkServices;
import org.dasein.cloud.network.IpAddressSupport;
import org.dasein.cloud.network.NetworkServices;

import javax.annotation.Nullable;

/**
 * Created by mariapavlova on 17/11/2015.
 */
public class DONetworkServices extends AbstractNetworkServices<DigitalOcean> {
    public DONetworkServices(DigitalOcean digitalOcean) {
        super(digitalOcean);
    }

    @Nullable @Override public IpAddressSupport getIpAddressSupport() {
        return new DOFloatingIP(getProvider());
    }
}
