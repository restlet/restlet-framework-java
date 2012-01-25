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

package org.restlet.example.ext.oauth.experimental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Method;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.security.Role;

/**
 * EXPERIMENTAL, and not part of the OAuth specification Implementation might
 * change in future releases.
 * 
 * @author Kristoffer Gronowski
 * 
 */
public class DiscoverableEndpointInfo {

    public enum ScopeSet { // Describe if the scope set is mutable or not
        fixed, dynamic
    }

    private List<Role> roles = new ArrayList <Role> ();

    @SuppressWarnings("unchecked")
    private Set<Method> methods = Collections.EMPTY_SET;

    private String owner;

    private String scopeSet = ScopeSet.fixed.name();

    private String wadl;

    public DiscoverableEndpointInfo(Set<Method> methods) {
        this.methods = methods;
    }

    public void setScopes(List <Role> r) {
        this.roles = r;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setScopeSet(ScopeSet mutation) {
        this.scopeSet = mutation.name();
    }

    public void setWadlUrl(String url) {
        wadl = url;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        JSONArray a = new JSONArray();
        for (Role r : roles) {
            a.put(Scopes.toScope(r));
        }
        json.put("scopes", a);

        a = new JSONArray();
        for (Method m : methods) {
            a.put(m.getName());
        }
        json.put("methods", a);

        if (owner != null && owner.length() > 0) {
            json.put("owner", owner);
        }

        json.put("scope_set", scopeSet);

        if (wadl != null && wadl.length() > 0) {
            json.put("wadl_url", wadl);
        }

        return json;
    }
}
