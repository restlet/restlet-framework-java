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
import org.restlet.data.ChallengeScheme;

import com.noelios.restlet.CompressFilter;
import com.noelios.restlet.DecompressFilter;
import com.noelios.restlet.DirectoryHandler;
import com.noelios.restlet.ExtractFilter;
import com.noelios.restlet.GuardFilter;
import com.noelios.restlet.HostRouter;
import com.noelios.restlet.LogFilter;
import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.StatusFilter;

/**
 * Fluent builder for Filters.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class FilterBuilder extends RestletBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public FilterBuilder(ObjectBuilder parent, Filter node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public Filter getNode()
   {
      return (Filter)super.getNode();
   }

   /**
    * Attaches a target Restlet. Note that you don't need to specify an owner component 
    * for your target as the filter's owner will automatically be set for you.
    * @param target The target Restlet to attach.
    * @return The builder for the target.
    */
   public RestletBuilder attach(Restlet target)
   {
   	target.setOwner(getNode().getOwner());
      getNode().setTarget(target);
      return Builders.buildRestlet(this, target);
   }

   /**
    * Attaches a target Filter. Note that you don't need to specify an owner component 
    * for your target as the filter's owner will automatically be set for you.
    * @param target The target filter to attach.
    * @return The builder for the target.
    */
   public FilterBuilder attach(Filter target)
   {
   	target.setOwner(getNode().getOwner());
      getNode().setTarget(target);
      return Builders.buildFilter(this, target);
   }

   /**
    * Attaches a target Router. Note that you don't need to specify an owner component 
    * for your target as the filter's owner will automatically be set for you.
    * @param target The target router to attach.
    * @return The builder for the target.
    */
   public RouterBuilder attach(Router target)
   {
   	target.setOwner(getNode().getOwner());
      getNode().setTarget(target);
      return Builders.buildRouter(this, target);
   }

   /**
    * Attaches a Compress Filter.
    * @return The builder for the created node.
    */
   public FilterBuilder attachCompress()
   {
      CompressFilter node = new CompressFilter(getNode().getOwner());
      getNode().setTarget(node);
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
      DecompressFilter node = new DecompressFilter(getNode().getOwner(), decodeInput, decodeOutput);
      getNode().setTarget(node);
      return Builders.buildFilter(this, node);
	}
   
   /**
    *	Attaches a Directory Restlet.
    * @param rootUri The directory's root URI.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    * @return The builder for the created node.
    */
   public DirectoryHandlerBuilder attachDirectory(String rootUri, boolean deeply, String indexName)
   {
      DirectoryHandler node = new DirectoryHandler(getNode().getOwner(), rootUri, deeply, indexName);
      getNode().setTarget(node);
      return Builders.buildDirectory(this, node);
   }

   /**
    * Attaches an Extract Filter.
    * @return The builder for the created node.
    */
   public ExtractFilterBuilder attachExtract()
   {
      ExtractFilter node = new ExtractFilter(getNode().getOwner());
      getNode().setTarget(node);
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
   	GuardFilter node = new GuardFilter(getNode().getOwner(), logName, authentication, scheme, realm, authorization);
      getNode().setTarget(node);
      return Builders.buildGuard(this, node);
   }
   
   /**
    * Attaches a host router. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder attachHost(int port)
   {
      HostRouter node = new HostRouter(getNode().getOwner(), port);
      getNode().setTarget(node);
      return Builders.buildHost(this, node);
   }
   
   /**
    * Attaches a host router. 
    * @param domain The domain name. 
    * @return The builder for the created node.
    */
   public HostRouterBuilder attachHost(String domain)
   {
      HostRouter node = new HostRouter(getNode().getOwner(), domain);
      getNode().setTarget(node);
      return Builders.buildHost(this, node);
   }
   
   /**
    * Attaches a host router. 
    * @param domain The domain name. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder attachHost(String domain, int port)
   {
      HostRouter node = new HostRouter(getNode().getOwner(), domain, port);
      getNode().setTarget(node);
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
      LogFilter node = new LogFilter(getNode().getOwner(), logName);
      getNode().setTarget(node);
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
      LogFilter node = new LogFilter(getNode().getOwner(), logName, logFormat);
      getNode().setTarget(node);
      return Builders.buildFilter(this, node);
   }

   /**
    * Attaches a Path Router. 
    * @return The builder for the created node.
    */
   public RouterBuilder attachRouter()
   {
   	DefaultRouter node = new DefaultRouter(getNode().getOwner());
      getNode().setTarget(node);
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
      RedirectRestlet node = new RedirectRestlet(getNode().getOwner(), targetPattern, mode);
      getNode().setTarget(node);
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
      RedirectRestlet node = new RedirectRestlet(getNode().getOwner(), targetPattern, connectorName);
      getNode().setTarget(node);
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
      StatusFilter node = new StatusFilter(getNode().getOwner(), overwrite, email, homeURI);
      getNode().setTarget(node);
      return Builders.buildFilter(this, node);
   }

}
