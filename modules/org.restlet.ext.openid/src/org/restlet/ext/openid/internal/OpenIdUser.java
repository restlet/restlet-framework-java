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

package org.restlet.ext.openid.internal;

import java.util.Set;

import org.restlet.ext.openid.AttributeExchange;
import org.restlet.security.User;

/**
 * Authenticated user with OpenID specific properties.
 * 
 * @author Martin Svensson
 */
public class OpenIdUser extends User {

    public static void setValueFromAX(AttributeExchange attribute, String value, User u) {
        if (attribute == AttributeExchange.EMAIL)
            u.setEmail(value);
        else if (attribute == AttributeExchange.FIRST_NAME)
            u.setFirstName(value);
        else if (attribute == AttributeExchange.LAST_NAME)
            u.setLastName(value);
    }

    private volatile boolean approved;

    private volatile Set<AttributeExchange> attributes;

    private volatile String claimedId;

    public OpenIdUser() {
        super();
    }

    public OpenIdUser(String identifier) {
        super(identifier);
    }

    public OpenIdUser(String identifier, char[] secret) {
        super(identifier, secret);
    }

    public OpenIdUser(String identifier, char[] secret, String firstName,
            String lastName, String email) {
        super(identifier, secret, firstName, lastName, email);
    }

    public OpenIdUser(String identifier, String secret) {
        super(identifier, secret);
    }

    public OpenIdUser(String identifier, String secret, String firstName,
            String lastName, String email) {
        super(identifier, secret, firstName, lastName, email);
    }

    public Set<AttributeExchange> attributes() {
        return this.attributes;
    }

    public boolean getApproved() {
        return this.approved;
    }

    public String getAXValue(AttributeExchange attribute) {
        if (attribute == AttributeExchange.FULL_NAME)
            return this.getName();
        else if (attribute == AttributeExchange.EMAIL)
            return this.getEmail();
        else if (attribute == AttributeExchange.FIRST_NAME)
            return this.getFirstName();
        else if (attribute == AttributeExchange.LAST_NAME)
            return this.getLastName();
        return null;
    }

    public String getClaimedId() {
        return this.claimedId;
    }

    public boolean setApproved(boolean approved) {
        return this.approved = approved;
    }

    public void setAttributes(Set<AttributeExchange> attributes) {
        this.attributes = attributes;
    }

    public void setClaimedId(String claimedId) {
        this.claimedId = claimedId;
    }

    public void setValueFromAX(AttributeExchange attribute, String value) {
        setValueFromAX(attribute, value, this);
    }

}
