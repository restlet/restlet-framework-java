/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.oauth.internal;

import java.util.Map;
import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.ResponseType;

/**
 * A POJO representing a OAuth client_id. Each client can have collected a
 * number of authenticated users to allow working on their behalf.
 * 
 * Implementors should implement the storage and retrieval.
 * 
 * @author Kristoffer Gronowski
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public interface Client {

    public static final String PROPERTY_APPLICATION_NAME = "application_name";

    public static final String PROPERTY_DESCRIPTION = "description";

    public static final String PROPERTY_SUPPORTED_FLOWS = "supported_flows";

    public static enum ClientType {
        CONFIDENTIAL, PUBLIC
    }

    /**
     * Client id that the client has registered at the auth provider.
     * 
     * @return the stored client id
     */
    public String getClientId();

    /**
     * Client secret that the client has registered at the auth provider.
     * 
     * @return the stored client secret
     */

    public char[] getClientSecret();

    /**
     * Redirect URL that the client has registered at the auth provider.
     * 
     * @return redirect callback url for code and token flows.
     */
    public String[] getRedirectURIs();

    public Map<String, Object> getProperties();

    public boolean isResponseTypeAllowed(ResponseType responseType);

    public boolean isGrantTypeAllowed(GrantType grantType);

    public ClientType getClientType();
}
