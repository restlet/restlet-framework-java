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
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;

import com.noelios.restlet.CompressChainlet;
import com.noelios.restlet.DecompressChainlet;
import com.noelios.restlet.DirectoryRestlet;
import com.noelios.restlet.ExtractChainlet;
import com.noelios.restlet.FileRestlet;
import com.noelios.restlet.GuardChainlet;
import com.noelios.restlet.HostMaplet;
import com.noelios.restlet.HostMaplet.AttachmentMode;
import com.noelios.restlet.LogChainlet;
import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.StatusChainlet;

/**
 * Fluent builder for Maplets.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class MapletBuilder extends RestletBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped Maplet.
	 */
   public MapletBuilder(DefaultBuilder parent, Maplet node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public Maplet getNode()
   {
      return (Maplet)super.getNode();
   }
   
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
      return new RestletBuilder(this, target);
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
      return new RestletBuilder(this, target);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pattern The URI pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @return The current Maplet for further attachments.
    * @see java.util.regex.Pattern
    */
   public DefaultBuilder attach(String pattern, Class<? extends Restlet> targetClass)
   {
      getNode().attach(pattern, targetClass);
      return new DefaultBuilder(this, (Object)targetClass);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pattern The URI pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @param override Indicates if this attachment should have a higher priority that existing ones.
    * @return The current Maplet for further attachments.
    * @see java.util.regex.Pattern
    */
   public DefaultBuilder attach(String pattern, Class<? extends Restlet> targetClass, boolean override)
   {
      getNode().attach(pattern, targetClass, override);
      return new DefaultBuilder((DefaultBuilder)this, (Object)targetClass);
   }

   /**
    * Attaches a Compress Chainlet.
    * @param pattern The URI pattern used to map calls.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachCompress(String pattern)
   {
      CompressChainlet node = new CompressChainlet(getNode().getOwner());
      getNode().attach(pattern, node);
      return new ChainletBuilder(this, node);
   }

   /**
    * Attaches a Decompress Chainlet. Only decodes input representations before call handling.
    * @param pattern The URI pattern used to map calls.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachDecompress(String pattern)
   {
   	return attachDecompress(pattern, true, false);
   }

   /**
    * Attaches a Decompress Chainlet.
    * @param pattern The URI pattern used to map calls.
	 * @param decodeInput Indicates if the input representation should be decoded.
	 * @param decodeOutput Indicates if the output representation should be decoded.
    * @return The builder for the created node.
	 */
	public ChainletBuilder attachDecompress(String pattern, boolean decodeInput, boolean decodeOutput)
	{
      DecompressChainlet node = new DecompressChainlet(getNode().getOwner(), decodeInput, decodeOutput);
      getNode().attach(pattern, node);
      return new ChainletBuilder(this, node);
	}
   
   /**
    *	Attaches a Directory Restlet.
    * @param pattern The URI pattern used to map calls.
    * @param rootPath The directory's root path.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    * @param commonExtensions Indicates if the common extensions should be added.
    * @return The builder for the created node.
    */
   public DirectoryRestletBuilder attachDirectory(String pattern, String rootPath, boolean deeply, String indexName, boolean commonExtensions)
   {
      DirectoryRestlet node = new DirectoryRestlet(getNode().getOwner(), rootPath, deeply, indexName, commonExtensions);
      getNode().attach(pattern, node);
      return new DirectoryRestletBuilder(this, node);
   }

   /**
    * Attaches an Extract Chainlet.
    * @param pattern The URI pattern used to map calls.
    * @return The builder for the created node.
    */
   public ExtractChainletBuilder attachExtract(String pattern)
   {
      ExtractChainlet node = new ExtractChainlet(getNode().getOwner());
      getNode().attach(pattern, node);
      return new ExtractChainletBuilder(this, node);
   }

   /**
    * Attaches a File Restlet.
    * @param pattern The URI pattern used to map calls.
    * @param filePath The file's path.
    * @param mediaType The file's media type.
    */
   public RestletBuilder attachFile(String pattern, String filePath, MediaType mediaType)
   {
      FileRestlet node = new FileRestlet(getNode().getOwner(), filePath, mediaType);
      getNode().attach(pattern, node);
      return new RestletBuilder(this, node);
   }

   /**
    * Attaches an Guard Chainlet.
    * @param pattern The URI pattern used to map calls.
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
      return new GuardChainletBuilder(this, node);
   }
   
   /**
    * Attaches a Host Maplet. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostMapletBuilder attachHost(int port)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), port);
      node.setMode(AttachmentMode.MAPLET);
      getNode().attach(node.getPattern(), node);
      return new HostMapletBuilder(this, node);
   }
   
   /**
    * Attaches a Host Maplet. 
    * @param domain The domain name. 
    * @return The builder for the created node.
    */
   public HostMapletBuilder attachHost(String domain)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), domain);
      node.setMode(AttachmentMode.MAPLET);
      getNode().attach(node.getPattern(), node);
      return new HostMapletBuilder(this, node);
   }
   
   /**
    * Attaches a Host Maplet. 
    * @param domain The domain name. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostMapletBuilder attachHost(String domain, int port)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), domain, port);
      node.setMode(AttachmentMode.MAPLET);
      getNode().attach(node.getPattern(), node);
      return new HostMapletBuilder(this, node);
   }

   /**
    * Attaches Log Chainlet using the default format.<br/>
    * Default format using <a href="http://analog.cx/docs/logfmt.html">Analog syntax</a>: %Y-%m-%d\t%h:%n:%j\t%j\t%r\t%u\t%s\t%j\t%B\t%f\t%c\t%b\t%q\t%v\t%T
    * @param pattern The URI pattern used to map calls.
    * @param logName The log name to used in the logging.properties file.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachLog(String pattern, String logName)
   {
      LogChainlet node = new LogChainlet(getNode().getOwner(), logName);
      getNode().attach(pattern, node);
      return new ChainletBuilder(this, node);
   }

   /**
    * Attaches Log Chainlet.
    * @param pattern The URI pattern used to map calls.
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
      return new ChainletBuilder(this, node);
   }

   /**
    * Attaches a Maplet. 
    * @param pattern The URI pattern used to map calls.
    * @return The builder for the created node.
    */
   public MapletBuilder attachMaplet(String pattern)
   {
      Maplet node = new DefaultMaplet(getNode().getOwner());
      getNode().attach(pattern, node);
      return new MapletBuilder(this, node);
   }

   /**
    * Attaches a Redirect Restlet.
    * @param pattern The URI pattern used to map calls.
    * @param targetPattern The pattern to build the target URI.
    * @param mode The redirection mode.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirectRestlet(String pattern, String targetPattern, int mode)
   {
      RedirectRestlet node = new RedirectRestlet(getNode().getOwner(), targetPattern, mode);
      getNode().attach(pattern, node);
      return new RestletBuilder(this, node);
   }

   /**
    * Attaches a Redirect Restlet in the Connector mode.
    * @param pattern The URI pattern used to map calls.
    * @param targetPattern The pattern to build the target URI.
    * @param connectorName The connector Name.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirectRestlet(String pattern, String targetPattern, String connectorName)
   {
      RedirectRestlet node = new RedirectRestlet(getNode().getOwner(), targetPattern, connectorName);
      getNode().attach(pattern, node);
      return new RestletBuilder(this, node);
   }
   
   /**
    * Attaches a Status Chainlet.
    * @param pattern The URI pattern used to map calls.
    * @param overwrite Indicates whether an existing representation should be overwritten.
    * @param email Email address of the administrator to contact in case of error.
    * @param homeURI The home URI to display in case the user got a "not found" exception.
    * @return The builder for the target.
    */
   public ChainletBuilder attachStatus(String pattern, boolean overwrite, String email, String homeURI)
   {
      StatusChainlet node = new StatusChainlet(getNode().getOwner(), overwrite, email, homeURI);
      getNode().attach(pattern, node);
      return new ChainletBuilder(this, node);
   }

}
