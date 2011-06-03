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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
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
import org.restlet.ext.oauth.internal.JsonStringRepresentation;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;
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
 * @see org.restlet.ext.oauth.ValidationServerResource
 * 
 * @author Kristoffer Gronowski
 */
public class OAuthAuthorizer extends RoleAuthorizer {

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


    private JSONObject createValidationRequest(String accessToken, Request req) throws JSONException{
        JSONObject request = new JSONObject();
        Reference uri = req.getOriginalRef();

        request.put("access_token", accessToken);

        //add any roles...
        List <Role> roles = this.getAuthorizedRoles();
        if (roles != null && roles.size() > 0) {
            JSONArray jArray = new JSONArray();
            for (Role r : roles)
                jArray.put(Scopes.toScope(r));
            request.put("scope", jArray);
            getLogger().info("Found scopes: "+jArray.toString());
        }
        String owner = (String) req.getAttributes().get("oauth-user");
        if(owner != null && owner.length() > 0){
            getLogger().info("Found Owner:"+owner);
            request.put("owner", owner);
        }
        request.put("uri", uri.getHierarchicalPart());
        return request;
    }

    // GET SIZE TO HANDLE BUG IN GLASSFISH
    /*private Representation createJsonRepresentation(JSONObject request){    
        JsonRepresentation repr = new JsonRepresentation(request);
        StringRepresentation sr = new StringRepresentation(
                request.toString());
        sr.setCharacterSet(repr.getCharacterSet());
        repr.setSize(sr.getSize());
        sr.release();
        return repr;
    }*/

    private void setUser(Request req, JSONObject response, String accessToken) throws JSONException{
        String tokenOwner = response.getString("tokenOwner");
        getLogger().info(
                "User " + tokenOwner + " is accessing : "
                + req.getOriginalRef());
        User user = new User(tokenOwner, accessToken);

        // Set the user so that the Resource knows who is executing
        // Based on the person issuing the token in the first place.
        req.getClientInfo().setUser(user);
        req.getClientInfo().setAuthenticated(true);
    }

    private void handleError(String error, Response resp){
        if (error != null && error.length() > 0) {
            ChallengeRequest cr = new ChallengeRequest(
                    ChallengeScheme.HTTP_OAUTH, "oauth"); // TODO set
            // realm
            Series<Parameter> parameters = new Form();
            parameters.add("error", error);
            OAuthError code = OAuthError.valueOf(error);

            switch (code) {
            case INVALID_REQUEST:
                // TODO report bug in Restlet and verify, can not handle
                // space char.
                // parameters.add("error_description",
                // "The request is missing a required parameter.");
                resp.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                break;
            case INVALID_TOKEN:
            case EXPIRED_TOKEN:
                // parameters.add("error_description",
                // "The access token provided is invalid.");
                resp.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                break;
            case INSUFFICIENT_SCOPE:
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
    }
    
    private String getAccessToken(Request req){
        //Auth Header present
        String accessToken = null;
        if(req.getChallengeResponse() != null){
            accessToken = req.getChallengeResponse().getRawValue();
            getLogger().info("Found Authorization header" + accessToken);
        }
        //Check query for token
        else if(accessToken == null || accessToken.length() == 0){
            getLogger().info("Didn't contain a Authorization header - checking query");
            accessToken = req.getOriginalRef().getQueryAsForm()
            .getFirstValue(OAuthServerResource.OAUTH_TOKEN);

            //check body if all else fail:
            if(accessToken == null || accessToken.length() == 0) {
                if(req.getMethod() == Method.POST
                        || req.getMethod() == Method.PUT
                        || req.getMethod() == Method.DELETE) {
                    Representation r = req.getEntity();
                    if(r != null && MediaType.APPLICATION_WWW_FORM.equals(r
                            .getMediaType())) {
                        // Search for an OAuth Token
                        Form form = new Form(r);
                        accessToken = form.getFirstValue(OAuthServerResource.OAUTH_TOKEN);
                        if (accessToken != null && accessToken.length() > 0) {
                            // restore the entity body
                            req.setEntity(form.getWebRepresentation());
                        }
                    }
                }
            }
        }
        return accessToken;
    }

    @Override
    public boolean authorize(Request req, Response resp){
        getLogger().info("Checking for param access_token");
        String accessToken = getAccessToken(req);

        if (accessToken == null || accessToken.length() == 0) {
            ChallengeRequest cr = new ChallengeRequest(
                    ChallengeScheme.HTTP_OAUTH, "oauth"); // TODO set realm
            Series<Parameter> parameters = new Form();
            parameters.add("error", OAuthError.INVALID_REQUEST.name());
            cr.setParameters(parameters);
            resp.getChallengeRequests().add(cr);
            resp.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        else {
            getLogger().info("Found Access Token " + accessToken);
            ClientResource authResource = new CookieCopyClientResource(
                    validateRef);

            JSONObject request;
            try {
                request = createValidationRequest(accessToken, req);
                //Representation repr = this.createJsonRepresentation(request);
                Representation repr = new JsonStringRepresentation(request);
                getLogger().info("Posting to validator... json = " + request);
                // RETRIEVE JSON...WORKAROUND TO HANDLE ANDROID
                Representation r = authResource.post(repr);
                getLogger().info("After posting to validator...");
                repr.release();
                getLogger().info("Got Respose from auth resource OK "
                        + r.getClass().getCanonicalName());
                JsonRepresentation returned = new JsonRepresentation(r);

                // GET OBJECT
                JSONObject response = returned.getJsonObject();
                boolean authenticated = response.getBoolean("authenticated");

                if (response.has("tokenOwner"))
                    this.setUser(req, response, accessToken);


                String error = null;
                if (response.has("error"))
                    error = response.getString("error");

                getLogger().info("In Auth Filer -> " + authenticated);

                // Clean-up
                returned.release();
                r.release();
                authResource.release();

                if(authenticated) return true;

                //handle any errors:
                handleError(error, resp);

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