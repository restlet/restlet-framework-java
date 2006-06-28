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

import org.restlet.DefaultRouter;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.component.RestletContainer;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;

import com.noelios.restlet.CompressFilter;
import com.noelios.restlet.DecompressFilter;
import com.noelios.restlet.DirectoryHandler;
import com.noelios.restlet.ExtractFilter;
import com.noelios.restlet.GuardFilter;
import com.noelios.restlet.HostRouter;
import com.noelios.restlet.LogFilter;
import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.StatusFilter;
import com.noelios.restlet.HostRouter.UsageMode;

/**
 * Fluent builder for Restlet Containers.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class RestletContainerBuilder extends ComponentBuilder
{
	/**
	 * Constructor for new containers.
	 */
	public RestletContainerBuilder()
	{
		this(null, new RestletContainer());
	}
	
	/**
	 * Constructor for existing containers.
	 * @param node The wrapped node.
	 */
	public RestletContainerBuilder(RestletContainer node)
	{
		this(null, node);
	}
	
	/**
	 * Constructor for existing containers.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public RestletContainerBuilder(ObjectBuilder parent, RestletContainer node)
	{
		super(parent, node);
	}
	
   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
	public RestletContainer getNode()
	{
		return (RestletContainer)super.getNode();
	}

   /**
    * Adds a server connector to this component.
    * @param name The unique connector name.
    * @param server The server connector to add.
    * @return The current builder.
    */
   public RestletContainerBuilder addServer(String name, Server server)
   {
      return (RestletContainerBuilder)super.addServer(name, server);
   }

   /**
    * Adds a server connector to this component.
    * @param name The unique connector name.
    * @param protocol The connector protocol.
    * @param port The listening port.
    * @return The current builder.
    */
   public RestletContainerBuilder addServer(String name, Protocol protocol, int port)
   {
      return (RestletContainerBuilder)super.addServer(name, protocol, port);
   }

   /**
    * Adds a server connector to this component.
    * @param name The unique connector name.
    * @param protocol The connector protocol.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
    * @return The current builder.
    */
   public RestletContainerBuilder addServer(String name, Protocol protocol, String address, int port)
   {
      return (RestletContainerBuilder)super.addServer(name, protocol, address, port);
   }

   /**
    * Adds a client connector to this component.
    * @param name The unique connector name.
    * @param client The client connector to add.
    * @return The current builder.
    */
   public RestletContainerBuilder addClient(String name, Client client)
   {
      return (RestletContainerBuilder)super.addClient(name, client);
   }

   /**
    * Adds a new client connector to this component. 
    * @param name The unique connector name.
    * @param protocol The connector protocol.
    * @return The current builder.
    */
   public RestletContainerBuilder addClient(String name, Protocol protocol)
   {
      return (RestletContainerBuilder)super.addClient(name, protocol);
   }

   /**
    * Sets a property.
    * @param name The property name.
    * @param value The property value.
    * @return The current builder.
    */
   public RestletContainerBuilder addParameter(String name, String value)
   {
      return (RestletContainerBuilder)super.addParameter(name, value);
   }

   /** 
    * Starts the component. 
    * @return The current builder.
    */
   public RestletContainerBuilder start() throws Exception
   {
      return (RestletContainerBuilder)super.start();
   }

   
   // --------------------------------------
   // Methods copied from the Filter Builder
   // --------------------------------------
   
   /**
    * Attaches a target Restlet. Note that you don't need to specify an owner component 
    * for your target as the container's owner will automatically be set for you.
    * @param target The target instance to attach.
    * @return The builder for the target.
    */
   public RestletBuilder attach(Restlet target)
   {
   	target.setOwner(getNode().getOwner());
      getNode().attach(target);
      return Builders.buildRestlet(this, target);
   }

   /**
    * Attaches a target Filter. Note that you don't need to specify an owner component 
    * for your target as the container's owner will automatically be set for you.
    * @param target The target filter to attach.
    * @return The builder for the target.
    */
   public FilterBuilder attach(Filter target)
   {
   	target.setOwner(getNode().getOwner());
      getNode().attach(target);
      return Builders.buildFilter(this, target);
   }

   /**
    * Attaches a target Router. Note that you don't need to specify an owner component 
    * for your target as the container's owner will automatically be set for you.
    * @param target The target router to attach.
    * @return The builder for the target.
    */
   public RouterBuilder attach(Router target)
   {
   	target.setOwner(getNode().getOwner());
      getNode().attach(target);
      return Builders.buildRouter(this, target);
   }

   /**
    * Attaches a Compress Filter.
    * @return The builder for the created node.
    */
   public FilterBuilder attachCompress()
   {
      CompressFilter node = new CompressFilter(getNode());
      getNode().attach(node);
      return Builders.buildFilter(this, node);
   }

   /**
    * Attaches a Decompress Filter. Only decodes input representations before call handling.
    * @return The builder for the created node.
    */
   public FilterBuilder attachDecompress()
   {
   	return attachDecompress(true, false);
   }

   /**
    * Attaches a Decompress Filter.
	 * @param decodeInput Indicates if the input representation should be decoded.
	 * @param decodeOutput Indicates if the output representation should be decoded.
    * @return The builder for the created node.
	 */
	public FilterBuilder attachDecompress(boolean decodeInput, boolean decodeOutput)
	{
      DecompressFilter node = new DecompressFilter(getNode(), decodeInput, decodeOutput);
      getNode().attach(node);
      return Builders.buildFilter(this, node);
	}
   
   /**
    *	Attaches a Directory Restlet.
    * @param rootUri The directory's root URI.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    * @return The builder for the created node.
    */
   public RestletBuilder attachDirectory(String rootUri, boolean deeply, String indexName)
   {
      DirectoryHandler node = new DirectoryHandler(getNode(), rootUri, deeply, indexName);
      getNode().attach(node);
      return Builders.buildRestlet(this, node);
   }

   /**
    * Attaches an Extract Filter.
    * @return The builder for the created node.
    */
   public ExtractFilterBuilder attachExtract()
   {
      ExtractFilter node = new ExtractFilter(getNode());
      getNode().attach(node);
      return Builders.buildExtract(this, node);
   }

   /**
    * Attaches an Guard Filter.
    * @param logName The log name to used in the logging.properties file.
    * @param authentication Indicates if the guard should attempt to authenticate the caller.
    * @param scheme The authentication scheme to use. 
    * @param realm The authentication realm.
    * @param authorization Indicates if the guard should attempt to authorize the caller.
    * @return The builder for the created node.
    */
   public GuardFilterBuilder attachGuard(String logName, boolean authentication, ChallengeScheme scheme, String realm, boolean authorization)
   {
   	GuardFilter node = new GuardFilter(getNode(), logName, authentication, scheme, realm, authorization);
      getNode().attach(node);
      return Builders.buildGuard(this, node);
   }
   
   /**
    * Creates a host router for attachment in router mode. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(int port)
   {
      HostRouter node = new HostRouter(getNode(), port);
      node.setMode(UsageMode.ROUTER);
      return Builders.buildHost(this, node);
   }
   
   /**
    * Creates a host router for attachment in router mode. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param domain The domain name. 
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(String domain)
   {
      HostRouter node = new HostRouter(getNode(), domain);
      node.setMode(UsageMode.ROUTER);
      return Builders.buildHost(this, node);
   }

   /**
    * Creates a host router for attachment in router mode. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param domain The domain name. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(String domain, int port)
   {
      HostRouter node = new HostRouter(getNode(), domain, port);
      node.setMode(UsageMode.ROUTER);
      return Builders.buildHost(this, node);
   }

   /**
    * Attaches Log Filter using the default format.<br/>
    * Default format using <a href="http://analog.cx/docs/logfmt.html">Analog syntax</a>: %Y-%m-%d\t%h:%n:%j\t%j\t%r\t%u\t%s\t%j\t%B\t%f\t%c\t%b\t%q\t%v\t%T
    * @param logName The log name to used in the logging.properties file.
    * @return The builder for the created node.
    */
   public FilterBuilder attachLog(String logName)
   {
      LogFilter node = new LogFilter(getNode(), logName);
      getNode().attach(node);
      return Builders.buildFilter(this, node);
   }

   /**
    * Attaches Log Filter.
    * @param logName The log name to used in the logging.properties file.
    * @param logFormat The log format to use.
    * @return The builder for the created node.
    * @see com.noelios.restlet.util.CallModel
    * @see com.noelios.restlet.util.StringTemplate
    */
   public FilterBuilder attachLog(String logName, String logFormat)
   {
      LogFilter node = new LogFilter(getNode(), logName, logFormat);
      getNode().attach(node);
      return Builders.buildFilter(this, node);
   }

   /**
    * Attaches a router. 
    * @return The builder for the created node.
    */
   public RouterBuilder attachRouter()
   {
      DefaultRouter node = new DefaultRouter(getNode());
      getNode().attach(node);
      return Builders.buildRouter(this, node);
   }

   /**
    * Attaches a Redirect Restlet.
    * @param targetPattern The pattern to build the target URI.
    * @param mode The redirection mode.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirect(String targetPattern, int mode)
   {
      RedirectRestlet node = new RedirectRestlet(getNode(), targetPattern, mode);
      getNode().attach(node);
      return Builders.buildRestlet(this, node);
   }

   /**
    * Attaches a Redirect Restlet in the Connector mode.
    * @param targetPattern The pattern to build the target URI.
    * @param connectorName The connector Name.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirect(String targetPattern, String connectorName)
   {
      RedirectRestlet node = new RedirectRestlet(getNode(), targetPattern, connectorName);
      getNode().attach(node);
      return Builders.buildRestlet(this, node);
   }
   
   /**
    * Attaches a Status Filter.
    * @param overwrite Indicates whether an existing representation should be overwritten.
    * @param email Email address of the administrator to contact in case of error.
    * @param homeURI The home URI to display in case the user got a "not found" exception.
    * @return The builder for the target.
    */
   public FilterBuilder attachStatus(boolean overwrite, String email, String homeURI)
   {
      StatusFilter node = new StatusFilter(getNode(), overwrite, email, homeURI);
      getNode().attach(node);
      return Builders.buildFilter(this, node);
   }

}
