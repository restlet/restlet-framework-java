/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.engine.io;

import java.io.BufferedReader;

/**
 * IO manipulation utilities.
 * 
 * @author Thierry Boileau
 */
public class IoUtils {

    /** The buffer size. */
    public static final int BUFFER_SIZE = 8192;

    /** The number of milliseconds after which IO operation will time out. */
    public static final int IO_TIMEOUT = 60000;

    /**
     * Returns the size to use when instantiating buffered items such as
     * instances of the {@link BufferedReader} class. It looks for the System
     * property "org.restlet.engine.io.buffer.size" and if not defined, uses the
     * {@link #BUFFER_SIZE}.
     * 
     * @return The size to use when instantiating buffered items.
     */
    public static int getBufferSize() {
        int result = BUFFER_SIZE;

        // [ifndef gwt]
        try {
            result = Integer.parseInt(System
                    .getProperty("org.restlet.engine.io.buffer.size"));
        } catch (NumberFormatException nfe) {
            result = BUFFER_SIZE;
        }
        // [enddef]

        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private IoUtils() {
    }
}
