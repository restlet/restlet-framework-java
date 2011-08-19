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

package org.restlet.ext.openid.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;



import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.ServerManager;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.ext.openid.AX;
import org.restlet.ext.openid.internal.OPReturn.OPR;

/**
 * @author esvmart
 *
 */
public class OP {
    
    public static final String OPENID_MODE = "openid.mode";
    
    public enum OpenIdMode{
        associate, checkid_setup, checkid_immediate,
        check_authentication, errorMode;
    }
    
    private final Map <String, UserSession> sessions = new HashMap <String, UserSession> ();
    
    
    public UserSession getSession(String sessionId){
        return this.sessions.get(sessionId);
    }
    
    public OPReturn processOPRequest(ServerManager sm, ParameterList pl, 
            Request req, Response res, UserSession us){
        String modeParam = null;// pl.getParameterValue(OPENID_MODE);
        OpenIdMode mode = null;
        Message response;
             
        if(pl == null && us != null){
            pl = us.pl;
            
        }
        mode = OpenIdMode.valueOf(pl.getParameterValue(OPENID_MODE));
        
        try{
          
        }
        catch(Exception e){
            Engine.getAnonymousLogger().warning("No Known openid.mode: "+modeParam);
            mode = OpenIdMode.errorMode;
        }
        Engine.getAnonymousLogger().info("processRequest: "+mode);
        switch(mode){
        case associate:
            response = sm.associationResponse(pl);
            return new OPReturn(OPR.OK, response.keyValueFormEncoding());
        case checkid_setup:
        case checkid_immediate:
            if(us == null || us.user == null){ //this means no authorization has taken place yet
                String session = UUID.randomUUID().toString();
                this.sessions.put(session, new UserSession(pl));
                return new OPReturn(OPR.GET_USER, session);
            }
            OpenIdUser user = us.user;
            response = sm.authResponse(pl, user.getClaimedId(), user.getClaimedId(),
                    user.getApproved());
            //add any attributes:

            if(response instanceof DirectError){
                return new OPReturn(OPR.OK, response.keyValueFormEncoding());
            }
            if(us.user.attributes() != null && us.user.attributes().size() > 0){
                FetchResponse fr = null;
                fr = FetchResponse.createFetchResponse();
                for(AX attr: us.user.attributes()){
                    String val = us.user.getAXValue(attr);
                    if(val != null){
                        try {
                            fr.addAttribute(attr.getName(), 
                                    attr.getSchema(), val);
                        } catch (MessageException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                if(fr.getAttributes().size() > 0){
                    try {
                        response.addExtension(fr);
                        sm.sign((AuthSuccess) response);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
            }
            res.redirectSeeOther(response.getDestinationUrl(true));
            return new OPReturn(OPR.OK, "");
        case check_authentication:
            response = sm.verify(pl);
            return new OPReturn(OPR.OK, response.keyValueFormEncoding());
        case errorMode:
            response = DirectError.createDirectError("Unknown request");
            return new OPReturn(OPR.OK, response.keyValueFormEncoding());
        }
        return null;
    }
    
    public Message fetchAttributes(ParameterList pl) throws Exception{
        Message m = Message.createMessage(pl);
        if(m.hasExtension(AxMessage.OPENID_NS_AX)){
            return m;
        }
        return null;   
    }
    
    public Message fetchAttributes(UserSession us) throws Exception{
        return fetchAttributes(us.pl);
    }
    
    @SuppressWarnings("rawtypes")
    public Map getRequiredAttributes(Message m) throws Exception{
        FetchRequest req = (FetchRequest) m.getExtension(AxMessage.OPENID_NS_AX);
        return req.getAttributes(true);
    }
    
    @SuppressWarnings("rawtypes")
    public Map getOptionalAttributes(Message m) throws Exception{
        FetchRequest req = (FetchRequest) m.getExtension(AxMessage.OPENID_NS_AX);
        return req.getAttributes(false);
    }
    
    public Logger getLogger() {
        Logger result = null;

        Context context = Context.getCurrent();

        if (context != null) {
            result = context.getLogger();
        }

        if (result == null) {
            result = Engine.getLogger(this, "org.restlet.ext.openid.OP");
        }

        return result;
    }

}
