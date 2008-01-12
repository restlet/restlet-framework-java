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

import java.util.ArrayList;
import java.util.List;

import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Component that is easily configurable from Spring. Here is a usage example:
 * 
 * <pre>
 * &lt;bean id=&quot;component&quot; class=&quot;org.restlet.ext.spring.SpringComponent&quot;&gt;
 *     &lt;property name=&quot;server&quot; ref=&quot;server&quot; /&gt;
 *     &lt;property name=&quot;defaultTarget&quot; ref=&quot;application&quot; /&gt;
 * &lt;/bean&gt;
 * 
 * &lt;bean id=&quot;server&quot; class=&quot;org.restlet.ext.spring.SpringServer&quot;&gt;
 *     &lt;constructor-arg value=&quot;http&quot; /&gt;
 *     &lt;constructor-arg value=&quot;8182&quot; /&gt;
 * &lt;/bean&gt;
 * 
 * ...
 * </pre>
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SpringComponent extends org.restlet.Component {

    /**
     * Sets the list of clients, either as protocol names, Protocol instances or
     * Client instances.
     * 
     * @param clients
     *                The list of clients.
     */
    public void setClients(List<Object> clients) {
        for (Object client : clients) {
            if (client instanceof String) {
                getClients().add(Protocol.valueOf((String) client));
            } else if (client instanceof Protocol) {
                getClients().add((Protocol) client);
            } else if (client instanceof Server) {
                getClients().add((Client) client);
            } else {
                getLogger()
                        .warning(
                                "Unknown object found in the clients list. Only instances of String, org.restlet.data.Protocol and org.restlet.Client are allowed.");
            }
        }
    }

    public void setDefaultTarget(Restlet target) {
        getDefaultHost().attach(target);
    }

    public void setServer(Object serverInfo) {
        List<Object> servers = new ArrayList<Object>();
        servers.add(serverInfo);
        setServers(servers);
    }

    /**
     * Sets the list of servers, either as protocol names, Protocol instances or
     * Server instances.
     * 
     * @param serversInfo
     *                The list of servers.
     */
    public void setServers(List<Object> serversInfo) {
        for (Object serverInfo : serversInfo) {
            if (serverInfo instanceof String) {
                getServers().add(Protocol.valueOf((String) serverInfo));
            } else if (serverInfo instanceof Protocol) {
                getServers().add((Protocol) serverInfo);
            } else if (serverInfo instanceof Server) {
                Server server = (Server) serverInfo;
                server.setContext(getContext());
                getServers().add(server);
            } else {
                getLogger()
                        .warning(
                                "Unknown object found in the servers list. Only instances of String, org.restlet.data.Protocol and org.restlet.Server are allowed.");
            }
        }
    }

}
