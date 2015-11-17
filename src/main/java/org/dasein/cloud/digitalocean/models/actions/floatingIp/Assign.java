package org.dasein.cloud.digitalocean.models.actions.floatingIp;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.digitalocean.models.rest.DigitalOceanPostAction;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mariapavlova on 16/11/2015.
 */
public class Assign extends DigitalOceanPostAction {

    //Required
    private String dropletId = null;

    public Assign(String dropletId) {
        this.dropletId = dropletId;
    }

    @Override
    public String toString() {
        return "/v2/floating_ips/%s/actions";
    }

    public JSONObject getParameters() throws CloudException, JSONException, InternalException {
        JSONObject postData = new JSONObject();

        if ( dropletId == null) {
            throw new InternalException("Missing required parameter 'dropletId'");
        }
        postData.put("type", "assign");
        postData.put("droplet_id", dropletId);

        return postData;
    }
}
