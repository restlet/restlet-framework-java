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
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.CookieCopyClientResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.User;
import org.restlet.util.Series;

/**
 * Class for OAuth 2.0 protection of REST resources. Can be deployed outside the
 * Authorization Server Restlet application. A Validation resource must be
 * started and mapped in the auth server.
 * 
 * Example invocation:
 * 
 * <pre>
 * {@code
 * public Restlet createInboundRoot(){
 *   ...
 *   OAuthAuthorizer auth = new OAuthAuthorizer(
 *              "http://localhost:8080/OAuth2Provider/validate",
 *              "http://localhost:8080/OAuth2Provider/authorize");
 *   auth.setNext(ProtectedResource.class);
 *   router.attach("/me", auth);
 *   ...
 * }
 * }
 * @see ScopedResource
 * @see org.restlet.ext.oauth.ValidationServerResource
 * 
 * @author Kristoffer Gronowski
 */
public class OAuthAuthorizer extends Authorizer {

    // Resource authenticateURI;
    protected final Reference authorizeRef;

    protected final Reference validateRef;

    /**
     * Constructor.
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     * @param authorizationRef
     *            The authorization URI referencing that should be invoked on
     *            errors.
     */
    public OAuthAuthorizer(String validationRef, String authorizationRef) {
        this(validationRef, authorizationRef, false);
    }

    /**
     * Set up a Remote or Local Authorizer. Can only be deployed together with
     * the Authorization Server Restlet application. A Validation resource must
     * be started and mapped in the auth server.
     * 
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     * @param authorizationRef
     *            The authorization URI referencing that should be invoked on
     *            errors.
     * @param local
     *            if true a local authorizer will be created
     */
    public OAuthAuthorizer(String validationRef, String authorizationRef,
            boolean local) {
        if (local) {
            authorizeRef = new Reference(authorizationRef);
            validateRef = new Reference("riap://application" + validationRef);
        } else {
            authorizeRef = new Reference(authorizationRef);
            validateRef = new Reference(validationRef);
        }
    }

    /**
     * Set up a RemoteAuthorizer
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     * @param authorizationRef
     *            The authorization URI referencing that should be invoked on
     *            errors.
     */
    public OAuthAuthorizer(Reference validationRef, Reference authorizationRef) {
        authorizeRef = authorizationRef;
        validateRef = validationRef;
    }

    /**
     * Constructor.
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     */
    public OAuthAuthorizer(Reference validationRef) {
        this(validationRef, null); // Nothing on errors
    }

    /**
     * Default constructor.
     */
    protected OAuthAuthorizer() {
        this.authorizeRef = null;
        this.validateRef = null;
    } // For extending the class

    @Override
    protected boolean authorize(Request req, Response resp) {
        Logger log = Context.getCurrentLogger();
        log.info("Checking for param access_token");

        String accessToken = null;
        if (req.getChallengeResponse() != null) {
            // There is a Authorization header
            accessToken = req.getChallengeResponse().getRawValue();
            getLogger().info("Found Authorization header" + accessToken);
        }
        // check the query for token
        else if (accessToken == null || accessToken.length() == 0) {
            log.info("Didn't contain a Authorization header - checking query");
            accessToken = req.getOriginalRef().getQueryAsForm()
                    .getFirstValue(OAuthServerResource.OAUTH_TOKEN);
            // Last chance, checking body
            if (accessToken == null || accessToken.length() == 0) {
                if (req.getMethod() == Method.POST
                        || req.getMethod() == Method.PUT
                        || req.getMethod() == Method.DELETE) {
                    Representation r = req.getEntity();
                    if (r != null
                            && MediaType.APPLICATION_WWW_FORM.equals(r
                                    .getMediaType())) {
                        // Search for a oauth Token
                        Form form = new Form(r);
                        accessToken = form
                                .getFirstValue(OAuthServerResource.OAUTH_TOKEN);
                        if (accessToken != null && accessToken.length() > 0) {
                            // restore the entity body
                            req.setEntity(form.getWebRepresentation());
                        }
                    }
                }
            }
        }
        if (accessToken == null || accessToken.length() == 0) {
            ChallengeRequest cr = new ChallengeRequest(
                    ChallengeScheme.HTTP_OAUTH, "oauth"); // TODO set realm
            Series<Parameter> parameters = new Form();
            parameters
                    .add("error", OAuthError.ErrorCode.invalid_request.name());
            cr.setParameters(parameters);
            resp.getChallengeRequests().add(cr);
            resp.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        } else {
            getLogger().info("Found Access Token " + accessToken);
            ClientResource authResource = new CookieCopyClientResource(
                    validateRef);
            JSONObject request = new JSONObject();
            try {
                Reference uri = req.getOriginalRef();
                request.put("access_token", accessToken);

                // TODO it would be better to do this once not on every request
                ScopedResource scoped = null;
                log.info("Looking for a scoped resource");
                for (Restlet next = getNext(); next != null;) {
                    if (next instanceof Finder) {
                        Finder f = (Finder) next;
                        ServerResource sr = f.find(req, resp);
                        if (sr instanceof ScopedResource) {
                            scoped = (ScopedResource) sr;
                        }
                        break;
                        // next = ((Filter)next).getNext();
                    } else if (next instanceof Filter) {
                        next = ((Filter) next).getNext();
                    } else if (next instanceof Router) {
                        next = ((Router) next).getNext(req, resp);
                    } else {
                        getLogger().warning(
                                "Unsupported class found in loop : "
                                        + next.getClass().getCanonicalName());
                        break;
                    }
                }
                log.info("After scoped resource - " + scoped);
                if (scoped != null) {
                    String owner = scoped.getOwner(uri);
                    if (owner != null && owner.length() > 0)
                        request.put("owner", owner);
                    log.info("Found owner = " + owner);
                    // More job here but easier for the developer to use []
                    String[] scopes = scoped.getScope(uri, req.getMethod());
                    log.info("Found scopes = " + scopes);
                    if (scopes != null && scopes.length > 0) {
                        JSONArray jArray = new JSONArray();
                        for (String scope : scopes)
                            jArray.put(scope);
                        request.put("scope", jArray);
                    }
                }
                request.put("uri", uri.getHierarchicalPart());
                // GET SIZE TO HANDLE BUG IN GLASSFISH
                JsonRepresentation repr = new JsonRepresentation(request);
                StringRepresentation sr = new StringRepresentation(
                        request.toString());
                sr.setCharacterSet(repr.getCharacterSet());
                repr.setSize(sr.getSize());

                log.info("Posting to validator... json = " + request);
                // RETRIEVE JSON...WORKAROUND TO HANDLE ANDROID
                Representation r = authResource.post(repr);
                log.info("After posting to validator...");
                repr.release();
                sr.release();

                getLogger().info(
                        "Got Respose from auth resource OK "
                                + r.getClass().getCanonicalName());
                JsonRepresentation returned = new JsonRepresentation(r);

                // GET OBJECT
                JSONObject response = returned.getJsonObject();
                boolean authenticated = response.getBoolean("authenticated");
                if (response.has("tokenOwner")) {
                    String tokenOwner = response.getString("tokenOwner");
                    log.info("User " + tokenOwner + " is accessing : "
                            + req.getOriginalRef());
                    User user = new User(tokenOwner, accessToken);
                    // Set the user so that the Resource knows who is executing
                    // Based on the person issuing the token in the first place.
                    req.getClientInfo().setUser(user);
                    req.getClientInfo().setAuthenticated(true);
                }
                String error = null;
                if (response.has("error")) {
                    error = response.getString("error");
                }
                getLogger().info("In Auth Filer -> " + authenticated);

                // Clean-up
                returned.release();
                r.release();
                authResource.release();

                if (authenticated)
                    return true;
                else if (error != null && error.length() > 0) {
                    ChallengeRequest cr = new ChallengeRequest(
                            ChallengeScheme.HTTP_OAUTH, "oauth"); // TODO set
                    // realm
                    Series<Parameter> parameters = new Form();
                    parameters.add("error", error);

                    OAuthError.ErrorCode code = OAuthError.ErrorCode
                            .valueOf(error);
                    switch (code) {
                    case invalid_request:
                        // TODO report bug in Restlet and verify, can not handle
                        // space char.
                        // parameters.add("error_description",
                        // "The request is missing a required parameter.");
                        resp.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        break;
                    case invalid_token:
                    case expired_token:
                        // parameters.add("error_description",
                        // "The access token provided is invalid.");
                        resp.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        break;
                    case insufficient_scope:
                        // parameters.add("error_description",
                        // "The request requires higher privileges than provided "
                        // +"by the access token.");
                        resp.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                        break;

                    }
                    // parameters.add("error_uri",authorizeRef.toString());
                    cr.setParameters(parameters);
                    resp.getChallengeRequests().add(cr);
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (authResource != null) {
                    authResource.getResponse().release();
                    authResource.release();
                }
            }
        }
        return false;
    }

    @Override
    protected int unauthorized(Request request, Response response) {
        return STOP;
        // TODO propose to add if statement to Authenticator.
        // if( response.getStatus().isRedirection()) return STOP;
        // return super.unauthorized(request, response);
    }
}