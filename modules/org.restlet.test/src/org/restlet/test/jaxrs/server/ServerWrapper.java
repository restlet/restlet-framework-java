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
package org.restlet.test.jaxrs.server;


import javax.ws.rs.core.ApplicationConfig;

import org.restlet.Component;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.test.jaxrs.services.tests.JaxRsTestCase;

/**
 * This interface wraps a server for the tests. The default implementation is
 * the {@link RestletServerWrapper}, but there are other implementations
 * possible, for example for the JSR-311 reference implementation Jersey.
 * 
 * @see JaxRsTestCase#setServerWrapper(ServerWrapper)
 * 
 * @author Stephan Koops
 */
public interface ServerWrapper {
    /**
     * Starts the server with the given protocol on the given port with the
     * given Collection of root resource classes. The method {@link #setUp()}
     * will do this on every test start up.
     * 
     * @param appConfig
     * @param protocol
     *                the protocol to use
     * @param challengeScheme
     *                The challenge scheme for authentification
     * @param contextParameter
     *                A parameter to add to the context. may be null
     * @return Returns the started component. Should be stopped with
     *         {@link #stopServer(Component)}
     * @throws Exception
     */
    public void startServer(final ApplicationConfig appConfig,
            Protocol protocol, ChallengeScheme challengeScheme,
            Parameter contextParameter) throws Exception;

    /**
     * Stops the component. The method {@link #tearDown()} do this after every
     * test.
     * 
     * @param component
     * @throws Exception
     */
    public void stopServer() throws Exception;

    /**
     * Returns the port the server is running on.
     * @return
     */
    public int getPort();
}
