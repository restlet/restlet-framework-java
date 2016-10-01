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

import java.net.URI;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class ProtectedClientResource extends ClientResource implements
        OAuthResourceDefs {

    private volatile Token token;

    private volatile boolean useBodyMethod;

    protected ProtectedClientResource() {
        super();
    }

    public ProtectedClientResource(ClientResource resource) {
        super(resource);
    }

    public ProtectedClientResource(Context context, Method method,
            Reference reference) {
        super(context, method, reference);
    }

    public ProtectedClientResource(Context context, Method method, String uri) {
        super(context, method, uri);
    }

    public ProtectedClientResource(Context context, Method method, URI uri) {
        super(context, method, uri);
    }

    public ProtectedClientResource(Context context, Reference reference) {
        super(context, reference);
    }

    public ProtectedClientResource(Context context, Request request,
            Response response) {
        super(context, request, response);
    }

    public ProtectedClientResource(Context context, String uri) {
        super(context, uri);
    }

    public ProtectedClientResource(Context context, URI uri) {
        super(context, uri);
    }

    public ProtectedClientResource(Method method, Reference reference) {
        super(method, reference);
    }

    public ProtectedClientResource(Method method, String uri) {
        super(method, uri);
    }

    public ProtectedClientResource(Method method, URI uri) {
        super(method, uri);
    }

    public ProtectedClientResource(Reference reference) {
        super(reference);
    }

    public ProtectedClientResource(Request request, Response response) {
        super(request, response);
    }

    public ProtectedClientResource(String uri) {
        super(uri);
    }

    public ProtectedClientResource(URI uri) {
        super(uri);
    }

    public Token getToken() {
        return token;
    }

    @Override
    public Response handleOutbound(Request request) {
        if (token == null) {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Token not found");
        }
        if (TOKEN_TYPE_BEARER.equalsIgnoreCase(token.getTokenType())) {
            if (isUseBodyMethod()) {
                Representation entity = request.getEntity();
                if (entity != null
                        && entity.getMediaType().equals(
                                MediaType.APPLICATION_WWW_FORM)) {
                    Form form = new Form(entity);
                    form.add(ACCESS_TOKEN, token.getAccessToken());
                    request.setEntity(form.getWebRepresentation());
                } else {
                    request.getResourceRef().addQueryParameter(ACCESS_TOKEN,
                            token.getAccessToken());
                }
            } else {
                ChallengeResponse cr = new ChallengeResponse(
                        ChallengeScheme.HTTP_OAUTH_BEARER);
                cr.setRawValue(token.getAccessToken());
                request.setChallengeResponse(cr);
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Unsupported token type.");
        }
        return super.handleOutbound(request);
    }

    public boolean isUseBodyMethod() {
        return useBodyMethod;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void setUseBodyMethod(boolean useBodyMethod) {
        this.useBodyMethod = useBodyMethod;
    }
}
