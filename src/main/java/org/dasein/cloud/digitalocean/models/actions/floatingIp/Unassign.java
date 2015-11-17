package org.dasein.cloud.digitalocean.models.actions.floatingIp;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.digitalocean.models.rest.DigitalOceanPostAction;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mariapavlova on 16/11/2015.
 */
public class Unassign extends DigitalOceanPostAction {

    public Unassign() {
    }

    @Override
    public  String toString() {
        return "/v2/floating_ips/%s/actions";
    }

    public JSONObject getParameters() throws CloudException, JSONException, InternalException {
        JSONObject postData = new JSONObject();
        postData.put("type", "unassign");
        return postData;
    }
}
