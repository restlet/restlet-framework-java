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

import org.restlet.DefaultMaplet;
import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.component.RestletContainer;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;

import com.noelios.restlet.CompressChainlet;
import com.noelios.restlet.DecompressChainlet;
import com.noelios.restlet.DirectoryRestlet;
import com.noelios.restlet.ExtractChainlet;
import com.noelios.restlet.GuardChainlet;
import com.noelios.restlet.HostMaplet;
import com.noelios.restlet.LogChainlet;
import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.StatusChainlet;
import com.noelios.restlet.HostMaplet.AttachmentMode;

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
   public RestletContainerBuilder setProperty(String name, String value)
   {
      return (RestletContainerBuilder)super.setProperty(name, value);
   }

   /** 
    * Starts the component. 
    * @return The current builder.
    */
   public RestletContainerBuilder start() throws Exception
   {
      return (RestletContainerBuilder)super.start();
   }

   
   // ---------------------------------------
   // Methods copied from the ChainletBuilder
   // ---------------------------------------
   
   /**
    * Attaches a target instance shared by all calls. Note that you don't need to specify an owner component 
    * for your target as the chainlet's owner will automatically be set for you.
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
    * Attaches a target class. A new instance will be created for each call.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @return The builder for the attached target.
    */
   @SuppressWarnings("unchecked")
	public ObjectBuilder attach(Class<? extends Restlet> targetClass)
   {
      getNode().attach(targetClass);
      return Builders.buildObject(this, targetClass);
   }

   /**
    * Attaches a Compress Chainlet.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachCompress()
   {
      CompressChainlet node = new CompressChainlet(getNode().getOwner());
      getNode().attach(node);
      return Builders.buildChainlet(this, node);
   }

   /**
    * Attaches a Decompress Chainlet. Only decodes input representations before call handling.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachDecompress()
   {
   	return attachDecompress(true, false);
   }

   /**
    * Attaches a Decompress Chainlet.
	 * @param decodeInput Indicates if the input representation should be decoded.
	 * @param decodeOutput Indicates if the output representation should be decoded.
    * @return The builder for the created node.
	 */
	public ChainletBuilder attachDecompress(boolean decodeInput, boolean decodeOutput)
	{
      DecompressChainlet node = new DecompressChainlet(getNode().getOwner(), decodeInput, decodeOutput);
      getNode().attach(node);
      return Builders.buildChainlet(this, node);
	}
   
   /**
    *	Attaches a Directory Restlet.
    * @param fileClientName The file client connector name.
    * @param rootPath The directory's root path.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    * @return The builder for the created node.
    */
   public RestletBuilder attachDirectory(String fileClientName, String rootPath, boolean deeply, String indexName)
   {
      DirectoryRestlet node = new DirectoryRestlet(getNode().getOwner(), fileClientName, rootPath, deeply, indexName);
      getNode().attach(node);
      return Builders.buildRestlet(this, node);
   }

   /**
    * Attaches an Extract Chainlet.
    * @return The builder for the created node.
    */
   public ExtractChainletBuilder attachExtract()
   {
      ExtractChainlet node = new ExtractChainlet(getNode().getOwner());
      getNode().attach(node);
      return Builders.buildExtract(this, node);
   }

   /**
    * Attaches an Guard Chainlet.
    * @param logName The log name to used in the logging.properties file.
    * @param authentication Indicates if the guard should attempt to authenticate the caller.
    * @param scheme The authentication scheme to use. 
    * @param realm The authentication realm.
    * @param authorization Indicates if the guard should attempt to authorize the caller.
    * @return The builder for the created node.
    */
   public GuardChainletBuilder attachGuard(String logName, boolean authentication, ChallengeScheme scheme, String realm, boolean authorization)
   {
   	GuardChainlet node = new GuardChainlet(getNode().getOwner(), logName, authentication, scheme, realm, authorization);
      getNode().attach(node);
      return Builders.buildGuard(this, node);
   }
   
   /**
    * Attaches a Host Maplet. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostMapletBuilder attachHost(int port, AttachmentMode mode)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), port);
      node.setMode(mode);
      
      if(mode.equals(AttachmentMode.CHAINLET))
      {
      	getNode().attach(node);
      }
      else if(mode.equals(AttachmentMode.MAPLET))
      {
      	getNode().attach(node.getPattern(), node);
      }
      return Builders.buildHost(this, node);
   }
   
   /**
    * Creates a Host Maplet for attachment in Maplet mode. 
    * This variant of attachHost is necessary if all the configuration of the Host Maplet requires 
    * more than a domain name and a port number. This is because the Maplet attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostMapletBuilder createHost(int port)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), port);
      node.setMode(AttachmentMode.MAPLET);
      return Builders.buildHost(this, node);
   }
   
   /**
    * Attaches a Host Maplet. 
    * @param domain The domain name. 
    * @return The builder for the created node.
    */
   public HostMapletBuilder attachHost(String domain, AttachmentMode mode)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), domain);
      node.setMode(mode);
      
      if(mode.equals(AttachmentMode.CHAINLET))
      {
      	getNode().attach(node);
      }
      else if(mode.equals(AttachmentMode.MAPLET))
      {
      	getNode().attach(node.getPattern(), node);
      }
      return Builders.buildHost(this, node);
   }
   
   /**
    * Creates a Host Maplet for attachment in Maplet mode. 
    * This variant of attachHost is necessary if all the configuration of the Host Maplet requires 
    * more than a domain name and a port number. This is because the Maplet attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param domain The domain name. 
    * @return The builder for the created node.
    */
   public HostMapletBuilder createHost(String domain)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), domain);
      node.setMode(AttachmentMode.MAPLET);
      return Builders.buildHost(this, node);
   }

   /**
    * Attaches a Host Maplet.
    * @param domain The domain name. 
    * @param port The host port.
    * @param mode The attachment mode.
    * @return The builder for the created node.
    */
   public HostMapletBuilder attachHost(String domain, int port, AttachmentMode mode)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), domain, port);
      node.setMode(mode);
      
      if(mode.equals(AttachmentMode.CHAINLET))
      {
      	getNode().attach(node);
      }
      else if(mode.equals(AttachmentMode.MAPLET))
      {
      	getNode().attach(node.getPattern(), node);
      }

      return Builders.buildHost(this, node);
   }
   
   /**
    * Creates a Host Maplet for attachment in Maplet mode. 
    * This variant of attachHost is necessary if all the configuration of the Host Maplet requires 
    * more than a domain name and a port number. This is because the Maplet attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param domain The domain name. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostMapletBuilder createHost(String domain, int port)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), domain, port);
      node.setMode(AttachmentMode.MAPLET);
      return Builders.buildHost(this, node);
   }

   /**
    * Attaches Log Chainlet using the default format.<br/>
    * Default format using <a href="http://analog.cx/docs/logfmt.html">Analog syntax</a>: %Y-%m-%d\t%h:%n:%j\t%j\t%r\t%u\t%s\t%j\t%B\t%f\t%c\t%b\t%q\t%v\t%T
    * @param logName The log name to used in the logging.properties file.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachLog(String logName)
   {
      LogChainlet node = new LogChainlet(getNode().getOwner(), logName);
      getNode().attach(node);
      return Builders.buildChainlet(this, node);
   }

   /**
    * Attaches Log Chainlet.
    * @param logName The log name to used in the logging.properties file.
    * @param logFormat The log format to use.
    * @return The builder for the created node.
    * @see com.noelios.restlet.util.CallModel
    * @see com.noelios.restlet.util.StringTemplate
    */
   public ChainletBuilder attachLog(String logName, String logFormat)
   {
      LogChainlet node = new LogChainlet(getNode().getOwner(), logName, logFormat);
      getNode().attach(node);
      return Builders.buildChainlet(this, node);
   }

   /**
    * Attaches a Maplet. 
    * @return The builder for the created node.
    */
   public MapletBuilder attachMaplet()
   {
      Maplet node = new DefaultMaplet(getNode().getOwner());
      getNode().attach(node);
      return Builders.buildMaplet(this, node);
   }

   /**
    * Attaches a Redirect Restlet.
    * @param targetPattern The pattern to build the target URI.
    * @param mode The redirection mode.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirectRestlet(String targetPattern, int mode)
   {
      RedirectRestlet node = new RedirectRestlet(getNode().getOwner(), targetPattern, mode);
      getNode().attach(node);
      return Builders.buildRestlet(this, node);
   }

   /**
    * Attaches a Redirect Restlet in the Connector mode.
    * @param targetPattern The pattern to build the target URI.
    * @param connectorName The connector Name.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirectRestlet(String targetPattern, String connectorName)
   {
      RedirectRestlet node = new RedirectRestlet(getNode().getOwner(), targetPattern, connectorName);
      getNode().attach(node);
      return Builders.buildRestlet(this, node);
   }
   
   /**
    * Attaches a Status Chainlet.
    * @param overwrite Indicates whether an existing representation should be overwritten.
    * @param email Email address of the administrator to contact in case of error.
    * @param homeURI The home URI to display in case the user got a "not found" exception.
    * @return The builder for the target.
    */
   public ChainletBuilder attachStatus(boolean overwrite, String email, String homeURI)
   {
      StatusChainlet node = new StatusChainlet(getNode().getOwner(), overwrite, email, homeURI);
      getNode().attach(node);
      return Builders.buildChainlet(this, node);
   }

   // -------------------------------------
   // Methods copied from the MapletBuilder
   // -------------------------------------
   
   /**
    * Attaches a target instance shared by all calls. Note that you don't need to specify an owner component 
    * for your target as the chainlet's owner will automatically be set for you.
    * @param pattern The URI pattern used to map calls.
    * @param target The target instance to attach.
    * @return The current Maplet for further attachments.
    * @see java.util.regex.Pattern
    */
   public RestletBuilder attach(String pattern, Restlet target)
   {
   	target.setOwner(getNode().getOwner());
      getNode().attach(pattern, target);
      return Builders.buildRestlet(this, target);
   }

   /**
    * Attaches at a specific a target instance shared by all calls. Note that you don't need to specify an 
    * owner component for your target as the chainlet's owner will automatically be set for you.
    * @param pattern The URI pattern used to map calls.
    * @param target The target instance to attach.
    * @param override Indicates if this attachment should have a higher priority that existing ones.
    * @return The current Maplet for further attachments.
    * @see java.util.regex.Pattern
    */
   public RestletBuilder attach(String pattern, Restlet target, boolean override)
   {
   	target.setOwner(getNode().getOwner());
      getNode().attach(pattern, target, override);
      return Builders.buildRestlet(this, target);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pattern The URI pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @return The current Maplet for further attachments.
    * @see java.util.regex.Pattern
    */
   @SuppressWarnings("unchecked")
	public ObjectBuilder attach(String pattern, Class<? extends Restlet> targetClass)
   {
      getNode().attach(pattern, targetClass);
      return Builders.buildObject(this, (Object)targetClass);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pattern The URI pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @param override Indicates if this attachment should have a higher priority that existing ones.
    * @return The current Maplet for further attachments.
    * @see java.util.regex.Pattern
    */
   @SuppressWarnings("unchecked")
	public ObjectBuilder attach(String pattern, Class<? extends Restlet> targetClass, boolean override)
   {
      getNode().attach(pattern, targetClass, override);
      return Builders.buildObject((ObjectBuilder)this, (Object)targetClass);
   }

   /**
    * Attaches a Compress Chainlet.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachCompress(String pattern)
   {
      CompressChainlet node = new CompressChainlet(getNode().getOwner());
      getNode().attach(pattern, node);
      return Builders.buildChainlet(this, node);
   }

   /**
    * Attaches a Decompress Chainlet. Only decodes input representations before call handling.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachDecompress(String pattern)
   {
   	return attachDecompress(pattern, true, false);
   }

   /**
    * Attaches a Decompress Chainlet.
	 * @param decodeInput Indicates if the input representation should be decoded.
	 * @param decodeOutput Indicates if the output representation should be decoded.
    * @return The builder for the created node.
	 */
	public ChainletBuilder attachDecompress(String pattern, boolean decodeInput, boolean decodeOutput)
	{
      DecompressChainlet node = new DecompressChainlet(getNode().getOwner(), decodeInput, decodeOutput);
      getNode().attach(pattern, node);
      return Builders.buildChainlet(this, node);
	}
   
   /**
    *	Attaches a Directory Restlet.
    * @param fileClientName The file client connector name.
    * @param rootPath The directory's root path.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    * @return The builder for the created node.
    */
   public RestletBuilder attachDirectory(String pattern, String fileClientName, String rootPath, boolean deeply, String indexName)
   {
      DirectoryRestlet node = new DirectoryRestlet(getNode().getOwner(), fileClientName, rootPath, deeply, indexName);
      getNode().attach(pattern, node);
      return Builders.buildRestlet(this, node);
   }

   /**
    * Attaches an Extract Chainlet.
    * @return The builder for the created node.
    */
   public ExtractChainletBuilder attachExtract(String pattern)
   {
      ExtractChainlet node = new ExtractChainlet(getNode().getOwner());
      getNode().attach(pattern, node);
      return Builders.buildExtract(this, node);
   }

   /**
    * Attaches an Guard Chainlet.
    * @param logName The log name to used in the logging.properties file.
    * @param authentication Indicates if the guard should attempt to authenticate the caller.
    * @param scheme The authentication scheme to use. 
    * @param realm The authentication realm.
    * @param authorization Indicates if the guard should attempt to authorize the caller.
    * @return The builder for the created node.
    */
   public GuardChainletBuilder attachGuard(String pattern, String logName, boolean authentication, ChallengeScheme scheme, String realm, boolean authorization)
   {
   	GuardChainlet node = new GuardChainlet(getNode().getOwner(), logName, authentication, scheme, realm, authorization);
      getNode().attach(pattern, node);
      return Builders.buildGuard(this, node);
   }

   /**
    * Attaches Log Chainlet.
    * @param logName The log name to used in the logging.properties file.
    * @param logFormat The log format to use.
    * @return The builder for the created node.
    * @see com.noelios.restlet.util.CallModel
    * @see com.noelios.restlet.util.StringTemplate
    */
   public ChainletBuilder attachLog(String pattern, String logName, String logFormat)
   {
      LogChainlet node = new LogChainlet(getNode().getOwner(), logName, logFormat);
      getNode().attach(pattern, node);
      return Builders.buildChainlet(this, node);
   }

   /**
    * Attaches a Maplet. 
    * @return The builder for the created node.
    */
   public MapletBuilder attachMaplet(String pattern)
   {
      Maplet node = new DefaultMaplet(getNode().getOwner());
      getNode().attach(pattern, node);
      return Builders.buildMaplet(this, node);
   }

   /**
    * Attaches a Redirect Restlet.
    * @param targetPattern The pattern to build the target URI.
    * @param mode The redirection mode.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirectRestlet(String pattern, String targetPattern, int mode)
   {
      RedirectRestlet node = new RedirectRestlet(getNode().getOwner(), targetPattern, mode);
      getNode().attach(pattern, node);
      return Builders.buildRestlet(this, node);
   }

   /**
    * Attaches a Redirect Restlet in the Connector mode.
    * @param targetPattern The pattern to build the target URI.
    * @param connectorName The connector Name.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirectRestlet(String pattern, String targetPattern, String connectorName)
   {
      RedirectRestlet node = new RedirectRestlet(getNode().getOwner(), targetPattern, connectorName);
      getNode().attach(pattern, node);
      return Builders.buildRestlet(this, node);
   }
   
   /**
    * Attaches a Status Chainlet.
    * @param overwrite Indicates whether an existing representation should be overwritten.
    * @param email Email address of the administrator to contact in case of error.
    * @param homeURI The home URI to display in case the user got a "not found" exception.
    * @return The builder for the target.
    */
   public ChainletBuilder attachStatus(String pattern, boolean overwrite, String email, String homeURI)
   {
      StatusChainlet node = new StatusChainlet(getNode().getOwner(), overwrite, email, homeURI);
      getNode().attach(pattern, node);
      return Builders.buildChainlet(this, node);
   }

}
