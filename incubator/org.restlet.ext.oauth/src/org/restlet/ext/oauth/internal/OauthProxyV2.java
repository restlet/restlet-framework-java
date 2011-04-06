/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
import org.restlet.data.Reference;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.security.Authorizer;
import org.restlet.security.User;

/**
 * Used for OAuth 2 draft 00 support. Will be removed once major players like
 * Facebook is up to spec.
 * 
 * @author Kristoffer Gronowski
 */
@Deprecated
public class OauthProxyV2 extends Authorizer {
    
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
    protected boolean authorize(Request request, Response response) {
        // StringBuilder response = new StringBuilder();
        Boolean auth = false;
        // Sets the no-store Cache-Control header
        request.setCacheDirectives(no);
        response.setCacheDirectives(no);

        redirectUri = request.getResourceRef().toUrl().toString();
        Form query = new Form(request.getOriginalRef().getQuery());

        String code = query.getFirstValue("code");
        getLogger().info("Incomming request query = " + query);

        if (code == null) {
            Form form = new Form();
            form.add("type", "web_server");
            form.add("client_id", params.getClientId());
            form.add("redirect_uri", redirectUri);
            // form.add("redirect_uri",
            // request.getReference().toUrl().toString());
            form.add("scope", params.getScope());
            try {
                form.encode();
            } catch (IOException ioe) {
                getLogger().warning(ioe.getMessage());
            }
            String q = form.getQueryString();
            getLogger().info("TEST query = " + q);

            Reference redirRef = new Reference(params.getBaseRef(),
                    params.getAuthorizePath(), q, null);
            // Reference redirRef = new
            // Reference(baseRef,"OAuth2Provider/authorize",q,null);
            getLogger().info("Redirecting to : " + redirRef.toUri());
            response.redirectSeeOther(redirRef);
            // response.commit();
            getLogger().info("After Redirecting to : " + redirRef.toUri());
            // return true;
            // return null;
        } else {
            getLogger().info("Came back after SNS code = " + code);

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
            form.add("redirect_uri", request.getResourceRef().getBaseRef().toUri().toString());
            String redir = request.getResourceRef().getHostIdentifier() + 
            request.getResourceRef().getPath();
            form.add("redirect_uri", redir);
            form.add("client_secret", params.getClientSecret());
            form.add("code", code);

            Representation body = tokenResource.post(form
                    .getWebRepresentation());
            if (tokenResource.getResponse().getStatus().isSuccess()) {
                Form answer = new Form(body);
                getLogger().info(
                        "Got answer on AccessToken = " + answer.toString());
                accessToken = answer.getFirstValue("access_token");
                getLogger().info("AccessToken in changed OldOauthProxy = " + accessToken);
                request.getClientInfo().setUser(new User(accessToken, accessToken.toCharArray()));
                request.getClientInfo().setAuthenticated(true);
                auth = true;
            }
            getLogger().info("Before graph release");
            body.release();
            tokenResource.release();
            graphResource.release();
        }
        return auth;
    }

    @Override
    protected int unauthorized(Request request, Response response) {
        if (response.getStatus().isRedirection())
            return STOP;
        return super.unauthorized(request, response);
    }

    public String getAccessToken() {
        return accessToken;
    }

}