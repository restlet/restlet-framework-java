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

package org.restlet.ext.oauth.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Filter;

/**
 * Used for OAuth 2 draft 00 support. Will be removed once major players like
 * Facebook is up to spec.
 * 
 * @author Kristoffer Gronowski
 */
@Deprecated
public class OauthProxyV2 extends Filter {

    public static final String VERSION = "DRAFT-2";

    List<CacheDirective> no = new ArrayList<CacheDirective>();

    private final OAuthParameters params;

    private String redirectUri;

    private String accessToken = null;

    public OauthProxyV2(OAuthParameters params, Context ctx) {
        setContext(ctx);
        this.params = params;
        no.add(CacheDirective.noStore());
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        // StringBuilder response = new StringBuilder();
        Boolean auth = false;
        // Sets the no-store Cache-Control header
        request.setCacheDirectives(no);
        response.setCacheDirectives(no);

        redirectUri = request.getResourceRef().toUrl().toString();
        Form query = new Form(request.getOriginalRef().getQuery());
        if(handleError(query, response))
            return STOP;
        
        
        String code = query.getFirstValue("code");
        getLogger().fine("Incomming request query = " + query);

        if (code == null) {
            Form form = new Form();
            form.add("type", "web_server");
            form.add("client_id", params.getClientId());
            form.add("redirect_uri", redirectUri);
            // form.add("redirect_uri",
            // request.getReference().toUrl().toString());
            //form.add("scope", params.getScope());
            form.add("scope", Scopes.toScope(params.getRoles()));
            try {
                form.encode();
            } catch (IOException ioe) {
                getLogger().warning(ioe.getMessage());
            }
            String q = form.getQueryString();
            getLogger().fine("TEST query = " + q);

            Reference redirRef = new Reference(params.getBaseRef(),
                    params.getAuthorizePath(), q, null);
            // Reference redirRef = new
            // Reference(baseRef,"OAuth2Provider/authorize",q,null);
            getLogger().fine("Redirecting to : " + redirRef.toUri());
            response.redirectSeeOther(redirRef);
            // response.commit();
            getLogger().fine("After Redirecting to : " + redirRef.toUri());
            // return true;
            // return null;
        } else {
            getLogger().fine("Came back after SNS code = " + code);

            ClientResource graphResource = new ClientResource(
                    params.getBaseRef());

            ClientResource tokenResource = graphResource.getChild(params
                    .getAccessTokenPath());
            // ClientResource tokenResource = graphResource
            // .getChild("OAuth2Provider/access_token");
            Form form = new Form();
            form.add("type", "web_server");
            form.add("client_id", params.getClientId());
            // form.add("redirect_uri", redirectUri);
            // form.add("redirect_uri",
            // request.getResourceRef().getBaseRef().toUri().toString());
            String redir = request.getResourceRef().getHostIdentifier()
                    + request.getResourceRef().getPath();
            form.add("redirect_uri", redir);
            form.add("client_secret", params.getClientSecret());
            form.add("code", code);

            Representation body = tokenResource.post(form
                    .getWebRepresentation());
            if (tokenResource.getStatus().isSuccess()) {
                Form answer = new Form(body);
                getLogger().fine(
                        "Got answer on AccessToken = " + answer.toString());
                accessToken = answer.getFirstValue("access_token");
                getLogger()
                        .fine("AccessToken in changed OldOauthProxy = "
                                + accessToken);
                request.getClientInfo().setUser(
                        new OAuthUser(request.getClientInfo().getUser(), accessToken));
                request.getClientInfo().setAuthenticated(true);
                auth = true;
            }
            getLogger().fine("Before graph release");
            body.release();
            tokenResource.release();
            graphResource.release();
        }
        if(auth){
            return CONTINUE;
        }
        else{
            if (response.getStatus().isSuccess()
                    || response.getStatus().isServerError()) {
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            }
            return STOP;
        }
    }

    /*
    @Override
    protected int unauthorized(Request request, Response response) {
        if (response.getStatus().isRedirection())
            return STOP;
        return super.unauthorized(request, response);
    }
    */
    
    private boolean handleError(Form query, Response response){
        String error = query.getFirstValue(OAuthServerResource.ERROR);

        if ((error != null) && (error.length() > 0)) {
            // Failed in initial auth resource request
            Representation repr = new EmptyRepresentation();
            String desc = query.getFirstValue(OAuthServerResource.ERROR_DESC);
            String uri = query.getFirstValue(OAuthServerResource.ERROR_URI);

            if ((desc != null) || (uri != null)) {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><body><pre>");
                sb.append("OAuth2 error detected.\n");

                if (desc != null) {
                    sb.append("Error description : ").append(desc);
                }

                if (uri != null) {
                    sb.append("<a href=\"");
                    sb.append(uri);
                    sb.append("\">Error Description</a>");
                }

                sb.append("</pre></body></html>");

                repr = new StringRepresentation(sb.toString(),
                        MediaType.TEXT_HTML);
            }

            OAuthError ec = OAuthError.valueOf(error);

            switch (ec) {
            case invalid_request:
                response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, error);
                response.setEntity(repr);
                break;
            case invalid_client:
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, error);
                response.setEntity(repr);
                break;
            case unauthorized_client:
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, error);
                response.setEntity(repr);
                break;
            case redirect_uri_mismatch:
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, error);
                response.setEntity(repr);
                break;
            case access_denied:
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, error);
                response.setEntity(repr);
                break;
            case unsupported_response_type:
                response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, error);
                response.setEntity(repr);
                break;
            case invalid_scope:
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, error);
                response.setEntity(repr);
                break;
            default:
                getLogger().warning(
                        "Unhandled error response type. " + ec.name());
            }
            return true;
            // return false;
        }
        return false;
    }

}
