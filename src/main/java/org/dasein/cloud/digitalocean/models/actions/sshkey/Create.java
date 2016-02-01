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


/**
 * Copyright (C) 2014 ACenterA, Inc.
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
package org.dasein.cloud.digitalocean.models.actions.sshkey;

import org.dasein.cloud.InternalException;
import org.dasein.cloud.digitalocean.models.rest.DigitalOceanPostAction;
import org.json.JSONException;
import org.json.JSONObject;


public class Create extends DigitalOceanPostAction {	

	//Required
	private String name = "";
	private String public_key= null;
	
	
	public Create( String name, String public_key) {
		this.name = name;
		this.public_key = public_key;		
	}	
	
	public String getName() {
		return this.name;
	}

	public void setName(String n ) {
		this.name = n ;
	}

	@Override
	public  String toString() {			
		return "v2/account/keys";
	}
	
	public JSONObject getParameters() throws InternalException {
		JSONObject postData = new JSONObject();

		if (this.name == null) {
			throw new InternalException("Missing required parameter 'name'");
		}
		try {
			postData.put("name",  this.name);
		}
		catch( JSONException ignore ) {
		}

		if (this.public_key == null) {
			throw new InternalException("Missing required parameter 'public_key'");
		}

		try {
			postData.put("public_key",  this.public_key);
		}
		catch( JSONException ignore ) {
		}

		return postData;
	}
	
}

