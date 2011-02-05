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

package org.restlet.ext.spring;

import java.util.Enumeration;
import java.util.Properties;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * Server that is easily configurable with Spring. Here is a usage example:
 * 
 * <pre>
 * &lt;bean id=&quot;server&quot; class=&quot;org.restlet.ext.spring.SpringServer&quot;&gt;
 *      &lt;constructor-arg value=&quot;http&quot; /&gt;
 *      &lt;constructor-arg value=&quot;8111&quot; /&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel</a>
 */
public class SpringServer extends org.restlet.Server {

    /**
     * Constructor.
     * 
     * @param protocol
     *            The server's protocol such as "HTTP" or "HTTPS".
     */
    public SpringServer(String protocol) {
        super(new Context(), Protocol.valueOf(protocol), (Restlet) null);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The server's protocol such as "HTTP" or "HTTPS".
     * @param port
     *            The port number.
     */
    public SpringServer(String protocol, int port) {
        super(new Context(), Protocol.valueOf(protocol), port, (Restlet) null);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The server's protocol such as "HTTP" or "HTTPS".
     * @param address
     *            The IP address.
     * @param port
     *            The port number.
     */
    public SpringServer(String protocol, String address, int port) {
        super(new Context(), Protocol.valueOf(protocol), address, port, null);
    }

    /**
     * Sets parameters on the server.
     * 
     * @param parameters
     *            Parameters to set on the server.
     */
    public void setParameters(Properties parameters) {
        final Enumeration<?> names = parameters.propertyNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            getContext().getParameters()
                    .add(name, parameters.getProperty(name));
        }
    }

}
