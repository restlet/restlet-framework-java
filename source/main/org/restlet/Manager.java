/*
 * Copyright 2005-2006 Jerome LOUVEL
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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.component.Component;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.*;

/**
 * The main manager that also acts as an object factory. Façade around the current Restlet API implementation.
 */
public class Manager
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("org.restlet.Manager");

   public static final String VERSION_LONG = "1.0 beta 10";
   public static final String VERSION_SHORT = "1.0b10";

   /** The registered factory. */
   protected static Factory registeredFactory = null;

   /**
    * Creates a new uniform call.
    * @return A new uniform call.
    */
   public static RestletCall createCall()
   {
      return getRegisteredFactory().createCall();
   }

   /**
    * Creates a challenge response for a specific scheme (ex: HTTP BASIC authentication)
    * using a login and a password as the credentials.
    * @param scheme The challenge scheme to use.
    * @param userId The user identifier to use.
    * @param password The user password.
    * @return The challenge response to attach to an uniform call.
    */
   public static ChallengeResponse createChallengeResponse(ChallengeScheme scheme, String userId, String password)
   {
      return getRegisteredFactory().createChallengeResponse(scheme, userId, password);
   }

   /**
    * Creates a new character set from its standard name.
    * @param name The standard character set name.
    * @return The new character set.
    */
   public static CharacterSet createCharacterSet(String name)
   {
      return getRegisteredFactory().createCharacterSet(name);
   }

   /**
    * Create a new client connector for a given protocol.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @return The new client connector.
    */
   public static Client createClient(Protocol protocol, String name)
   {
      return getRegisteredFactory().createClient(protocol, name);
   }

   /**
    * Creates a delegate Chainlet for internal usage by the AbstractChainlet.<br/>
    * If you need a Chainlet for your application, you should be subclassing the AbstractChainlet instead. 
    * @param parent The parent component.
    * @return A new Chainlet.
    */
   public static Chainlet createDelegateChainlet(Component parent)
   {
      return getRegisteredFactory().createDelegateChainlet(parent);
   }

   /**
    * Creates a delegate Maplet for internal usage by the DefaultMaplet.<br/>
    * If you need a Maplet for your application, you should be using the DefaultMaplet instead. 
    * @param parent The parent component.
    * @return A new Maplet.
    */
   public static Maplet createDelegateMaplet(Component parent)
   {
      return getRegisteredFactory().createDelegateMaplet(parent);
   }

   /**
    * Creates a new encoding from its standard name.
    * @param name The standard encoding name.
    * @return The new encoding.
    */
   public static Encoding createEncoding(String name)
   {
      return getRegisteredFactory().createEncoding(name);
   }

   /**
    * Creates an empty form.
    * @return A new form.
    */
   public static Form createForm()
   {
      return getRegisteredFactory().createForm();
   }

   /**
    * Creates a new language from its standard name.
    * @param name The standard language name.
    * @return The new language.
    */
   public static Language createLanguage(String name)
   {
      return getRegisteredFactory().createLanguage(name);
   }

   /**
    * Creates a new media type from its standard name.
    * @param name The standard media type name.
    * @return The new media type.
    */
   public static MediaType createMediaType(String name)
   {
      return getRegisteredFactory().createMediaType(name);
   }

   /**
    * Creates a new method from its standard name.
    * @param name The standard method name.
    * @return The new method.
    */
   public static Method createMethod(String name)
   {
      return getRegisteredFactory().createMethod(name);
   }

   /**
    * Creates a new representation metadata.
    * @param mediaType The representation mediatype.
    */
   public static RepresentationMetadata createRepresentationMetadata(MediaType mediaType)
   {
      return getRegisteredFactory().createRepresentationMetadata(mediaType);
   }

   /**
    * Create a new server connector for a given protocol.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param target The target Restlet.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new server connector.
    */
   public static Server createServer(Protocol protocol, String name, Restlet target, String address, int port)
   {
      return getRegisteredFactory().createServer(protocol, name, target, address, port);
   }

   /**
    * Creates a new status from its standard code.
    * @param code The standard status code.
    * @return The new status.
    */
   public static Status createStatus(int code)
   {
      return getRegisteredFactory().createStatus(code);
   }

   /**
    * Creates a new tag.
    * @param name The tag name.
    * @return The new tag.
    */
   public static Tag createTag(String name)
   {
      return getRegisteredFactory().createTag(name);
   }

   /**
    * Returns the registered factory.
    * @return The registered factory.
    */
   protected static Factory getRegisteredFactory()
   {
      Factory result = registeredFactory;

      if(result == null)
      {
         // Find the factory class name
         String factoryClassName = null;

         // Find the factory class name
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         URL configURL = cl.getResource("meta-inf/services/org.restlet.Factory");
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
               registeredFactory = (Factory)Class.forName(factoryClassName).newInstance();
               result = registeredFactory;
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
    * Register a new factory.
    * @param factory The factory to register.
    */
   public static void registerFactory(Factory factory)
   {
      registeredFactory = factory;
   }

}
