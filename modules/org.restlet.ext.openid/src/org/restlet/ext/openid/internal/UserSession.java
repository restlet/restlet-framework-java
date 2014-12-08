/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.openid.internal;

import org.openid4java.message.ParameterList;

/**
 * 
 * @author Martin Svensson
 */
public class UserSession {

    private volatile ParameterList pl;

    private volatile OpenIdUser user;

    public UserSession(ParameterList pl) {
        this.pl = pl;
    }

    public ParameterList getParameterList() {
        return pl;
    }

    public OpenIdUser getUser() {
        return user;
    }

    public void setParameterList(ParameterList pl) {
        this.pl = pl;
    }

    public void setUser(OpenIdUser user) {
        this.user = user;
    }

}
