/**
 * Copyright (C) 2012-2015 Dell, Inc.
 * See annotations for authorship information
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.cloud.digitalocean.models.rest;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.digitalocean.models.IDigitalOcean;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class DigitalOceanAction implements IDigitalOcean {
	protected RESTMethod method;
	protected ActionType actionType;
	
	public DigitalOceanRestModel fromJson(String jsonString) throws CloudException {
		//THIS IS NOT USED SINCE ALL ACTIONS RETURN AN EVENT OBJECT...
		return null;
	}

	public JSONObject getParameters() throws CloudException, InternalException {
		return null;
	}
	
	public RESTMethod getRestMethod() {
		return method;		
	}
	
	public String getActionTypeUrl() {
		return this.actionType.toString();
	}
	
	@Override
	public  String toString() {			
		return getActionTypeUrl();
	}
	
}

