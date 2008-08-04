/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream decorator to trap close() calls so that the decorated stream
 * does not get closed.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 */
public class KeepAliveOutputStream extends FilterOutputStream {

    /**
     * Constructor.
     * 
     * @param source
     *            The decorated source stream.
     */
    public KeepAliveOutputStream(OutputStream source) {
        super(source);
    }

    @Override
    public void close() throws IOException {
    }
}
