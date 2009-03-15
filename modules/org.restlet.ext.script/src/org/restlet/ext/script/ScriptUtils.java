/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.script;

import org.restlet.data.Request;

/**
 * Utility methods for the script extension.
 * 
 * @author Tal Liron
 */
public abstract class ScriptUtils {
    /**
     * Retrieves that part of the resource reference between the base reference
     * and the optional attributes.
     * 
     * @param request
     *            The request or null
     * @param def
     *            A default value to return if the request is null or if the
     *            relative part of the reference is empty
     * @return The relative part of the reference or the default value
     */
    public static String getRelativePart(Request request, String def) {
        String url = request.getResourceRef().getRemainingPart(true);

        int query = url.indexOf('?');
        if (query != -1) {
            url = url.substring(0, query);
        }

        if ((url == null) || (url.length() == 0) || url.equals("/")) {
            return def;
        } else {
            return url;
        }
    }

    private ScriptUtils() {
    }
}
