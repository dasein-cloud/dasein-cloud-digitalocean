package org.dasein.cloud.digitalocean.models;

import org.dasein.cloud.digitalocean.models.rest.PaginatedModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariapavlova on 13/11/2015.
 */
public class FloatingIps extends PaginatedModel {

    private List<FloatingIp> floatingIps;

    public void addFloatingIp(FloatingIp ip) {
        getFloatingIps().add(ip);
    }

    public List<FloatingIp> getFloatingIps() {
        if( floatingIps == null ) {
            floatingIps = new ArrayList<FloatingIp>();
        }
        return floatingIps;
    }

}
