/**
 * Copyright 2005-2012 Restlet S.A.S.
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

package org.restlet.ext.oauth;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Exception that represents OAuth 2.0 (draft30) Errors.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @see <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-30">The OAuth
 *      2.0 Authorization Framework draft-ietf-oauth-v2-30</a>
 */
public class OAuthException extends Exception {

    private static final long serialVersionUID = 1L;

    private OAuthError error;

    private String description;

    private String errorUri;

    public OAuthException(OAuthError error, String description, String errorUri) {
        this.error = error;
        this.description = description;
        this.errorUri = errorUri;
    }

    public static OAuthException toOAuthException(Throwable t) {
        if (t instanceof OAuthException) {
            return (OAuthException) t;
        } else if (t.getCause() instanceof OAuthException) {
            return (OAuthException) t.getCause();
        } else {
            return new OAuthException(OAuthError.server_error, t.getMessage(),
                    null);
        }
    }

    public OAuthError getError() {
        return error;
    }

    public String getErrorDescription() {
        return description;
    }

    public String getErrorURI() {
        return errorUri;
    }

    public JSONObject createErrorDocument() throws JSONException {
        JSONObject result = new JSONObject();

        result.put(OAuthServerResource.ERROR, error.name());
        if ((description != null) && (description.length() > 0)) {
            result.put(OAuthServerResource.ERROR_DESC, description);
        }
        if ((errorUri != null) && (errorUri.length() > 0)) {
            result.put(OAuthServerResource.ERROR_URI, errorUri);
        }

        return result;
    }
}
