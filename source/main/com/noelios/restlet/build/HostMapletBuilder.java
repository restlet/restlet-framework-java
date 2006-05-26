/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.build;

import org.restlet.data.Protocol;
import org.restlet.data.Status;

import com.noelios.restlet.HostMaplet;

/**
 * Fluent builder for Host Maplets.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HostMapletBuilder extends MapletBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public HostMapletBuilder(ObjectBuilder parent, HostMaplet node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public HostMaplet getNode()
   {
      return (HostMaplet)super.getNode();
   }

   /**
    * Attaches the configured Host Maplet to its parent Maplet.
    * @return The current builder.
    */
   public HostMapletBuilder attachParent()
   {
   	upMaplet().attach(getNode().getPattern(), getNode());
   	return this;
   }
   
   /**
    * Adds an allowed protocol.
    * @param protocol The protocol to allow.
    * @return The current builder.
    */
   public HostMapletBuilder allowProtocol(Protocol protocol)
   {
   	getNode().getAllowedProtocols().add(protocol);
   	return this;
   }

   /**
    * Removes an allowed protocol.
    * @param protocol The protocol to deny.
    * @return The current builder.
    */
   public HostMapletBuilder denyProtocol(Protocol protocol)
   {
   	getNode().getAllowedProtocols().remove(protocol);
   	return this;
   }

   /**
    * Adds an allowed domain.
    * @param domain The domain to allow.
    * @return The current builder.
    */
   public HostMapletBuilder allowDomain(String domain)
   {
   	getNode().getAllowedDomains().add(domain);
   	return this;
   }

   /**
    * Adds an allowed port.
    * @param port The port to allow.
    * @return The current builder.
    */
   public HostMapletBuilder allowPort(int port)
   {
   	getNode().getAllowedPorts().add(port);
   	return this;
   }

   /**
    * Sets the preferred protocol.
    * @param protocol The protocol to prefer.
    * @return The current builder.
    */
   public HostMapletBuilder preferProtocol(Protocol protocol)
   {
   	getNode().setPreferredProtocol(protocol);
   	return this;
   }

   /**
    * Sets the preferred domain.
    * @param domain The domain to prefer.
    * @return The current builder.
    */
   public HostMapletBuilder preferDomain(String domain)
   {
   	getNode().setPreferredDomain(domain);
   	return this;
   }

   /**
    * Sets the preferred port.
    * @param port The port to prefer.
    * @return The current builder.
    */
   public HostMapletBuilder preferPort(int port)
   {
   	getNode().setPreferredPort(port);
   	return this;
   }

   /**
    * Indicates that client redirects should be issued when the host URI doesn't match the preferred format.
    * @return The current builder.
    */
   public HostMapletBuilder redirectClient()
   {
   	getNode().setRedirectClient(true);
   	return this;
   }

	/**
	 * Indicates the redirection status used.
	 * @param status The redirection status used.
    * @return The current builder.
	 */
	public HostMapletBuilder redirectStatus(Status status)
	{
   	getNode().setRedirectStatus(status);
   	return this;
	}

   /**
    * Indicates that client warnings should be issued when the host URI doesn't match the preferred format.
	 * This will materialize as a Not Found status with a detailled explanation.
    * @return The current builder.
    */
   public HostMapletBuilder warnClient()
   {
   	getNode().setWarnClient(true);
   	return this;
   }
   
   /**
    * Indicates that IP addresses, equivalent of the domain names, are not allowed as a way to specify URIs.
    * @return The current builder.
    */
   public HostMapletBuilder denyIpAddresses()
   {
   	getNode().setAllowIpAddresses(false);
   	return this;
   }
   
   /**
    * Indicates that "localhost" is accepted as a valid domain name.
	 * In addition, if IP addresses are allowed, "127.0.0.1" is also denied.
    * @return The current builder.
    */
   public HostMapletBuilder denyLocalHost()
   {
   	getNode().setAllowLocalHost(false);
   	return this;
   }
   
   /**
	 * Indicates that default ports for the allowed protocols are not allowed.
	 * Concretely deny the usage of any URI without explicit port number.
    * @return The current builder.
    */
   public HostMapletBuilder denyDefaultPorts()
   {
   	getNode().setAllowDefaultPorts(false);
   	return this;
   }

}

