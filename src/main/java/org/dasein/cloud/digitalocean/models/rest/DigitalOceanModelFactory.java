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

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dasein.cloud.*;
import org.dasein.cloud.digitalocean.models.Action;
import org.dasein.cloud.digitalocean.models.Actions;
import org.dasein.cloud.digitalocean.models.Droplet;
import org.dasein.cloud.digitalocean.models.IDigitalOcean;
import org.dasein.cloud.digitalocean.models.actions.droplet.Create;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DigitalOceanModelFactory {

    static private final Logger wire = org.dasein.cloud.digitalocean.DigitalOcean.getWireLogger(DigitalOceanModelFactory.class);
	static private final Logger logger = org.dasein.cloud.digitalocean.DigitalOcean.getLogger(DigitalOceanModelFactory.class);

    //for get method
    private static DigitalOceanRestModel getModel(org.dasein.cloud.digitalocean.DigitalOcean provider, RESTMethod method, String token, DigitalOcean model, String endpoint) throws CloudException, InternalException {
        return model.fromJson(getModel(provider, method, token, endpoint, null));
    }
    
	private static String getModel(org.dasein.cloud.digitalocean.DigitalOcean provider, RESTMethod method, String token, String endpoint, DigitalOceanAction action) throws CloudException, InternalException {
		if( logger.isTraceEnabled() ) {
            logger.trace("ENTER - " + DigitalOceanModelFactory.class.getName() + ".performHttpRequest(" + method + "," + token + "," + endpoint + ")");
            logger.trace("CALLING - " + method + " "  + endpoint);
        }
        HttpResponse response;
        String responseBody = null;
        try {
            response = sendRequest(provider, method, token, endpoint, action);
            if( response.getEntity() != null ) {
                responseBody = IOUtils.toString(response.getEntity().getContent());
                if( wire.isDebugEnabled() ) {
                    wire.debug(responseBody);
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("RECEIVED - " + "[" + response.getStatusLine().getStatusCode() + "] " + responseBody);
            }

            if( response.getStatusLine().getStatusCode() >= 300 ) {
                JSONObject ob = new JSONObject(responseBody);
                String message = null;
                String code = null;
                if( ob != null ) {
                    code = ob.getString("id");
                    message = ob.getString("message");
                }
                logger.error("Status:" + response.getStatusLine().getStatusCode() + " - " + responseBody);
                throw new GeneralCloudException(CloudErrorType.GENERAL, response.getStatusLine().getStatusCode(), code, message);
            }
            return responseBody;

        } catch (JSONException e) {
            throw new CommunicationException("Unable to parse the response", e);
        } catch (IOException e) {
            throw new CommunicationException("IO error", e);
        } finally {
            if (logger.isTraceEnabled()) {
                logger.trace("EXIT - " + DigitalOceanModelFactory.class.getName() + ".performHttpRequest(" + method + "," + token + "," + endpoint + ")");
            }
            if (wire.isDebugEnabled()) {
                wire.debug("--------------------------------------------------------------------------------------");
                wire.debug("");
            }
        }
    }

    /**
     * Sent http request to the server
     * @return Http response
     * @throws CloudException
     */
    private static HttpResponse sendRequest(org.dasein.cloud.digitalocean.DigitalOcean provider, RESTMethod method, String token, String strUrl, DigitalOceanAction action) throws CloudException, InternalException {
        HttpRequestBase req = null;
        if (method == RESTMethod.GET) {
            req = new HttpGet(strUrl);
        } else if (method == RESTMethod.POST) {
            req = new HttpPost(strUrl);
        } else if (method == RESTMethod.PUT) {
            req = new HttpPut(strUrl);
        } else if (method == RESTMethod.DELETE) {
            req = new HttpDelete(strUrl);
        } else if (method == RESTMethod.HEAD) {
            req = new HttpHead(strUrl);
        }

        try {
            req.setHeader("Authorization", "Bearer " + token);
            req.setHeader("Accept", "application/json");
            req.setHeader("Content-Type", "application/json;charset=UTF-8");

            StringEntity requestEntity = null;
            if (req instanceof HttpEntityEnclosingRequestBase && action != null) {
                JSONObject jsonToPost = action.getParameters();
                if (jsonToPost != null) {
                    requestEntity = new StringEntity(
                            jsonToPost.toString(),
                            ContentType.APPLICATION_JSON);
                    ((HttpEntityEnclosingRequestBase) req).setEntity(requestEntity);
                }
            }

            HttpClient httpClient = provider.getClient();

            if (wire.isDebugEnabled()) {
                wire.debug("");
                wire.debug("--------------------------------------------------------------------------------------");
            }

            if (wire.isDebugEnabled()) {
                wire.debug(req.getRequestLine().toString());
                for (Header header : req.getAllHeaders()) {
                    wire.debug(header.getName() + ": " + header.getValue());
                }
                wire.debug("");

                if (requestEntity != null) {
                    try {
                        wire.debug(EntityUtils.toString(requestEntity));
                        wire.debug("");
                    } catch (IOException ignore) {
                    }
                }
            }

            HttpResponse response = null;
            int retryCount = 0;

            while (retryCount < 6) {
                response = httpClient.execute(req);

                if (wire.isDebugEnabled()) {
                    wire.debug(response.getStatusLine().toString());
                }

                if (method == RESTMethod.DELETE) {
                    if ((response.getStatusLine().getStatusCode() == 204)) {
                        break;
                    } else {
                        retryCount++;
                        Thread.sleep(5000);
                    }
                } else {
                    break;
                }
            }
            if (method == RESTMethod.DELETE && (response.getStatusLine().getStatusCode() != 204)) {
                //Error occurred
                throw new GeneralCloudException("Delete method returned unexpected code, despite retrying.", CloudErrorType.GENERAL);
            }
            return response;
        } catch (IOException e) {
            throw new GeneralCloudException("Problem sending request.", e, CloudErrorType.GENERAL);
        } catch (InterruptedException e) {
            throw new InternalException("Retry has been interrupted.", e);
        } finally {
            try {
//                req.releaseConnection();
            } catch (Exception e) {
            }

        }
    }

    public static DigitalOceanRestModel getModel(org.dasein.cloud.digitalocean.DigitalOcean provider, DigitalOcean model) throws CloudException, InternalException {
        return getModel(provider, model, 0);
    }

	public static DigitalOceanRestModel getModel(org.dasein.cloud.digitalocean.DigitalOcean provider, DigitalOcean model, int page) throws CloudException, InternalException {
		if( logger.isTraceEnabled() ) {
            logger.trace("ENTER - " + DigitalOceanModelFactory.class.getName() + ".getModel(" + provider + "," +  model + ")");
        }
			
		String token = (String) provider.getContext().getConfigurationValue("token");
    	
		try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(getApiUrl(provider)).append(getEndpoint(model));
            if( page > 0 ) {
                if( urlBuilder.indexOf("?") > 0 ) {
                    urlBuilder.append('&');
                }
                else {
                    urlBuilder.append('?');
                }
                urlBuilder.append("page=").append(page);
            }
            return getModel(provider, RESTMethod.GET, token, model, urlBuilder.toString());
        } finally {
			if( logger.isTraceEnabled() ) {
	            logger.trace("EXIT - " + DigitalOceanModelFactory.class.getName() + ".getModel(" + provider + "," + model + ")");
	        }
		}
	}

	public static DigitalOceanRestModel getModelById(org.dasein.cloud.digitalocean.DigitalOcean provider, DigitalOcean model, String id) throws CloudException, InternalException {
		if( logger.isTraceEnabled() ) {
            logger.trace("ENTER - " + DigitalOceanModelFactory.class.getName() + ".getModel(" + provider + "," +  model + "," + id + ")");
        }
		String token = (String) provider.getContext().getConfigurationValue("token");
		try {
			return getModel(provider, RESTMethod.GET, token, model, getApiUrl(provider) + getEndpoint(model, id));
        } finally {
			if( logger.isTraceEnabled() ) {
	            logger.trace("EXIT - " + DigitalOceanModelFactory.class.getName() + ".getModel(" + provider + "," + model + ")");
	        }
		}
	}

	private static String getEndpoint(IDigitalOcean d) {
		return d.toString();				
	}
	
	private static String getEndpoint(IDigitalOcean d, String id) {
		if (id == null) return getEndpoint(d);
		
		return String.format(d.toString(), id);				
	}

	private static String getApiUrl(org.dasein.cloud.digitalocean.DigitalOcean provider) {
		String url = provider.getContext().getCloud().getEndpoint();
		if (url == null) {
			//Return the default digitalocean endpoint.
			url = "https://api.digitalocean.com/";
		} else {
			if (url.endsWith("//")) {
				url = url.substring(0, url.length()-1);
			} else {
				if (!url.endsWith("/")) {
					url = url + "/";
				}
			}
		}
		return url;
	}

	public static Action performAction(org.dasein.cloud.digitalocean.DigitalOcean provider, DigitalOceanAction doa, String id) throws CloudException, InternalException {

		if( logger.isTraceEnabled() ) {
            logger.trace("ENTER - " + DigitalOceanModelFactory.class.getName() + ".destroyDroplet(" + provider + "," + id + ")");
		}

		String token = (String) provider.getContext().getConfigurationValue("token");

		String s = getModel(provider, doa.getRestMethod(), token,  getApiUrl(provider) + getEndpoint(doa, id), doa);
		
		try {
			//Delete have no output...
			if (doa.getRestMethod() == RESTMethod.DELETE) {
				return null;
			}
			
			Action result = (Action) DigitalOcean.ACTION.fromJson(s);
			
			if (!result.isError()) {
				return (Action) DigitalOcean.ACTION.fromJson(s);
			} else {
				//Not sure why in API V2 they removed the message of errors... we are now left blind
				throw new GeneralCloudException("An error occured while performing " + doa + " with parameters : " + doa.getParameters(), CloudErrorType.GENERAL);
			}
		} finally {
			if( logger.isTraceEnabled() ) {
	            logger.trace("EXIT - " + DigitalOceanModelFactory.class.getName() + ".destroyDroplet(" + provider + "," + id + ")");
	        }
		}
	}
	
	public static DigitalOceanRestModel performAction(@Nonnull org.dasein.cloud.digitalocean.DigitalOcean provider, DigitalOceanAction doa, IDigitalOcean returnObject) throws CloudException, InternalException {

		if( logger.isTraceEnabled() ) {
            logger.trace("ENTER - " + DigitalOceanModelFactory.class.getName() + ".performAction(" + provider.getCloudName() + ", " + returnObject + ")");
		}

		String token = (String) provider.getContext().getConfigurationValue("token");
        
		String s = getModel(provider, doa.getRestMethod(), token,  getApiUrl(provider) + getEndpoint(doa), doa);
		
		try {			
			return returnObject.fromJson(s);
        } finally {
			if( logger.isTraceEnabled() ) {
	            logger.trace("EXIT - " + DigitalOceanModelFactory.class.getName() + ".performAction(" + provider.getCloudName() + "," + returnObject + ")");
	        }
		}
	}

    /**
     * Return HTTP status code for an action request sent via HEAD method
     * @param provider
     * @param actionUrl
     * @return status code
     * @throws InternalException
     * @throws CloudException
     */
    public static int checkAction(@Nonnull org.dasein.cloud.digitalocean.DigitalOcean provider, String actionUrl) throws CloudException, InternalException {
        if( logger.isTraceEnabled() ) {
            logger.trace("ENTER - " + DigitalOceanModelFactory.class.getName() + ".checkAction(" + provider.getCloudName() + ")");
        }

        String token = (String) provider.getContext().getConfigurationValue("token");

        try {
            return sendRequest(provider, RESTMethod.HEAD, token,  getApiUrl(provider) + "v2/" + actionUrl, null).getStatusLine().getStatusCode();
        } finally {
            if( logger.isTraceEnabled() ) {
                logger.trace("EXIT - " + DigitalOceanModelFactory.class.getName() + ".checkAction(" + provider.getCloudName() + ")");
            }
        }
    }

    public static Droplet createInstance(org.dasein.cloud.digitalocean.DigitalOcean provider, String dropletName, String sizeId, String theImageId, String regionId, String bootstrapKey, Map<String, Object> extraParameters) throws CloudException, InternalException {

		if( logger.isTraceEnabled() ) {
            logger.trace("ENTER - " + DigitalOceanModelFactory.class.getName() + ".createInstance(" + dropletName + "," + sizeId + "," + theImageId + "," + regionId + "," + extraParameters + ")");
		}

        try {
			Create action = new Create(dropletName, sizeId, theImageId, regionId);
			List<String> ssh_key_ids = new ArrayList<String>();
			//Extra parameter is not part of DaseinCloud.... as its cloud specific
			if (extraParameters != null) {
				if (extraParameters.containsKey("backup_enabled")) {
                    action.setBackups((Boolean)extraParameters.get("backup_enabled"));
				}
				if (extraParameters.containsKey("private_networking")) {
                    action.setPrivateNetworking((Boolean)extraParameters.get("private_networking"));
				}
                if (extraParameters.containsKey("user_data")){
                    action.setUserdata((String)extraParameters.get("user_data"));
                }
			}
		
			if( bootstrapKey != null ) {
                ssh_key_ids.add(bootstrapKey);
			}
			action.setSshKeyIds(ssh_key_ids);
			
			return (Droplet) performAction(provider, action, DigitalOcean.DROPLET);
		} finally {
				
			if( logger.isTraceEnabled() ) {
	            logger.trace("EXIT - " + DigitalOceanModelFactory.class.getName() + ".createInstance(" + dropletName + "," + sizeId + "," + theImageId + "," + regionId + "," + extraParameters + ")");
			}
		}
	}

	public static Droplet getDropletByInstance(org.dasein.cloud.digitalocean.DigitalOcean provider, String dropletInstanceId) throws CloudException, InternalException {
		return (Droplet) getModelById(provider, DigitalOcean.DROPLET, dropletInstanceId);
	}

	public static Action getEventById(org.dasein.cloud.digitalocean.DigitalOcean provider, String id) throws CloudException, InternalException {
		return (Action) getModelById(provider, DigitalOcean.ACTION, id);
	}

    public static Actions getDropletEvents(org.dasein.cloud.digitalocean.DigitalOcean provider, String dropletId) throws CloudException, InternalException {
        return (Actions) getModelById(provider, DigitalOcean.DROPLET_ACTIONS, dropletId);
    }

    public static Actions getFloatingIpEvents(org.dasein.cloud.digitalocean.DigitalOcean provider, String floatingIp) throws CloudException, InternalException {
        return (Actions) getModelById(provider, DigitalOcean.FLOATING_IP_ACTIONS, floatingIp);
    }

}
