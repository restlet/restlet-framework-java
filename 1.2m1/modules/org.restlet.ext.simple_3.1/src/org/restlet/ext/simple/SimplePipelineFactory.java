/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.simple;

import java.io.IOException;
import java.net.Socket;

import simple.http.BufferedPipelineFactory;
import simple.http.Pipeline;

/**
 * A subclass of BufferedPipelineFactory that sets the connection socket on each
 * pipeline for later retrieval.
 * 
 * @author Jerome Louvel
 */
public class SimplePipelineFactory extends BufferedPipelineFactory {
    public static final String PROPERTY_SOCKET = "org.restlet.ext.simple.socket";

    /**
     * Constructor.
     */
    public SimplePipelineFactory() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param size
     *            The size of the output buffer used
     */
    public SimplePipelineFactory(int size) {
        super(size);
    }

    @Override
    public Pipeline getInstance(Socket sock) throws IOException {
        final Pipeline result = super.getInstance(sock);
        result.put(PROPERTY_SOCKET, sock);
        return result;
    }
}
