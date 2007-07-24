/*
 * Copyright 2005-2007 Noelios Consulting.
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
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel (contact@noelios.com)</a>
 */
public class SpringServer extends org.restlet.Server {

    public SpringServer(String protocol) {
        super(Protocol.valueOf(protocol), null);
    }

    public SpringServer(String protocol, int port) {
        super(Protocol.valueOf(protocol), port, null);
    }

    public SpringServer(String protocol, String address, int port) {
        super(Protocol.valueOf(protocol), address, port, null);
    }

}
