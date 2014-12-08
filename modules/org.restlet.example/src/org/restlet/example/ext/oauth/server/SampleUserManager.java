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

package org.restlet.example.ext.oauth.server;

import java.util.HashSet;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class SampleUserManager {

    private HashSet<SampleUser> userSet;

    public SampleUserManager() {
        userSet = new HashSet<SampleUser>();
    }

    public SampleUser addUser(String id) {
        SampleUser user = new SampleUser(id);
        if (!userSet.contains(user)) {
            userSet.add(user);
            return user;
        }
        return null;
    }

    public SampleUser findUserById(String id) {
        for (SampleUser user : userSet) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }
}
