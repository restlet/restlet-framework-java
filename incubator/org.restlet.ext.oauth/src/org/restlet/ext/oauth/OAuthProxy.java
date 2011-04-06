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

package org.restlet.ext.oauth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.ext.oauth.OAuthError.ErrorCode;
import org.restlet.ext.oauth.internal.CookieCopyClientResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.security.Authorizer;

/**
 * A restlet filter for initiating a web server flow or comparable to OAuth 2.0
 * 3-legged authorization.
 * 
 * On successful execution a working OAuth token will be maintained. It is
 * recommended to put a ServerResource after this filter to display to the end
 * user on successful service setup.
 * 
 * The following example shows how to protect "DummyResource" using OAuth.
 * 
 * <pre>
 * {
 *     &#064;code
 *     OAuthParameter params = new OAuthParameters(&quot;clientId&quot;, &quot;clientSecret&quot;,
 *             oauthURL, &quot;scope1 scope2&quot;);
 *     OAuthProxy proxy = new OauthProxy(params, getContext(), true);
 *     proxy.setNext(DummyResource.class);
 *     router.attach(&quot;/write&quot;, write);
 * }
 * </pre>
 * 
 * @author Kristoffer Gronowski
 * @see org.restlet.ext.oauth.OAuthParameters
 */
public class OAuthProxy extends Authorizer {

    List<CacheDirective> no = new ArrayList<CacheDirective>();
    
    public static final String VERSION = "DRAFT-10";

    private final OAuthParameters params;

    private ClientResource tokenResource;

    private String redirectUri;

    private OAuthUser authUser;

    private boolean basicSecret = false;

    protected Logger log;

    /**
     * Set up an OAuthProxy. Defaults to form based authentication and not http
     * basic
     * 
     * @param params
     *            OAuth parameters
     * @param ctx
     *            Restlet context
     */
    public OAuthProxy(OAuthParameters params, Context ctx) {
        setContext(ctx);
        this.params = params;
        no.add(CacheDirective.noStore());
        log = ctx.getLogger();
    }

    /**
     * Set up a an OauthProxy.
     * 
     * @param params
     *            The parameters
     * @param ctx
     *            Restlet Context
     * @param useBasicSecret
     *            If true use http basic authentication otherwise use form based
     */
    public OAuthProxy(OAuthParameters params, Context ctx,
            boolean useBasicSecret) {
        this(params, ctx);
        basicSecret = useBasicSecret;
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        tokenResource = new CookieCopyClientResource(params.getBaseRef()
                + params.getAccessTokenPath());
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        tokenResource.release();
    }

    @Override
    protected boolean authorize(Request request, Response response) {
        // StringBuilder response = new StringBuilder();
        Boolean auth = false;
        // Sets the no-store Cache-Control header
        request.setCacheDirectives(no);

        redirectUri = request.getResourceRef().toUrl().toString();
        Form query = new Form(request.getOriginalRef().getQuery());

        String error = query.getFirstValue(OAuthServerResource.ERROR);
        if (error != null && error.length() > 0) {
            // Failed in initial auth resource request

            Representation repr = new EmptyRepresentation();
            String desc = query.getFirstValue(OAuthServerResource.ERROR_DESC);
            String uri = query.getFirstValue(OAuthServerResource.ERROR_URI);
            if (desc != null || uri != null) {
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

            ErrorCode ec = ErrorCode.valueOf(error);
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
                log.warning("Unhandled error response type. " + ec.name());
            }

            return false;
        }

        String code = query.getFirstValue(OAuthServerResource.CODE);
        log.info("Incomming request query = " + query);

        if (code == null) {
            Form form = new Form();
            form.add(OAuthServerResource.RESPONSE_TYPE,
                    OAuthServerResource.ResponseType.code.name());
            form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
            form.add(OAuthServerResource.REDIR_URI, redirectUri);
            form.add(OAuthServerResource.SCOPE, params.getScope());
            // if( params.getOwner() != null && params.getOwner().length() > 0 )
            // {
            // form.add(OAuthResource.OWNER,params.getOwner());
            // }
            try {
                form.encode();
            } catch (IOException ioe) {
                getLogger().warning(ioe.getMessage());
            }
            String q = form.getQueryString();

            Reference redirRef = new Reference(params.getBaseRef(),
                    params.getAuthorizePath(), q, null);
            log.info("Redirecting to : " + redirRef.toUri());
            // response.redirectSeeOther(redirRef);
            response.setCacheDirectives(no);
            response.redirectTemporary(redirRef);
            // response.commit();
            log.info("After Redirecting to : " + redirRef.toUri());
            // return true;
            // return null;
        } else {
            log.info("Came back after SNS code = " + code);


            Form form = new Form();
            form.add(OAuthServerResource.GRANT_TYPE,
                    OAuthServerResource.GrantType.authorization_code.name());
            form.add(OAuthServerResource.REDIR_URI, request.getResourceRef()
                    .getBaseRef().toUri().toString());
            if (basicSecret) {
                ChallengeResponse authentication = new ChallengeResponse(
                        ChallengeScheme.HTTP_BASIC);
                authentication.setDigestAlgorithm("NONE");
                String basic = params.getClientId() + ':'
                        + params.getClientSecret();
                authentication.setRawValue(Base64.encode(basic.getBytes(),
                        false));
                tokenResource.getRequest().setChallengeResponse(authentication);
            } else {
                form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
                form.add(OAuthServerResource.CLIENT_SECRET,
                        params.getClientSecret());
            }
            form.add(OAuthServerResource.CODE, code);
            log.info("Sending access form : " + form.getQueryString()
                    + " to : " + tokenResource.getReference());

            Representation body = tokenResource.post(form
                    .getWebRepresentation());

            if (tokenResource.getResponse().getStatus().isSuccess()) {
                // Store away the user
                authUser = OAuthUtils.handleSuccessResponse(body);
                if (authUser != null) {
                    request.getClientInfo().setUser(authUser);
                    request.getClientInfo().setAuthenticated(true);
                    log.info("storing to context = : " + getContext());
                    // continue in the filter chain
                    auth = true;
                }
            }
            log.info("Before sns release");
            body.release();
        }
        return auth;
    }

    @Override
    protected int unauthorized(Request request, Response response) {
        if (response.getStatus().isSuccess()
                || response.getStatus().isServerError()) {
            return super.unauthorized(request, response);
        }

        // If redirect or a specific client error just propaget it on
        return STOP;
    }

    public String getAccessToken() {
        if (authUser == null)
            return null;
        return authUser.getAccessToken();
    }

    public String getRefreshToken() {
        if (authUser == null)
            return null;
        return authUser.getRefreshToken();
    }

    public long getExpiresIn() {
        if (authUser == null)
            return 0;
        return authUser.getExpiresIn();
    }

    public OAuthUser getAuthUser() {
        return authUser;
    }

}