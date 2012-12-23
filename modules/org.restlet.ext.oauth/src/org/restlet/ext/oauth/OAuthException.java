/*
 * Copyright 2012 Shotaro Uchida <fantom@xmaker.mx>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.restlet.ext.oauth;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Exception that represents OAuth 2.0 (draft30) Errors.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-30">The OAuth 2.0 Authorization Framework
 *      draft-ietf-oauth-v2-30</a>
 */
public class OAuthException extends Exception {
    
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
            return new OAuthException(OAuthError.server_error, t.getMessage(), null);
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
