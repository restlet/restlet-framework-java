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

package org.restlet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Container;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.Scorer;
import org.restlet.Server;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;

/**
 * Factory and registration service for Restlet API implementations.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class Factory
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(Factory.class.getCanonicalName());

	/** Common version info. */
	public static final String BETA_NUMBER = "21";

	public static final String VERSION_LONG = "1.0 beta " + BETA_NUMBER;

	public static final String VERSION_SHORT = "1.0b" + BETA_NUMBER;

	/** The registered factory. */
	private static Factory instance = null;

	/** Provider resource. */
	private static final String providerResource = "META-INF/services/org.restlet.util.Factory";

	/** Classloader to use for dynamic class loading. */
	private static ClassLoader classloader = Factory.class.getClassLoader();

	/**
	 * Sets a new class loader to use when creating instantiating implementation classes.
	 * @param newClassloader The new class loader to use.
	 */
	public static void setClassLoader(ClassLoader newClassloader)
	{
		classloader = newClassloader;
	}

	/**
	 * Returns a the class loader to use when creating instantiating implementation classes.
	 * By default, it reused the classloader of this Factory's class.
	 */
	public static ClassLoader getClassLoader()
	{
		return classloader;
	}

	/**
	 * Returns the factory of the Restlet implementation.
	 * 
	 * @return The factory of the Restlet implementation.
	 */
	public static Factory getInstance()
	{
		Factory result = instance;

		if (result == null)
		{
			// Find the factory class name
			String factoryClassName = null;

			// Try the default classloader
			ClassLoader cl = getClassLoader();
			URL configURL = cl.getResource(providerResource);

			if (configURL == null)
			{
				// Try the current thread's classloader
				cl = Thread.currentThread().getContextClassLoader();
				configURL = cl.getResource(providerResource);
			}

			if (configURL == null)
			{
				// Try the system classloader
				cl = ClassLoader.getSystemClassLoader();
				configURL = cl.getResource(providerResource);
			}

			if (configURL != null)
			{
				BufferedReader reader = null;
				try
				{
					reader = new BufferedReader(new InputStreamReader(configURL.openStream(),
							"utf-8"));
					String providerName = reader.readLine();

					if (providerName != null)
						factoryClassName = providerName.substring(0, providerName.indexOf('#'))
								.trim();
				}
				catch (IOException e)
				{
					logger
							.log(
									Level.SEVERE,
									"Unable to register the Restlet API implementation. Please check that the JAR file is in your classpath.");
				}
				finally
				{
					if (reader != null)
					{
						try
						{
							reader.close();
						}
						catch (IOException e)
						{
							logger
									.warning("IOException encountered while closing an open BufferedReader"
											+ e.getMessage());
						}
					}

				}

				// Instantiate the factory
				try
				{
					instance = (Factory) Class.forName(factoryClassName).newInstance();
					result = instance;
				}
				catch (Exception e)
				{
					logger.log(Level.SEVERE,
							"Unable to register the Restlet API implementation", e);
					throw new RuntimeException(
							"Unable to register the Restlet API implementation");
				}
			}

		}

		return result;
	}

	/**
	 * Sets the factory of the Restlet implementation.
	 * @param factory The factory to register.
	 */
	public static void setInstance(Factory factory)
	{
		instance = factory;
	}

	/**
	 * Creates a directory resource.
	 * @param handler The parent directory handler.
	 * @param request The handled call.
	 * @return A new directory resource.
	 * @throws IOException 
	 */
	public abstract Resource createDirectoryResource(Directory handler, Request request)
			throws IOException;

	/**
	 * Creates a new helper for a given container.
	 * @param application The application to help.
	 * @param parentContext The parent context, typically the container's context.
	 * @return The new helper.
	 */
	public abstract Helper createHelper(Application application, Context parentContext);

	/**
	 * Creates a new helper for a given client connector.
	 * @param client The client to help.
	 * @return The new helper.
	 */
	public abstract Helper createHelper(Client client);

	/**
	 * Creates a new helper for a given container.
	 * @param container The container to help.
	 * @return The new helper.
	 */
	public abstract Helper createHelper(Container container);

	/**
	 * Creates a new helper for a given server connector.
	 * @param server The server to help.
	 * @return The new helper.
	 */
	public abstract Helper createHelper(Server server);

	/**
	 * Creates a URI-based Restlet attachment that will score chained instance shared by all calls.
	 * The score will be proportional to the number of chararacters matched by the pattern, from the start
	 * of the context resource path.
	 * @param router The parent router.
	 * @param uriPattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
	 * @param target The target Restlet to attach.
	 * @see java.util.regex.Pattern
	 */
	public abstract Scorer createScorer(Router router, String uriPattern, Restlet target);

	/**
	 * Returns the best variant representation for a given resource according the the client preferences.
	 * @param client The client preferences.
	 * @param variants The list of variants to compare.
	 * @param fallbackLanguage The language to use if no preference matches.
	 * @return The best variant representation.
	 * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
	 */
	public abstract Representation getBestVariant(ClientInfo client,
			List<Representation> variants, Language fallbackLanguage);

	/**
	 * Parses an URL encoded Web form.
	 * @param logger The logger to use.
	 * @param form The target form.
	 * @param webForm The posted form.
	 */
	public abstract void parse(Logger logger, Form form, Representation webForm);

	/**
	 * Parses an URL encoded query string into a given form.
	 * @param logger The logger to use.
	 * @param form The target form.
	 * @param queryString Query string.
	 */
	public abstract void parse(Logger logger, Form form, String queryString);

	/**
	 * Sets the best response entity of a given resource according to the client preferences.<br/>
	 * If no representation is found, sets the status to "Not found".<br/>
	 * If no acceptable representation is available, sets the status to "Not acceptable".<br/>
	 * @param request The request containing the client preferences.
	 * @param response The response to update with the best entity.
	 * @param resource The resource for which the best representation needs to be set.
	 * @param fallbackLanguage The language to use if no preference matches.
	 * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
	 */
	public abstract void setResponseEntity(Request request, Response response,
			Resource resource, Language fallbackLanguage);

}
