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

package org.restlet.example.ext.oauth;

import java.util.logging.Logger;


import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthProxy;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Helper client class for accessing Facebook Graph API. It can request for
 * specific scopes for a developer key and secret.
 * 
 * <pre>
 * {
 * &#064;code
 * FacebookProxy proxy = new FacebookProxy(facebookClientId, facebookClientSecret 
 *                                          scope, getContext());
 * proxy.setNext(DummyResource.class);
 * router.attach("/protected", proxy);
 * }
 * </pre>
 * 
 * @author Kristoffer Gronowski
 * @see <a href="http://developers.facebook.com/docs/api">Facebook APIs</a>
 */
public class FacebookProxy extends OAuthProxy {

    public static final String FB_GRAPH = "https://graph.facebook.com/";


    

    /**
     * Create Proxy to facebook authentication
     * 
     * @param clientId
     *            facebook clientID
     * @param clientSecret
     *            facebook clientSecret
     * @param scope
     *            requested scope
     * @param ctx
     *            Restlet scope
     */
    public FacebookProxy(String clientId, String clientSecret, String scope, Context ctx) {
        super(new OAuthParameters(clientId, clientSecret, FB_GRAPH + "oauth/",
                Scopes.toRoles(scope)), ctx);
    }
    
    @Override
    protected int beforeHandle(Request request, Response response) {
        int cont = super.beforeHandle(request, response);
        if(cont == CONTINUE){ //successfull...just set the fb user id
            FBUser fbu = null;
            getLogger().info("FBProxy retrieving FACEBOOK ID");
          try {
              fbu = getMe(getToken(request));
          } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
            request.getClientInfo().getUser().setIdentifier(fbu.getId());
        }
        return cont;
    }
    
    public static String getToken(Request r) throws Exception{
        org.restlet.security.User u = r.getClientInfo().getUser();
        return ((OAuthUser) u).getAccessToken();
      }
    
    public static FBUser getMe(String accessToken) throws Exception{
        Logger.getLogger(FacebookProxy.class.getName()).info("Retrieving me object");
        Reference feedRef = new Reference("https://graph.facebook.com/me");
        feedRef.addQueryParameter("access_token", accessToken);
        
        ClientResource cr = new ClientResource(feedRef);
        Representation rep = cr.get();
        if(cr.getStatus().isSuccess()){
          JsonRepresentation jrep = new JsonRepresentation(rep);
          FBUser fbu = new FBUser(jrep.getJsonObject());
          cr.release();
          return fbu;
        }
        cr.release();
        return null;
      }

}
