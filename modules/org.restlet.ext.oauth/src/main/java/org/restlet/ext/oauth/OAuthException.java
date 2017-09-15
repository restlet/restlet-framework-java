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

package org.restlet.ext.oauth;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.engine.util.StringUtils;

/**
 * Exception that represents OAuth 2.0 (RFC6749) Errors.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @see <a href="http://tools.ietf.org/html/rfc6749">The OAuth 2.0 Authorization Framework (RFC6749)</a>
 */
public class OAuthException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Returns a new {@link OAuthException} based on the given parameters. It especially looks for the given parameters:<br>
     * {@link OAuthResourceDefs#ERROR} for the kind of Exception (see {@link OAuthError} for the supported values),<br>
     * {@link OAuthResourceDefs#ERROR_DESC} for the description,<br>
     * {@link OAuthResourceDefs#ERROR_URI} for the URI of a documentation resource. <br>
     * 
     * @param parameters
     *            The parameters used to set up the {@link OAuthException}.
     * @return A new instance of {@link OAuthException}.
     */
    public static OAuthException toOAuthException(Form parameters) {
        OAuthError error = Enum.valueOf(OAuthError.class, parameters.getFirstValue(OAuthResourceDefs.ERROR));
        OAuthException ex = new OAuthException(error);
        ex.errorDescription = parameters.getFirstValue(OAuthResourceDefs.ERROR_DESC);
        ex.errorUri = parameters.getFirstValue(OAuthResourceDefs.ERROR_URI);

        return ex;
    }

    /**
     * Returns a new {@link OAuthException} based on the JSON payload. It especially looks for the given parameters:<br>
     * {@link OAuthResourceDefs#ERROR} for the kind of Exception (see {@link OAuthError} for the supported values),<br>
     * {@link OAuthResourceDefs#ERROR_DESC} for the description,<br>
     * {@link OAuthResourceDefs#ERROR_URI} for the URI of a documentation resource. <br>
     * 
     * @param json
     *            The JSON object used to set up the {@link OAuthException}.
     * @return A new instance of {@link OAuthException}.
     */
    public static OAuthException toOAuthException(JSONObject json) throws JSONException {
        OAuthError error = Enum.valueOf(OAuthError.class, json.getString(OAuthResourceDefs.ERROR));
        OAuthException ex = new OAuthException(error);
        if (json.has(OAuthResourceDefs.ERROR_DESC)) {
            ex.errorDescription = json.getString(OAuthResourceDefs.ERROR_DESC);
        }
        if (json.has(OAuthResourceDefs.ERROR_URI)) {
            ex.errorUri = json.getString(OAuthResourceDefs.ERROR_URI);
        }
        return ex;
    }

    /**
     * Returns a new {@link OAuthException} based on the given exception.
     * 
     * @param exception
     *            The exception.
     * @return A new instance {@link OAuthException}.
     */
    public static OAuthException toOAuthException(Throwable exception) {
        if (exception instanceof OAuthException) {
            return (OAuthException) exception;
        } else if (exception.getCause() instanceof OAuthException) {
            return (OAuthException) exception.getCause();
        }

        Logger.getLogger(OAuthException.class.getName()).log(Level.SEVERE, "Internal Server Error.", exception);
        return new OAuthException(OAuthError.server_error, exception.getMessage(), null);
    }

    /** The kind of OAuth error. */
    private OAuthError error;

    /** The description of the error. */
    private String errorDescription;

    /** The URI of the resource that gives more details about the error. */
    private String errorUri;

    /**
     * Constructor.
     * 
     * @param error
     */
    private OAuthException(OAuthError error) {
        super(error.name());
        this.error = error;
    }

    /**
     * Constructor.
     * 
     * @param error
     *            The OAuth error.
     * @param description
     *            The description of the exception.
     * @param errorUri
     *            The URI of the resource that gives more details about the error.
     */
    public OAuthException(OAuthError error, String description, String errorUri) {
        super(error.name());
        this.error = error;
        this.errorDescription = description;
        this.errorUri = errorUri;
    }

    /**
     * Returns a JSON representation of the current object as a {@link JSONObject}.
     * 
     * @return A JSON representation of the current object as a {@link JSONObject}.
     * @throws JSONException
     */
    public JSONObject createErrorDocument() throws JSONException {
        JSONObject result = new JSONObject();

        result.put(OAuthResourceDefs.ERROR, error.name());
        if (!StringUtils.isNullOrEmpty(errorDescription)) {
            result.put(OAuthResourceDefs.ERROR_DESC, errorDescription);
        }
        if (!StringUtils.isNullOrEmpty(errorUri)) {
            result.put(OAuthResourceDefs.ERROR_URI, errorUri);
        }

        return result;
    }

    /**
     * Returns the kind of OAuth error.
     * 
     * @return The kind of OAuth error.
     */
    public OAuthError getError() {
        return error;
    }

    /**
     * Returns the description of the error.
     * 
     * @return The description of the error.
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Returns the URI of the resource that gives more details about the error.
     * 
     * @return The URI of the resource that gives more details about the error.
     */
    public String getErrorUri() {
        return errorUri;
    }

    /**
     * 
     * @return
     * @deprecated use {@link #getErrorUri()} instead.
     */
    @Deprecated
    public String getErrorURI() {
        return errorUri;
    }

    /**
     * Sets the kind of OAuth error.
     * 
     * @param error
     *            The kind of OAuth error.
     */
    public void setError(OAuthError error) {
        this.error = error;
    }

    /**
     * Sets the description of the error.
     * 
     * @param errorDescription
     *            The description of the error.
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * Sets the URI of the resource that gives more details about the error.
     * 
     * @param errorUri
     *            The URI of the resource that gives more details about the error.
     */
    public void setErrorUri(String errorUri) {
        this.errorUri = errorUri;
    }
}
