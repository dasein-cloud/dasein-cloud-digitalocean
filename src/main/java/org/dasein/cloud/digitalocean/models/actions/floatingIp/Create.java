package org.dasein.cloud.digitalocean.models.actions.floatingIp;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.digitalocean.models.rest.DigitalOceanPostAction;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mariapavlova on 16/11/2015.
 */
public class Create extends DigitalOceanPostAction {

    //Required
    String region = null;

    public Create( String region) {
        this.region = region;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String n ) {
        this.region = n ;
    }

    @Override
    public  String toString() {
        return "/v2/floating_ips";
    }

    public JSONObject getParameters() throws CloudException, JSONException {
        JSONObject postData = new JSONObject();

        if (this.region == null) {
            throw new CloudException("Missing required parameter 'region'");
        }
        postData.put("region",  this.region);

        return postData;
    }
}


