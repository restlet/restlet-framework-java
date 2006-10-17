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

package org.restlet.spi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Client;
import org.restlet.Container;
import org.restlet.Context;
import org.restlet.Handler;
import org.restlet.Holder;
import org.restlet.Router;
import org.restlet.Scorer;
import org.restlet.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;
import org.restlet.data.Request;
import org.restlet.data.Resource;
import org.restlet.data.Response;
import org.restlet.util.ClientList;
import org.restlet.util.ScorerList;
import org.restlet.util.ServerList;

/**
 * Factory and registration service for Restlet API implementations.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class Factory
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(Factory.class.getCanonicalName());

   /** Common version info. */
   public static final String BETA_NUMBER = "19";
   public static final String VERSION_LONG = "1.0 beta " + BETA_NUMBER;
   public static final String VERSION_SHORT = "1.0b" + BETA_NUMBER;

   /** The registered factory. */
   private static Factory instance = null;

   /**
    * Returns the factory of the Restlet implementation.
    * @return The factory of the Restlet implementation.
    */
   public static Factory getInstance()
   {
      Factory result = instance;

      if(result == null)
      {
         // Find the factory class name
         String factoryClassName = null;

         // Find the factory class name
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         URL configURL = cl.getResource("META-INF/services/org.restlet.spi.Factory");
         if(configURL != null)
         {
            try
            {
               BufferedReader reader = new BufferedReader(new InputStreamReader(configURL.openStream(), "utf-8"));
               String providerName = reader.readLine();
               factoryClassName = providerName.substring(0, providerName.indexOf('#')).trim();
            }
            catch (Exception e)
            {
               // Exception during resolution
            }
         }

         if(factoryClassName == null)
         {
            logger.log(Level.SEVERE, "Unable to register the Restlet API implementation. Please check that the JAR file is in your classpath.");
         }
         else
         {
            // Instantiate the factory
            try
            {
               instance = (Factory)Class.forName(factoryClassName).newInstance();
               result = instance;
            }
            catch(Exception e)
            {
               logger.log(Level.SEVERE, "Unable to register the Restlet API implementation", e);
               throw new RuntimeException("Unable to register the Restlet API implementation");
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
    * Creates a new holder using the given context.
    * @param context The context.
    * @param next The attached handler.
    * @return The new holder.
    */
   public abstract Holder createHolder(Context context, Handler next);

   /**
    * Creates a new client connector for a given protocol.
    * @param context The context.
    * @param protocols The connector protocols.
    * @return The new client connector.
    */
   public abstract Client createClient(Context context, List<Protocol> protocols);

   /**
    * Create a new list of client connectors.
    * @param context The context.
    * @return A new list of client connectors.
    */
   public abstract ClientList createClientList(Context context);

   /**
    * Create a new list of server connectors.
    * @param context The context.
	 * @param target The target handler of added servers.
    * @return A new list of server connectors.
    */
   public abstract ServerList createServerList(Context context, Handler target);
   
   /**
    * Creates a new container.
    * @return A new container.
    */
   public abstract Container createContainer();
   
   /**
    * Creates a string-base representation.
    * @param value The represented string.
    * @param mediaType The representation's media type.
    */
   public abstract Representation createRepresentation(String value, MediaType mediaType);

   /**
    * Creates a URI-based handler attachment that will score chained instance shared by all calls.
    * The score will be proportional to the number of chararacters matched by the pattern, from the start
    * of the context resource path.
    * @param router The parent router.
    * @param uriPattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target handler to attach.
    * @see java.util.regex.Pattern
    */
   public abstract Scorer createScorer(Router router, String uriPattern, Handler target);

   /**
    * Creates a new scorer list.
	 * @param router The parent router.
    * @return The new scorer list.
    */
   public abstract ScorerList createScorerList(Router router);
   
   /**
    * Create a new server connector for internal usage by the GenericClient.
    * @param context The context.
    * @param protocols The connector protocols.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
	 * @param target The target handler.
    * @return The new server connector.
    */
   public abstract Server createServer(Context context, List<Protocol> protocols, String address, int port, Handler target);

   /**
    * Returns the best variant representation for a given resource according the the client preferences.
    * @param client The client preferences.
    * @param variants The list of variants to compare.
    * @param fallbackLanguage The language to use if no preference matches.
    * @return The best variant representation.
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public abstract Representation getBestVariant(ClientInfo client, List<Representation> variants, Language fallbackLanguage);

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
   public abstract void setResponseEntity(Request request, Response response, Resource resource, Language fallbackLanguage);

   /**
    * Sets the credentials of a challenge response using a user ID and a password.<br/>
    * @param response The challenge response to set.
    * @param userId The user identifier to use.
    * @param password The user password.
    */
   public abstract void setCredentials(ChallengeResponse response, String userId, String password);

}
