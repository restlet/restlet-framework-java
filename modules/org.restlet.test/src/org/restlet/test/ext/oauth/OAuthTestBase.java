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

import static org.restlet.ext.oauth.OAuthResourceDefs.TOKEN_TYPE_BEARER;

import java.util.Map;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.ResponseType;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.ServerToken;
import org.restlet.ext.oauth.internal.Token;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class OAuthTestBase {

    public static final String STUB_ACCESS_TOKEN = "mF_9.B5f-4.1JqM";

    public static final String STUB_REFRESH_TOKEN = "tGzv3JOkF0XG5Qx2TlKWIA";

    public static final String STUB_CLIENT_ID = "s6BhdRkqt3";

    public static final String STUB_CLIENT_SECRET = "7Fjfp0ZBr1KtDRbnfVdmIw";

    public static final String STUB_USERNAME = "johndoe";

    public static final String STUB_PASSWORD = "A3ddj3w";

    public static final String STUB_CODE = "SplxlOBeZQQYbYS6WxSbIA";

    public static Token STUB_TOKEN = new ServerToken() {

        public String getAccessToken() {
            return STUB_ACCESS_TOKEN;
        }

        public String getTokenType() {
            return TOKEN_TYPE_BEARER;
        }

        public int getExpirePeriod() {
            return 3600;
        }

        public String getRefreshToken() {
            return STUB_REFRESH_TOKEN;
        }

        public String[] getScope() {
            return new String[] { "a", "b" };
        }

        public String getUsername() {
            return STUB_USERNAME;
        }

        public String getClientId() {
            return STUB_CLIENT_ID;
        }

        public boolean isExpired() {
            return false;
        }
    };
    
    public static Token SPRING_STUB_TOKEN = new ServerToken() {

        public String getAccessToken() {
            return STUB_ACCESS_TOKEN;
        }

        public String getTokenType() {
        	//Spring returns bearer in lower case
            return "bearer";
        }

        public int getExpirePeriod() {
            return 3600;
        }

        public String getRefreshToken() {
            return STUB_REFRESH_TOKEN;
        }

        public String[] getScope() {
            return new String[] { "a", "b" };
        }

        public String getUsername() {
            return STUB_USERNAME;
        }

        public String getClientId() {
            return STUB_CLIENT_ID;
        }

        public boolean isExpired() {
            return false;
        }
    };

    public static final Client STUB_CLIENT = new Client() {

        public String getClientId() {
            return STUB_CLIENT_ID;
        }

        public char[] getClientSecret() {
            return STUB_CLIENT_SECRET.toCharArray();
        }

        public String[] getRedirectURIs() {
            return null;
        }

        public Map<String, Object> getProperties() {
            return null;
        }

        public boolean isResponseTypeAllowed(ResponseType responseType) {
            return true;
        }

        public boolean isGrantTypeAllowed(GrantType grantType) {
            return true;
        }

        public ClientType getClientType() {
            return ClientType.CONFIDENTIAL;
        }

    };

    protected static Component component;

    protected Reference baseURI = new Reference(Protocol.HTTP, "localhost",
            8080);

}
