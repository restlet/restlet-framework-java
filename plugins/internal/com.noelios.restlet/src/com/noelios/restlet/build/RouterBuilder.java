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

import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.ChallengeScheme;

import com.noelios.restlet.CompressFilter;
import com.noelios.restlet.DecompressFilter;
import com.noelios.restlet.DirectoryFinder;
import com.noelios.restlet.ExtractFilter;
import com.noelios.restlet.GuardFilter;
import com.noelios.restlet.HostRouter;
import com.noelios.restlet.LogFilter;
import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.StatusFilter;

/**
 * Fluent builder for routers.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class RouterBuilder extends RestletBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public RouterBuilder(ObjectBuilder parent, Router node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public Router getNode()
   {
      return (Router)super.getNode();
   }
   
   /**
    * Attaches a Restlet.
    * @param pattern The URI pattern used to map calls.
    * @param option The Restlet to attach.
    * @return The builder for the target.
    * @see java.util.regex.Pattern
    */
   public RestletBuilder attach(String pattern, Restlet option)
   {
      getNode().getScorers().add(pattern, option);
      return Builders.buildRestlet(this, option);
   }

   /**
    * Attaches a Restlet at a specific position.
    * @param pattern The URI pattern used to map calls.
    * @param option The Restlet to attach.
    * @param index The insertion position in the list of scorers.
    * @return The builder for the target.
    * @see java.util.regex.Pattern
    */
   public RestletBuilder attach(String pattern, Restlet option, int index)
   {
      getNode().getScorers().add(pattern, option, index);
      return Builders.buildRestlet(this, option);
   }
   
   /**
    * Attaches a Filter.
    * @param pattern The URI pattern used to map calls.
    * @param option The Filter to attach.
    * @return The builder for the target.
    * @see java.util.regex.Pattern
    */
   public FilterBuilder attach(String pattern, Filter option)
   {
      getNode().getScorers().add(pattern, option);
      return Builders.buildFilter(this, option);
   }

   /**
    * Attaches a Filter.
    * @param pattern The URI pattern used to map calls.
    * @param option The Filter to attach.
    * @param index The insertion position in the list of scorers.
    * @return The builder for the target.
    * @see java.util.regex.Pattern
    */
   public FilterBuilder attach(String pattern, Filter option, int index)
   {
      getNode().getScorers().add(pattern, option, index);
      return Builders.buildFilter(this, option);
   }
   
   /**
    * Attaches a Router.
    * @param pattern The URI pattern used to map calls.
    * @param option The Router to attach.
    * @return The builder for the target.
    * @see java.util.regex.Pattern
    */
   public RestletBuilder attach(String pattern, Router option)
   {
      getNode().getScorers().add(pattern, option);
      return Builders.buildRouter(this, option);
   }

   /**
    * Attaches at a Router.
    * @param pattern The URI pattern used to map calls.
    * @param option The Router to attach.
    * @param index The insertion position in the list of scorers.
    * @return The builder for the target.
    * @see java.util.regex.Pattern
    */
   public RouterBuilder attach(String pattern, Router option, int index)
   {
      getNode().getScorers().add(pattern, option, index);
      return Builders.buildRouter(this, option);
   }

   /**
    * Attaches a Compress Filter.
    * @param pattern The URI pattern used to map calls.
    * @return The builder for the created node.
    */
   public FilterBuilder attachCompress(String pattern)
   {
      CompressFilter node = new CompressFilter(getNode().getContext());
      getNode().getScorers().add(pattern, node);
      return Builders.buildFilter(this, node);
   }

   /**
    * Attaches a Decompress Filter. Only decodes input representations before call handling.
    * @param pattern The URI pattern used to map calls.
    * @return The builder for the created node.
    */
   public FilterBuilder attachDecompress(String pattern)
   {
   	return attachDecompress(pattern, true, false);
   }

   /**
    * Attaches a Decompress Filter.
    * @param pattern The URI pattern used to map calls.
	 * @param decodeInput Indicates if the input representation should be decoded.
	 * @param decodeOutput Indicates if the output representation should be decoded.
    * @return The builder for the created node.
	 */
	public FilterBuilder attachDecompress(String pattern, boolean decodeInput, boolean decodeOutput)
	{
      DecompressFilter node = new DecompressFilter(getNode().getContext(), decodeInput, decodeOutput);
      getNode().getScorers().add(pattern, node);
      return Builders.buildFilter(this, node);
	}
   
   /**
    *	Attaches a Directory Restlet.
    * @param pattern The URI pattern used to map calls.
    * @param rootUri The directory's root URI.
    * @param indexName If no file name is specified, use the (optional) index name.
    * @return The builder for the created node.
    */
   public DirectoryFinderBuilder attachDirectory(String pattern, String rootUri, String indexName)
   {
      DirectoryFinder node = new DirectoryFinder(getNode().getContext(), rootUri, indexName);
      getNode().getScorers().add(pattern, node);
      return Builders.buildDirectory(this, node);
   }

   /**
    * Attaches an Extract Filter.
    * @param pattern The URI pattern used to map calls.
    * @return The builder for the created node.
    */
   public ExtractFilterBuilder attachExtract(String pattern)
   {
      ExtractFilter node = new ExtractFilter(getNode().getContext());
      getNode().getScorers().add(pattern, node);
      return Builders.buildExtract(this, node);
   }

   /**
    * Attaches an Guard Filter.
    * @param pattern The URI pattern used to map calls.
    * @param logName The log name to used in the logging.properties file.
    * @param authentication Indicates if the guard should attempt to authenticate the caller.
    * @param scheme The authentication scheme to use. 
    * @param realm The authentication realm.
    * @param authorization Indicates if the guard should attempt to authorize the caller.
    * @return The builder for the created node.
    */
   public GuardFilterBuilder attachGuard(String pattern, String logName, boolean authentication, ChallengeScheme scheme, String realm, boolean authorization)
   {
   	GuardFilter node = new GuardFilter(getNode().getContext(), logName, authentication, scheme, realm, authorization);
      getNode().getScorers().add(pattern, node);
      return Builders.buildGuard(this, node);
   }
   
   /**
    * Creates a host router. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(int port)
   {
      HostRouter node = new HostRouter(getNode().getContext(), port);
      node.setUsageMode(HostRouter.USAGE_ROUTING);
      return Builders.buildHost(this, node);
   }
   
   /**
    * Attaches a host router. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder attachHost(int port)
   {
   	return createHost(port).attachParent();
   }
   
   /**
    * Creates a host router. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param domain The domain name. 
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(String domain)
   {
      HostRouter node = new HostRouter(getNode().getContext(), domain);
      node.setUsageMode(HostRouter.USAGE_ROUTING);
      return Builders.buildHost(this, node);
   }
   
   /**
    * Attaches a host router. 
    * @param domain The domain name. 
    * @return The builder for the created node.
    */
   public HostRouterBuilder attachHost(String domain)
   {
   	return createHost(domain).attachParent();
   }
   
   /**
    * Creates a host router. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param domain The domain name. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(String domain, int port)
   {
      HostRouter node = new HostRouter(getNode().getContext(), domain, port);
      node.setUsageMode(HostRouter.USAGE_ROUTING);
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
   	return createHost(domain, port).attachParent();
   }

   /**
    * Attaches Log Filter using the default format.<br/>
    * Default format using <a href="http://analog.cx/docs/logfmt.html">Analog syntax</a>: %Y-%m-%d\t%h:%n:%j\t%j\t%r\t%u\t%s\t%j\t%B\t%f\t%c\t%b\t%q\t%v\t%T
    * @param pattern The URI pattern used to map calls.
    * @param logName The log name to used in the logging.properties file.
    * @return The builder for the created node.
    */
   public FilterBuilder attachLog(String pattern, String logName)
   {
      LogFilter node = new LogFilter(getNode().getContext(), logName);
      getNode().getScorers().add(pattern, node);
      return Builders.buildFilter(this, node);
   }

   /**
    * Attaches Log Filter.
    * @param pattern The URI pattern used to map calls.
    * @param logName The log name to used in the logging.properties file.
    * @param logFormat The log format to use.
    * @return The builder for the created node.
    * @see com.noelios.restlet.util.CallModel
    * @see com.noelios.restlet.util.StringTemplate
    */
   public FilterBuilder attachLog(String pattern, String logName, String logFormat)
   {
      LogFilter node = new LogFilter(getNode().getContext(), logName, logFormat);
      getNode().getScorers().add(pattern, node);
      return Builders.buildFilter(this, node);
   }

   /**
    * Attaches a Path Router. 
    * @param pattern The URI pattern used to map calls.
    * @return The builder for the created node.
    */
   public RouterBuilder attachRouter(String pattern)
   {
   	Router node = new Router(getNode().getContext());
      getNode().getScorers().add(pattern, node);
      return Builders.buildRouter(this, node);
   }

   /**
    * Attaches a Redirect Restlet.
    * @param pattern The URI pattern used to map calls.
    * @param targetPattern The pattern to build the target URI.
    * @param mode The redirection mode.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirect(String pattern, String targetPattern, int mode)
   {
      RedirectRestlet node = new RedirectRestlet(getNode().getContext(), targetPattern, mode);
      getNode().getScorers().add(pattern, node);
      return Builders.buildRestlet(this, node);
   }

   /**
    * Attaches a Redirect Restlet in the Connector mode.
    * @param pattern The URI pattern used to map calls.
    * @param targetPattern The pattern to build the target URI.
    * @return The builder for the created node.
    */
   public RestletBuilder attachRedirect(String pattern, String targetPattern)
   {
      RedirectRestlet node = new RedirectRestlet(getNode().getContext(), targetPattern);
      getNode().getScorers().add(pattern, node);
      return Builders.buildRestlet(this, node);
   }
   
   /**
    * Attaches a Status Filter.
    * @param pattern The URI pattern used to map calls.
    * @param overwrite Indicates whether an existing representation should be overwritten.
    * @param email Email address of the administrator to contact in case of error.
    * @param homeURI The home URI to display in case the user got a "not found" exception.
    * @return The builder for the target.
    */
   public FilterBuilder attachStatus(String pattern, boolean overwrite, String email, String homeURI)
   {
      StatusFilter node = new StatusFilter(getNode().getContext(), overwrite, email, homeURI);
      getNode().getScorers().add(pattern, node);
      return Builders.buildFilter(this, node);
   }

}
