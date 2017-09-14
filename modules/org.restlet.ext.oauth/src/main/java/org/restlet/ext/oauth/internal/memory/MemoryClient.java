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

package org.restlet.ext.oauth.internal.memory;

import java.util.Arrays;
import java.util.Map;

import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.ResponseType;
import org.restlet.ext.oauth.internal.Client;

/**
 * Memory implementation of Client interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MemoryClient implements Client {

    private String clientId;

    private char[] clientSecret;

    private ClientType clientType;

    private Map<String, Object> properties;

    private String[] redirectURIs;

    protected MemoryClient(String clientId, ClientType clientType, String[] redirectURIs, Map<String, Object> properties) {
        this.clientId = clientId;
        this.clientType = clientType;
        this.redirectURIs = redirectURIs;
        this.properties = properties;
    }

    public String getClientId() {
        return clientId;
    }

    public char[] getClientSecret() {
        return clientSecret;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String[] getRedirectURIs() {
        return redirectURIs;
    }

    private boolean isFlowSupported(Object flow) {
        return Arrays.asList(
                (Object[]) properties.get(PROPERTY_SUPPORTED_FLOWS)).contains(
                flow);
    }

    public boolean isGrantTypeAllowed(GrantType grantType) {
        return isFlowSupported(grantType);
    }

    public boolean isResponseTypeAllowed(ResponseType responseType) {
        return isFlowSupported(responseType);
    }

    protected void setClientSecret(char[] clientSecret) {
        this.clientSecret = clientSecret;
    }
}
