/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.simple;

import java.io.IOException;
import java.net.Socket;

import simple.http.BufferedPipelineFactory;
import simple.http.Pipeline;

/**
 * A subclass of BufferedPipelineFactory that sets the connection socket on each
 * pipeline for later retrieval.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
