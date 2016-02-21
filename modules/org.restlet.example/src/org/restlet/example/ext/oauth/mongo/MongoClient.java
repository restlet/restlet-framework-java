/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.ext.oauth.mongo;

import java.util.List;
import java.util.Map;

import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.ResponseType;
import org.restlet.ext.oauth.internal.Client;

import com.mongodb.DBObject;

/**
 * MongoDB implementation of Client interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MongoClient implements Client {

    public static final String CLIENT_SECRET = "client_secret";

    public static final String CLIENT_TYPE = "client_type";

    public static final String REDIRECT_URIS = "redirect_uris";

    public static final String ALLOWED_RESPONSE_TYPES = "allowed_response_types";

    public static final String ALLOWED_GRANT_TYPES = "allowed_grant_types";

    public static final String PROPERTIES = "properties";

    private DBObject client;

    public MongoClient(DBObject client) {
        this.client = client;
    }

    @Override
    public String getClientId() {
        return client.get("_id").toString();
    }

    @Override
    public char[] getClientSecret() {
        if (client.containsField(CLIENT_SECRET)) {
            return client.get(CLIENT_SECRET).toString().toCharArray();
        }
        return null;
    }

    @Override
    public String[] getRedirectURIs() {
        if (client.containsField(REDIRECT_URIS)) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) client.get(REDIRECT_URIS);
            String[] uris = new String[list.size()];

            for (int i = 0; i < list.size(); i++) {
                uris[i] = list.get(i).toString();
            }

            return uris;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getProperties() {
        DBObject properties = (DBObject) client.get(PROPERTIES);
        return properties.toMap();
    }

    public ClientType getClientType() {
        String type = client.get(CLIENT_TYPE).toString();
        if (type.equals("public")) {
            return ClientType.PUBLIC;
        } else if (type.equals("confidential")) {
            return ClientType.CONFIDENTIAL;
        }
        throw new IllegalStateException("Unknown Client Type");
    }

    public boolean isResponseTypeAllowed(ResponseType responseType) {
        return isTypeAllowed(ALLOWED_RESPONSE_TYPES, responseType.name());
    }

    public boolean isGrantTypeAllowed(GrantType grantType) {
        return isTypeAllowed(ALLOWED_GRANT_TYPES, grantType.name());
    }

    private boolean isTypeAllowed(String field, String typeName) {
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) client.get(field);

        for (Object allowedType : list) {
            if (allowedType.toString().equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return client.toString();
    }
}
