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

import java.util.Set;

import org.restlet.ext.openid.AX;
import org.restlet.security.User;

/**
 * @author esvmart
 *
 */
public class OpenIdUser extends User {
    
    private volatile String claimedId;
    private volatile boolean approved;
    private volatile Set <AX> attributes;
    
    public OpenIdUser() {
        super();
        // TODO Auto-generated constructor stub
    }

    public OpenIdUser(String identifier, char[] secret, String firstName,
            String lastName, String email) {
        super(identifier, secret, firstName, lastName, email);
        // TODO Auto-generated constructor stub
    }

    public OpenIdUser(String identifier, char[] secret) {
        super(identifier, secret);
        // TODO Auto-generated constructor stub
    }

    public OpenIdUser(String identifier, String secret, String firstName,
            String lastName, String email) {
        super(identifier, secret, firstName, lastName, email);
        // TODO Auto-generated constructor stub
    }

    public OpenIdUser(String identifier, String secret) {
        super(identifier, secret);
        // TODO Auto-generated constructor stub
    }

    public OpenIdUser(String identifier) {
        super(identifier);
        // TODO Auto-generated constructor stub
    }
    
    public void setClaimedId(String claimedId){
        this.claimedId = claimedId;
    }
    
    public String getClaimedId(){
        return this.claimedId;
    }
    
    public boolean setApproved(boolean approved){
        return this.approved = approved;
    }
    
    public boolean getApproved(){
        return this.approved;
    }
    
    public void setAttributes(Set <AX> attributes){
        this.attributes = attributes;
    }
    
    public Set <AX> attributes(){
        return this.attributes;
    }
    
    public static void setValueFromAX(AX attribute, String value, User u){
        if(attribute == AX.EMAIL)
            u.setEmail(value);
        else if(attribute == AX.FIRST_NAME)
            u.setFirstName(value);
        else if(attribute == AX.LAST_NAME)
            u.setLastName(value);
    }
    
    public void setValueFromAX(AX attribute, String value){
        setValueFromAX(attribute, value, this);
    }
    
    public String getAXValue(AX attribute){
        if(attribute == AX.FULL_NAME)
            return this.getName();
        else if(attribute == AX.EMAIL)
            return this.getEmail();
        else if(attribute == AX.FIRST_NAME)
            return this.getFirstName();
        else if(attribute == AX.LAST_NAME)
            return this.getLastName();
        return null;
    }
    

    
    
    

}
