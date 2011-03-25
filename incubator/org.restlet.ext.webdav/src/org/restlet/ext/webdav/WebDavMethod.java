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

package org.restlet.ext.webdav;

import org.restlet.data.Method;

/**
 * Constants for WebDAV methods.
 * 
 * @author Jerome Louvel
 */
public final class WebDavMethod {

    private static final String BASE_WEBDAV = "http://tools.ietf.org/html/rfc4918";

    /**
     * Retrieves the properties defined on the resource.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-9.1">WebDAV RFC
     *      - 9.1 PROPFIND</a>
     */
    public static final Method PROPFIND = new Method("PROPFIND",
            "Retrieve properties", BASE_WEBDAV + "#section-9.1", true, true);

    /**
     * Sets and/or removes properties defined on the resource.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-9.2">WebDAV RFC
     *      - 9.2 PROPPATCH</a>
     */
    public static final Method PROPPATCH = new Method("PROPPATCH",
            "Sets and/or removes properties", BASE_WEBDAV + "#section-9.2",
            false, true);

    /**
     * Creates a new collection resource.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-9.3">WebDAV RFC
     *      - 9.3 MKCOL</a>
     */
    public static final Method MKCOL = new Method("MKCOL",
            "Creates a new collection resource", BASE_WEBDAV + "#section-9.3",
            false, true);

    /**
     * Creates a duplicate of the source resource identified in the destination
     * resource.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-9.8">WebDAV RFC
     *      - 9.8 COPY</a>
     */
    public static final Method COPY = new Method("COPY",
            "Creates a duplicate resource", BASE_WEBDAV + "#section-9.8",
            false, true);

    /**
     * Moves the source resource identified in the destination resource.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-9.9">WebDAV RFC
     *      - 9.9 MOVE</a>
     */
    public static final Method MOVE = new Method("MOVE", "Moves the resource",
            BASE_WEBDAV + "#section-9.9", false, true);

    /**
     * Takes out a lock of any access type or refreshes an existing lock.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-9.10">WebDAV RFC
     *      - 9.10 LOCK</a>
     */
    public static final Method LOCK = new Method("LOCK",
            "Takes out or refreshes a lock", BASE_WEBDAV + "#section-9.10",
            false, false);

    /**
     * Removes the lock on the resource.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-9.11">WebDAV RFC
     *      - 9.11 UNLOCK</a>
     */
    public static final Method UNLOCK = new Method("UNLOCK",
            "Removes the lock", BASE_WEBDAV + "#section-9.11", false, true);

    /**
     * Returns the method associated to a given method name. If an existing
     * constant exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The method name.
     * @return The associated method.
     */
    public static Method valueOf(final String name) {
        Method result = null;

        if ((name != null) && !name.equals("")) {
            if (name.equalsIgnoreCase(PROPFIND.getName())) {
                result = PROPFIND;
            } else if (name.equalsIgnoreCase(PROPPATCH.getName())) {
                result = PROPPATCH;
            } else if (name.equalsIgnoreCase(MKCOL.getName())) {
                result = MKCOL;
            } else if (name.equalsIgnoreCase(COPY.getName())) {
                result = COPY;
            } else if (name.equalsIgnoreCase(MOVE.getName())) {
                result = MOVE;
            } else if (name.equalsIgnoreCase(LOCK.getName())) {
                result = LOCK;
            } else if (name.equalsIgnoreCase(UNLOCK.getName())) {
                result = UNLOCK;
            } else {
                result = new Method(name);
            }
        }

        return result;
    }
}
