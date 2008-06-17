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

package org.restlet.ext.spring;

import org.restlet.data.Protocol;

import java.util.Properties;
import java.util.Enumeration;

/**
 * Server that is easily configurable with Spring. Here is a usage example:
 * 
 * <pre>
 * &lt;bean id=&quot;server&quot; class=&quot;org.restlet.ext.spring.SpringServer&quot;&gt;
 *      &lt;constructor-arg value=&quot;http&quot; /&gt;
 *      &lt;constructor-arg value=&quot;8182&quot; /&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel (contact@noelios.com)</a>
 */
public class SpringServer extends org.restlet.Server {

    /**
     * Constructor.
     * 
     * @param protocol
     *                The server's protocol such as "HTTP" or "HTTPS".
     */
    public SpringServer(String protocol) {
        super(Protocol.valueOf(protocol), null);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *                The server's protocol such as "HTTP" or "HTTPS".
     * @param port
     *                The port number.
     */
    public SpringServer(String protocol, int port) {
        super(Protocol.valueOf(protocol), port, null);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *                The server's protocol such as "HTTP" or "HTTPS".
     * @param address
     *                The IP address.
     * @param port
     *                The port number.
     */
    public SpringServer(String protocol, String address, int port) {
        super(Protocol.valueOf(protocol), address, port, null);
    }

    /**
     * Sets parameters on the server.
     * 
     * @param parameters
     *                Parameters to set on the server.
     */
    public void setParameters(Properties parameters) {
        Enumeration<?> names = parameters.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            getContext().getParameters()
                    .add(name, parameters.getProperty(name));
        }
    }

}
