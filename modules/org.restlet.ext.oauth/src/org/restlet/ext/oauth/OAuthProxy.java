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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.restlet.ext.oauth.internal.CookieCopyClientResource;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;

/**
 * A restlet filter for initiating a web server flow or comparable to OAuth 2.0
 * 3-legged authorization.
 * 
 * On successful execution a working OAuth token will be maintained. It is
 * recommended to put a ServerResource after this filter to display to the end
 * user on successful service setup.
 * 
 * The following example shows how to gain an accesstoken that will be available
 * for "DummyResource" to use to access some remote protected resource
 * 
 * <pre>
 * {
 *     &#064;code
 *     OAuthParameter params = new OAuthParameters(&quot;clientId&quot;, &quot;clientSecret&quot;,
 *             oauthURL, &quot;scope1 scope2&quot;);
 *     OAuthProxy proxy = new OauthProxy(params, getContext(), true);
 *     proxy.setNext(DummyResource.class);
 *     router.attach(&quot;/write&quot;, write);
 *     
 *     //A Slightly more advanced example that also sets some SSL client parameters
 *     Client client = new Client(Protocol.HTTPS);
 *     Context c = new Context();
 *     client.setContext(c);
 *     c.getParameters().add(&quot;truststorePath&quot;, &quot;pathToKeyStoreFile&quot;);
 *        c.getParameters(0.add(&quot;truststorePassword&quot;, &quot;password&quot;);
 *     OAuthParameter params = new OAuthParameters(&quot;clientId&quot;, &quot;clientSecret&quot;,
 *             oauthURL, &quot;scope1 scope2&quot;);
 *     OAuthProxy proxy = new OauthProxy(params, getContext(), true, client);
 *     proxy.setNext(DummyResource.class);
 *     router.attach(&quot;/write&quot;, write);
 *     
 *     
 * }
 * </pre>
 * 
 * @author Kristoffer Gronowski
 * @see org.restlet.ext.oauth.OAuthParameters
 */
public class OAuthProxy extends Filter {

    private final static List<CacheDirective> no = new ArrayList<CacheDirective>();

    private final static String VERSION = "DRAFT-10";

    /**
     * Returns the current proxy's version.
     * 
     * @return The current proxy's version.
     */
    public static String getVersion() {
        return VERSION;
    }

    private final OAuthParameters params;

    private final boolean basicSecret;

    private final org.restlet.Client cc;

    /**
     * Sets up an OauthProxy. Defaults to form based authentication and not http
     * basic.
     * 
     * @param params
     *            The OAuth parameters.
     * @param ctx
     *            The Restlet context.
     */
    public OAuthProxy(OAuthParameters params, Context ctx) {
        this(params, ctx, false);
    }

    /**
     * Sets up an OAuthProxy.
     * 
     * @param params
     *            The OAuth parameters.
     * @param useBasicSecret
     *            If true use http basic authentication otherwise use form
     *            based.
     * @param ctx
     *            The Restlet context.
     */
    public OAuthProxy(OAuthParameters params, Context ctx,
            boolean useBasicSecret) {
        this(params, ctx, useBasicSecret, null);
    }

    /**
     * Sets up an OAuthProxy.
     * 
     * @param params
     *            The OAuth parameters.
     * @param useBasicSecret
     *            If true use http basic authentication otherwise use form
     *            based.
     * @param ctx
     *            The Restlet context.
     * @param requestClient
     *            A predefined client that will be used for remote client
     *            request. Useful when you need to set e.g. SSL initialization
     *            parameters
     */
    public OAuthProxy(OAuthParameters params, Context ctx,
            boolean useBasicSecret, org.restlet.Client requestClient) {
        this.basicSecret = useBasicSecret;
        setContext(ctx);
        this.params = params;
        no.add(CacheDirective.noStore());
        this.cc = requestClient;
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        // StringBuilder response = new StringBuilder();
        Boolean auth = false;
        // Sets the no-store Cache-Control header
        request.setCacheDirectives(no);
        String redirectUri = request.getResourceRef().toUrl().toString();

        Form query = new Form(request.getOriginalRef().getQuery());

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
            return STOP;
            // return false;
        }

        String code = query.getFirstValue(OAuthServerResource.CODE);
        getLogger().fine("Incomming request query = " + query);

        if (code == null) {
            Form form = new Form();
            form.add(OAuthServerResource.RESPONSE_TYPE,
                    ResponseType.code.name());
            form.add(OAuthServerResource.CLIENT_ID, this.params.getClientId());
            form.add(OAuthServerResource.REDIR_URI, redirectUri);
            // OLD form.add(OAuthServerResource.SCOPE, params.getScope());
            form.add(OAuthServerResource.SCOPE,
                    Scopes.toScope(this.params.getRoles()));

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

            Reference redirRef = new Reference(this.params.getBaseRef(),
                    this.params.getAuthorizePath(), q, null);
            getLogger().fine("Redirecting to : " + redirRef.toUri());
            response.setCacheDirectives(no);
            response.redirectTemporary(redirRef);
            getLogger().fine("After Redirecting to : " + redirRef.toUri());
        } else {
            getLogger().fine("Came back after SNS code = " + code);
            ClientResource tokenResource = new CookieCopyClientResource(
                    this.params.getBaseRef() + this.params.getAccessTokenPath());
            if (this.cc != null) {
                tokenResource.setNext(this.cc);
            }
            Form form = new Form();
            form.add(OAuthServerResource.GRANT_TYPE,
                    GrantType.authorization_code.name());
            String redir = request.getResourceRef().getHostIdentifier()
                    + request.getResourceRef().getPath();
            form.add(OAuthServerResource.REDIR_URI, redir);

            if (this.basicSecret) {
                ChallengeResponse authentication = new ChallengeResponse(
                        ChallengeScheme.HTTP_BASIC);
                authentication.setDigestAlgorithm("NONE");
                String basic = this.params.getClientId() + ':'
                        + this.params.getClientSecret();
                authentication.setRawValue(Base64.encode(basic.getBytes(),
                        false));
                tokenResource.setChallengeResponse(authentication);
            } else {
                form.add(OAuthServerResource.CLIENT_ID,
                        this.params.getClientId());
                form.add(OAuthServerResource.CLIENT_SECRET,
                        this.params.getClientSecret());
            }

            form.add(OAuthServerResource.CODE, code);
            getLogger().fine(
                    "Sending access form : " + form.getQueryString() + " to : "
                            + tokenResource.getReference());

            try {
                Representation input = form.getWebRepresentation();
                Representation body = tokenResource.post(input);

                if (tokenResource.getStatus().isSuccess()) {
                    // Store away the user
                    OAuthUser authUser = OAuthUser.createJson(request.getClientInfo().getUser(), body);

                    if (authUser != null) {
                        request.getClientInfo().setUser(authUser);
                        request.getClientInfo().setAuthenticated(true);
                        getLogger().fine(
                                "storing to context = : " + getContext());
                        // continue in the filter chain
                        auth = true;
                    }
                }
                getLogger().fine("Before sns release");
                body.release();
            } catch (ResourceException re) {
                getLogger().warning("Could not find token resource.");
            }
            tokenResource.release();
        }
        if (auth) {
            return CONTINUE;
        } else {
            if (response.getStatus().isSuccess()
                    || response.getStatus().isServerError()) {
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            }
            return STOP;
        }
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        // tokenResource = new CookieCopyClientResource(params.getBaseRef()
        // + params.getAccessTokenPath());
    }

    /*
     * @Override protected int unauthorized(Request request, Response response)
     * { if (response.getStatus().isSuccess() ||
     * response.getStatus().isServerError()) { return
     * super.unauthorized(request, response); }
     * 
     * // If redirect or a specific client error just propaget it on return
     * STOP; }
     */

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        // tokenResource.release();
    }

}
