package org.dasein.cloud.digitalocean.models.actions.floatingIp;

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
        return "v2/floating_ips/%s/actions";
    }

    public JSONObject getParameters() throws InternalException {
        JSONObject postData = new JSONObject();

        if ( dropletId == null) {
            throw new InternalException("Missing required parameter 'dropletId'");
        }
        try { postData.put("type", "assign"); } catch( JSONException ignore ) {}
        try { postData.put("droplet_id", dropletId); } catch( JSONException ignore ) {}

        return postData;
    }
}
