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

package org.restlet.ext.net.internal;

import java.net.HttpURLConnection;

import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

/**
 * Representation that wraps another representation and closes the parent
 * {@link HttpURLConnection} when the representation is released.
 * 
 * @author Kevin Conaway
 */
class ConnectionClosingRepresentation extends WrapperRepresentation {

    /** The parent connection. */
    private final HttpURLConnection connection;

    /**
     * Default constructor.
     * 
     * @param wrappedRepresentation
     *            The wrapped representation.
     * @param connection
     *            The parent connection.
     */
    public ConnectionClosingRepresentation(
            Representation wrappedRepresentation, HttpURLConnection connection) {
        super(wrappedRepresentation);
        this.connection = connection;
    }

    @Override
    public void release() {
        this.connection.disconnect();
        super.release();
    }

}