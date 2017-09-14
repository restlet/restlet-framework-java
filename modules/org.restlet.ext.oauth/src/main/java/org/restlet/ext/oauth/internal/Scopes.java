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

package org.restlet.ext.oauth.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.restlet.security.Role;

/**
 * Utility methods for converting between OAuth Scopes and Restlet Roles
 * 
 * @author Martin Svensson
 */
public class Scopes {

    public static boolean isIdentical(String[] a, String[] b) {
        List<String> al = Arrays.asList(a);
        List<String> bl = Arrays.asList(b);
        return al.containsAll(bl) && bl.containsAll(al);
    }

    public static String[] parseScope(List<Role> roles) {
        String[] scopes = new String[roles.size()];
        for (int i = 0; i < roles.size(); i++) {
            scopes[i] = roles.get(i).getName();
        }
        return scopes;
    }

    public static String[] parseScope(String scopes) {
        if (scopes != null && scopes.length() > 0) {
            StringTokenizer st = new StringTokenizer(scopes, " ");
            String[] scope = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++)
                scope[i] = st.nextToken();
            return scope;
        }
        return new String[0];
    }

    @SuppressWarnings("deprecation")
    public static Role toRole(String scope) {
        return new Role(scope, null);
    }

    @SuppressWarnings("deprecation")
    public static List<Role> toRoles(String scopes) {
        String[] tmp = parseScope(scopes);
        List<Role> toRet = new ArrayList<Role>(tmp.length);

        for (String scope : tmp) {
            toRet.add(new Role(scope, null));
        }

        return toRet;
    }

    public static String toScope(List<Role> roles)
            throws IllegalArgumentException {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Role r : roles) {
            String scope = toScope(r);
            sb.append(' ');
            sb.append(scope);

        }
        return sb.substring(1);
    }

    public static String toScope(Role r) throws IllegalArgumentException {
        String rname = r.getName();
        if (rname == null)
            throw new IllegalArgumentException("Role name cannot be null");
        rname = rname.trim();
        if (rname.length() < 1)
            throw new IllegalArgumentException("Role name cannot be empty");
        else if (rname.contains(" "))
            throw new IllegalArgumentException("Role name cannot contain space");
        return rname;
    }

    public static String toString(String[] scopes) {
        StringBuilder sb = new StringBuilder();
        for (String scope : scopes) {
            sb.append(' ');
            sb.append(scope);
        }
        return sb.substring(1);
    }
}
