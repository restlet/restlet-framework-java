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

package org.restlet.util;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Delegate used by API classes to get support from the implementation classes.
 * Note that this is an SPI class that is not intended for public usage.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Helper {
    /**
     * Creates a new context.
     * 
     * @param loggerName
     *            The JDK's logger name to use for contextual logging.
     * @return The new context.
     */
    public abstract Context createContext(String loggerName);

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public abstract void handle(Request request, Response response);

    /** Start callback. */
    public abstract void start() throws Exception;

    /** Stop callback. */
    public abstract void stop() throws Exception;
}
