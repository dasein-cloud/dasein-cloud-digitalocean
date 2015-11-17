package org.dasein.cloud.digitalocean.models;

import org.dasein.cloud.digitalocean.models.rest.DigitalOceanRestModel;

/**
 * Created by mariapavlova on 13/11/2015.
 */
public class FloatingIp implements DigitalOceanRestModel {
    private String ip;
    private String droplet;
    private boolean locked;
    private Region region;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDroplet() {
        return droplet;
    }

    public void setDroplet(String droplet) {
        this.droplet = droplet;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}
