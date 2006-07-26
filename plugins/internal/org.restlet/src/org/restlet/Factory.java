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

package org.restlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.component.Component;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

/**
 * Factory and registration service for Restlet API implementations.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class Factory
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(Factory.class.getCanonicalName());

   /** The default context client name. */
   public static final String CONTEXT_CLIENT_NAME = "ContextClient";

   /** Common version info. */
   public static final String BETA_NUMBER = "18";
   public static final String VERSION_LONG = "1.0 beta " + BETA_NUMBER;
   public static final String VERSION_SHORT = "1.0b" + BETA_NUMBER;

   /** The registered factory. */
   protected static Factory instance = null;

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
         URL configURL = cl.getResource("META-INF/services/org.restlet.Factory");
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
    * Creates a URI-based handler attachment that will score target instance shared by all calls.
    * The score will be proportional to the number of chararacters matched by the pattern, from the start
    * of the context resource path.
    * @param router The parent router.
    * @param pattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public abstract Scorer createScorer(Router router, String pattern, Restlet target);

   /**
    * Creates a call.
    * @return A call.
    */
   public abstract Call createCall();

   /**
    * Creates a new client connector for a given protocol.
    * @param protocols The connector protocols.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @return The new client connector.
    */
   public abstract Client createClient(List<Protocol> protocols, Component owner, ParameterList parameters);

   /**
    * Create a new server connector for internal usage by the GenericClient.
    * @param protocols The connector protocols.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new server connector.
    */
   public abstract Server createServer(List<Protocol> protocols, Component owner, ParameterList parameters, String address, int port);

   /**
    * Creates a string-base representation.
    * @param value The represented string.
    * @param mediaType The representation's media type.
    */
   public abstract Representation createRepresentation(String value, MediaType mediaType);

   /**
    * Parses an URL encoded Web form.
    * @param form The target form.
    * @param webForm The posted form.
    */
   public abstract void parse(Form form, Representation webForm) throws IOException;

   /**
    * Parses an URL encoded query string into a given form.
    * @param form The target form.
    * @param queryString Query string.
    */
   public abstract void parse(Form form, String queryString) throws IOException;

   /**
    * Sets the credentials of a challenge response using a user ID and a password.<br/>
    * @param response The challenge response to set.
    * @param userId The user identifier to use.
    * @param password The user password.
    */
   public abstract void setCredentials(ChallengeResponse response, String userId, String password);

}
