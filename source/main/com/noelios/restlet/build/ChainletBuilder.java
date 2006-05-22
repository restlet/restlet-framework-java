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

import org.restlet.Chainlet;
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
import com.noelios.restlet.LogChainlet;
import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.StatusChainlet;

/**
 * Fluent builder for Chainlets.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ChainletBuilder extends RestletBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped chainlet.
	 */
   public ChainletBuilder(DefaultBuilder parent, Chainlet node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public Chainlet getNode()
   {
      return (Chainlet)super.getNode();
   }

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
      return new RestletBuilder(this, target);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @return The builder for the attached target.
    */
   public DefaultBuilder attach(Class<? extends Restlet> targetClass)
   {
      getNode().attach(targetClass);
      return new DefaultBuilder(this, targetClass);
   }

   /**
    * Attaches a Compress Chainlet.
    * @return The builder for the created node.
    */
   public ChainletBuilder attachCompress()
   {
      CompressChainlet node = new CompressChainlet(getNode().getOwner());
      getNode().attach(node);
      return new ChainletBuilder(this, node);
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
      return new ChainletBuilder(this, node);
	}
   
   /**
    *	Attaches a Directory Restlet.
    * @param rootPath The directory's root path.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    * @return The builder for the created node.
    */
   public DirectoryRestletBuilder attachDirectory(String rootPath, boolean deeply, String indexName)
   {
      DirectoryRestlet node = new DirectoryRestlet(getNode().getOwner(), rootPath, deeply, indexName);
      getNode().attach(node);
      return new DirectoryRestletBuilder(this, node);
   }

   /**
    * Attaches an Extract Chainlet.
    * @return The builder for the created node.
    */
   public ExtractChainletBuilder attachExtract()
   {
      ExtractChainlet node = new ExtractChainlet(getNode().getOwner());
      getNode().attach(node);
      return new ExtractChainletBuilder(this, node);
   }

   /**
    * Attaches a File Restlet.
    * @param filePath The file's path.
    * @param mediaType The file's media type.
    */
   public RestletBuilder attachFile(String filePath, MediaType mediaType)
   {
      FileRestlet node = new FileRestlet(getNode().getOwner(), filePath, mediaType);
      getNode().attach(node);
      return new RestletBuilder(this, node);
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
      getNode().attach(node);
      return new HostMapletBuilder(this, node);
   }
   
   /**
    * Attaches a Host Maplet. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostMapletBuilder attachHost(String domain, int port)
   {
      HostMaplet node = new HostMaplet(getNode().getOwner(), port);
      getNode().attach(node);
      return new HostMapletBuilder(this, node);
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
      return new ChainletBuilder(this, node);
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
      return new ChainletBuilder(this, node);
   }

   /**
    * Attaches a Maplet. 
    * @return The builder for the created node.
    */
   public MapletBuilder attachMaplet()
   {
      Maplet node = new DefaultMaplet(getNode().getOwner());
      getNode().attach(node);
      return new MapletBuilder(this, node);
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
      return new RestletBuilder(this, node);
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
      return new RestletBuilder(this, node);
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
      return new ChainletBuilder(this, node);
   }

}
