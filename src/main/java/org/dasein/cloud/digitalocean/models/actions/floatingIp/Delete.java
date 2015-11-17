package org.dasein.cloud.digitalocean.models.actions.floatingIp;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.digitalocean.models.rest.DigitalOceanDeleteAction;
import org.dasein.cloud.digitalocean.models.rest.DigitalOceanPostAction;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mariapavlova on 16/11/2015.
 */
public class Delete extends DigitalOceanDeleteAction {

    @Override
    public  String toString() {
        return "v2/floating_ips/%s";
    }
}
