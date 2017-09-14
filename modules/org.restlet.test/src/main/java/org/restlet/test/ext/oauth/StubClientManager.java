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

package org.restlet.test.ext.oauth;

import java.util.Map;

import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Client.ClientType;
import org.restlet.ext.oauth.internal.ClientManager;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class StubClientManager extends OAuthTestBase implements ClientManager {

    public Client createClient(ClientType clientType, String[] redirectURIs,
            Map<String, Object> properties) {
        return null;
    }

    public void deleteClient(String id) {
    }

    public Client findById(String id) {
        if (id.equals(STUB_CLIENT_ID)) {
            return STUB_CLIENT;
        } else {
            return null;
        }
    }
}
