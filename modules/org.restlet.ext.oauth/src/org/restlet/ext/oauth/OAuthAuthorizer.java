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
import java.util.Arrays;
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
 * Authorizer for OAuth 2.0 protection of REST resources. Can be deployed outside the
 * Authorization Server Restlet application. A Validation resource must be
 * started and mapped in the auth server.
 * 
 * In addition to the normal operation of a RoleAuthorizer the OAuthAuthorizer can authorize
 * roles per request by setting a Request Attribute (defaults to DEFAULT_ROLE_ATTRIBUTE) with
 * a Role[]. If that is present it will authorize those Roles instead of the global roles for
 * the Authorizer.
 * 
 * The OAuthAuthorizer also allows for authorizing against a specific owner - a person who has issued
 * a token. The token provided to the authorizer will then be validated against that owner. The owner
 * is also specified as request attribute (defaults to DEFAULT_OWNER_ATTRIBUTE)
 * 
 * Example invocation:
 * 
 * <pre>
 * {
 *      &#064;code
 *      public Restlet createInboundRoot(){
 *              ...
 *              OAuthAuthorizer auth = new OAuthAuthorizer(
 *              &quot;http://localhost:8080/OAuth2Provider/validate&quot;);
 *              auth.setNext(ProtectedResource.class);
 *              router.attach(&quot;/me&quot;, auth);
 *              ...
 *      }
 *      
 *      //Set up an OAuthAuthorizer for SSL (can be set using global properties as well)
 *      public Restlet createInboundRoot(){
 *              ...
 *              Client client = new Client(Protocol.HTTPS);
 *              Context c = new Context();
 *              client.setContext(c);
 *              c.getParameters().add(&quot;truststorePath&quot;, &quot;pathToKeyStoreFile&quot;);
 *                 c.getParameters(0.add(&quot;truststorePassword&quot;, &quot;password&quot;);
 *              OAuthAuthorizer auth = new OAuthAuthorizer(
 *                      &quot;https://path/to/validate&quot;, client);
 *              ...
 *      }
 *      
 *      //Set up an OAuthAuthorizer that validates owner as well
 *      public Restlet createInboundRoot(){
 *              ...
 *              OAuthAuthorizer auth = new OAuthAuthorizer(
 *              &quot;http://localhost:8080/OAuth2Provider/validate&quot;);
 *              auth.setNext(ProtectedResource.class);
 *              router.attach(&quot;/{&quot;+DEFAULT_OWNER_ATTRIBUTE+&quot;}&quot;, auth);
 *              ...
 *      }
 * }
 * @see org.restlet.ext.oauth.ValidationServerResource
 * 
 * @author Kristoffer Gronowski
 */
public class OAuthAuthorizer extends RoleAuthorizer {

    // Resource authenticateURI;
    // protected final Reference authorizeRef;

    protected final Reference validateRef;

    protected final org.restlet.Client client;
    
    
    public static String DEFAULT_OWNER_ATTRIBUTE = "oauth-user";
    private final String ownerAttribute;
    
    public static String DEFAULT_ROLE_ATTRIBUTE = "oauth-roles";
    private final String roleAttribute;
    

    /**
     * Default constructor.
     */
    protected OAuthAuthorizer() {
        // this.authorizeRef = null;
        this.validateRef = null;
        this.client = null;
        this.ownerAttribute = DEFAULT_OWNER_ATTRIBUTE;
        this.roleAttribute = DEFAULT_ROLE_ATTRIBUTE;
    } // For extending the class

    /**
     * Sets up an OAuthAuthorizer
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     */
    public OAuthAuthorizer(Reference validationRef) {
        this(validationRef, null); // Nothing on errors
    }

    /**
     * Sets up a OAuthAuthorizer.
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     * @param requestClient
     *            A predefined client that will be used for remote client
     *            request. Useful when you need to set e.g. SSL initialization
     *            parameters
     */
    public OAuthAuthorizer(Reference validationRef,
            org.restlet.Client requestClient) {
        this.validateRef = validationRef;
        this.client = requestClient;
        this.ownerAttribute = DEFAULT_OWNER_ATTRIBUTE;
        this.roleAttribute = DEFAULT_ROLE_ATTRIBUTE;
    }

    /**
     * Sets up an OAuthAuthorizer
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     */
    public OAuthAuthorizer(String validationRef) {
        this(new Reference(validationRef));
    }
    
    /**
     * Set up an OAuthAuthorizer.
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     * @param local
     *            If local is set to true "riap://application" will appended to
     *            the validationRef
     * @param requestClient
     *            A predefined client that will be used for remote client
     *            request. Useful when you need to set e.g. SSL initialization
     *            parameters (not needed for e.g. local)
     */
    public OAuthAuthorizer(String validationRef, boolean local,
            org.restlet.Client requestClient) {
        this(validationRef, local, requestClient, null, null);
    }

    /**
     * Set up an OAuthAuthorizer.
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     * @param local
     *            If local is set to true "riap://application" will appended to
     *            the validationRef
     * @param requestClient
     *            A predefined client that will be used for remote client
     *            request. Useful when you need to set e.g. SSL initialization
     *            parameters (not needed for e.g. local)
     * @param ownerAttr
     *            To dynamically authorize against a specific this Authorizer
     *            search for this request attribute. If null it will be set to
     *            DEFAULT_OWNER_ATTRIBUTE
     * @param roleAttr
     *            To dynamically authorize against specific roles this Authorizer
     *            search for this request attribute. If null it will be set to
     *            DEFAULT_ROLE_ATTRIBUTE. Any dynamic roles will have precedence over
     *            default roles for this authorizer
     */
    public OAuthAuthorizer(String validationRef, boolean local,
            org.restlet.Client requestClient, String ownerAttr, String roleAttr) {
        this.client = requestClient;
        if (local) {
            this.validateRef = new Reference("riap://application"
                    + validationRef);
        } else {
            this.validateRef = new Reference(validationRef);
        }
        this.ownerAttribute = ownerAttr != null ? ownerAttr : DEFAULT_OWNER_ATTRIBUTE;
        this.roleAttribute = roleAttr != null ? roleAttr : DEFAULT_ROLE_ATTRIBUTE; 
    }

    /**
     * Set up an OAuthAuthorizer.
     * 
     * @param validationRef
     *            The validation URI referencing the auth server validation
     *            resource.
     * @param requestClient
     *            A predefined client that will be used for remote client
     *            request. Useful when you need to set e.g. SSL initialization
     *            parameters (not needed for e.g. local)
     */
    public OAuthAuthorizer(String validationRef,
            org.restlet.Client requestClient) {
        this(validationRef, false, requestClient, null, null);
    }

    @Override
    public boolean authorize(Request req, Response resp) {
        getLogger().fine("Checking for param access_token");
        String accessToken = getAccessToken(req);

        if ((accessToken == null) || (accessToken.length() == 0)) {
            ChallengeRequest cr = new ChallengeRequest(
                    ChallengeScheme.HTTP_OAUTH, "oauth"); // TODO set realm
            Series<Parameter> parameters = new Form();
            parameters.add("error", OAuthError.invalid_request.name());
            cr.setParameters(parameters);
            resp.getChallengeRequests().add(cr);
            resp.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        } else {
            getLogger().fine("Found Access Token " + accessToken);
            ClientResource authResource = new CookieCopyClientResource(
                    this.validateRef);
            if (this.client != null) {
                authResource.setNext(this.client);
            }

            JSONObject request;
            try {
                request = createValidationRequest(accessToken, req);
                // Representation repr = new JsonRepresentation(request);
                Representation repr = new JsonStringRepresentation(request);
                getLogger().fine("Posting to validator... json = " + request);
                // RETRIEVE JSON...WORKAROUND TO HANDLE ANDROID
                Representation r = authResource.post(repr);
                getLogger().fine("After posting to validator...");
                repr.release();
                getLogger().fine(
                        "Got Respose from auth resource OK "
                                + r.getClass().getCanonicalName());
                JsonRepresentation returned = new JsonRepresentation(r);

                // GET OBJECT
                JSONObject response = returned.getJsonObject();
                boolean authenticated = response.getBoolean("authenticated");

                if (response.has("tokenOwner")) {
                    setUser(req, response, accessToken);
                }

                String error = null;
                if (response.has("error")) {
                    error = response.getString("error");
                }
                // XXX: What should we do when the error is null ?

                getLogger().fine("In Auth Filer -> " + authenticated);

                // Clean-up
                returned.release();
                r.release();
                authResource.release();

                if (authenticated) {
                    return true;
                }

                // handle any errors:
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
    
    

    // GET SIZE TO HANDLE BUG IN GLASSFISH
    /*
     * private Representation createJsonRepresentation(JSONObject request){
     * JsonRepresentation repr = new JsonRepresentation(request);
     * StringRepresentation sr = new StringRepresentation( request.toString());
     * sr.setCharacterSet(repr.getCharacterSet()); repr.setSize(sr.getSize());
     * sr.release(); return repr; }
     */

    /**
     * Returns an instance of {@link JSONObject} that represents the validation
     * request.
     * 
     * @param accessToken
     *            The access token.
     * @param req
     *            The current request.
     * @return An instance of {@link JSONObject} that represents the validation
     *         request.
     * @throws JSONException
     */
    private JSONObject createValidationRequest(String accessToken, Request req)
            throws JSONException {
        JSONObject request = new JSONObject();
        Reference uri = req.getOriginalRef();

        request.put("access_token", accessToken);

        // add any roles...
        List<Role> roles = getAuthorizedRoles();
        
        //now check if any dynamic roles has been set:
        Object obj = req.getAttributes().get(this.roleAttribute);
        if(obj != null && obj instanceof Role[]){
            getLogger().fine("Found dynamic scopes");
            roles = Arrays.asList((Role[])obj);
        }
        if ((roles != null) && (roles.size() > 0)) {
            JSONArray jArray = new JSONArray();
            for (Role r : roles) {
                jArray.put(Scopes.toScope(r));
            }
            request.put("scope", jArray);
            getLogger().fine("Found scopes: " + jArray.toString());
        }
        String owner = (String) req.getAttributes().get(this.ownerAttribute);
        if ((owner != null) && (owner.length() > 0)) {
            getLogger().fine("Found Owner:" + owner);
            request.put("owner", owner);
        }
        request.put("uri", uri.getHierarchicalPart());
        return request;
    }

    /**
     * Returns the access token taken from a given request.
     * 
     * @param request
     *            The request.
     * @return The access token taken from a given request.
     */
    private String getAccessToken(Request request) {
        // Auth Header present
        String accessToken = null;
        if (request.getChallengeResponse() != null) {
            accessToken = request.getChallengeResponse().getRawValue();
            getLogger().fine("Found Authorization header" + accessToken);
        }
        // Check query for token
        else if ((accessToken == null) || (accessToken.length() == 0)) {
            getLogger().fine(
                    "Didn't contain a Authorization header - checking query");
            accessToken = request.getOriginalRef().getQueryAsForm()
                    .getFirstValue(OAuthServerResource.OAUTH_TOKEN);

            // check body if all else fail:
            if ((accessToken == null) || (accessToken.length() == 0)) {
                if ((request.getMethod() == Method.POST)
                        || (request.getMethod() == Method.PUT)
                        || (request.getMethod() == Method.DELETE)) {
                    Representation r = request.getEntity();
                    if ((r != null)
                            && MediaType.APPLICATION_WWW_FORM.equals(r
                                    .getMediaType())) {
                        // Search for an OAuth Token
                        Form form = new Form(r);
                        accessToken = form
                                .getFirstValue(OAuthServerResource.OAUTH_TOKEN);
                        if ((accessToken != null) && (accessToken.length() > 0)) {
                            // restore the entity body
                            request.setEntity(form.getWebRepresentation());
                        }
                    }
                }
            }
        }
        return accessToken;
    }

    /**
     * Completes the given {@link Response} according to the given error.
     * 
     * @param error
     *            The error.
     * @param resp
     *            The response to complete.
     */
    private void handleError(String error, Response resp) {
        if ((error != null) && (error.length() > 0)) {
            ChallengeRequest cr = new ChallengeRequest(
                    ChallengeScheme.HTTP_OAUTH, "oauth"); // TODO set
            // realm
            Series<Parameter> parameters = new Form();
            parameters.add("error", error);
            OAuthError code = OAuthError.valueOf(error);

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
    }

    /**
     * Instantiates the {@link User} according to the given credentials and
     * update the given {@link Request}.
     * 
     * @param req
     *            The request.
     * @param response
     *            The {@link JSONObject} that represents the response.
     * @param accessToken
     *            The access token.
     * @throws JSONException
     */
    private void setUser(Request req, JSONObject response, String accessToken)
            throws JSONException {
        String tokenOwner = response.getString("tokenOwner");
        getLogger().fine(
                "User " + tokenOwner + " is accessing : "
                        + req.getOriginalRef());
        User user = new User(tokenOwner, accessToken);

        // Set the user so that the Resource knows who is executing
        // Based on the person issuing the token in the first place.
        req.getClientInfo().setUser(user);
        req.getClientInfo().setAuthenticated(true);
    }

    @Override
    protected int unauthorized(Request request, Response response) {
        // TODO Implement this:
        /*
         * if (response.getStatus().isSuccess() ||
         * response.getStatus().isServerError()) { return
         * super.unauthorized(request, response); }
         * 
         * // If redirect or a specific client error just propaget it on return
         * STOP;
         */
        return STOP;
    }
}
