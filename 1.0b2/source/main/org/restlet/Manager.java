/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.component.RestletContainer;
import org.restlet.component.RestletServer;
import org.restlet.connector.HttpClient;
import org.restlet.connector.HttpServer;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Tag;

/**
 * The main manager that also acts as an object factory. Façade around the current Restlet API implementation.
 */
public class Manager
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("org.restlet.Manager");

   /** Static fields. */
   public static final String PROPERTY_FACTORY = "org.restlet.impl";
   public static final String VERSION_LONG = "1.0 beta 1";
   public static final String VERSION_SHORT = "1.0b1";
   
   /** The registered factory. */
   protected static Factory registeredFactory = null;
   
   /**
    * Create a new HTTP client connector.
    * @param name The unique connector name.
    * @return The new HTTP client.
    */
   public static HttpClient createHttpClient(String name)
   {
      return getRegisteredFactory().createHttpClient(name);
   }

   /**
    * Create a new HTTP server connector.
    * @param name The unique connector name.
    * @param target The target handler.
    * @param listenerType The listener type.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new HTTP server.
    */
   public static HttpServer createHttpServer(String name, UniformInterface target, int listenerType, String address, int port)
   {
      return getRegisteredFactory().createHttpServer(name, target, listenerType, address, port);
   }

   /**
    * Creates a new uniform call.
    * @return A new uniform call.
    */
   public static UniformCall createCall()
   {
      return getRegisteredFactory().createCall();
   }

   /**
    * Creates a delegate restlet server.
    * @param parent The parent restlet server.
    * @param name The server's name.
    * @return The new restlet server.
    */
   public static RestletServer createRestletServer(RestletServer parent, String name)
   {
      return getRegisteredFactory().createRestletServer(parent, name);
   }

   /**
    * Creates a delegate restlet container.
    * @param parent The parent restlet container.
    * @param name The container's name.
    * @return The new restlet container.
    */
   public static RestletContainer createRestletContainer(RestletContainer parent, String name)
   {
      return getRegisteredFactory().createRestletContainer(parent, name);
   }

   /**
    * Creates a delegate maplet.
    * @param container The restlet container.
    * @return A new maplet.
    */
   public static Maplet createMaplet(RestletContainer container)
   {
      return getRegisteredFactory().createMaplet(container);
   }

   /**
    * Creates a delegate chainlet.
    * @param container The restlet container.
    * @return A new chainlet.
    */
   public static Chainlet createChainlet(RestletContainer container)
   {
      return getRegisteredFactory().createChainlet(container);
   }

   /**
    * Returns a new cookie.
    * @param name The name.
    * @param value The value.
    * @return A new cookie.
    */
   public static Cookie createCookie(String name, String value)
   {
      return getRegisteredFactory().createCookie(name, value);
   }

   /**
    * Returns a new cookie setting.
    * @param name The name.
    * @param value The value.
    * @return A new cookie setting.
    */
   public static CookieSetting createCookieSetting(String name, String value)
   {
      return getRegisteredFactory().createCookieSetting(name, value);
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
    * Creates a new reference from a URI reference.
    * @param uriReference The URI reference.
    * @return The new URI reference.
    */
   public static Reference createReference(String uriReference)
   {
      return getRegisteredFactory().createReference(uriReference);
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
    * Creates a new encoding from its standard name.
    * @param name The standard encoding name.
    * @return The new encoding.
    */
   public static Encoding createEncoding(String name)
   {
      return getRegisteredFactory().createEncoding(name);
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
    * Creates a new parameter.
    * @param name The parameter's name.
    * @param value The parameter's value.
    * @return The new parameter.
    */
   public static Parameter createParameter(String name, String value)
   {
      return getRegisteredFactory().createParameter(name, value);
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
    * Register a new factory.
    * @param factory The factory to register.
    */
   public static void registerFactory(Factory factory)
   {
      registeredFactory = factory;
   }

   /**
    * Returns the registered factory.
    * @return The registered factory.
    */
   protected static Factory getRegisteredFactory()
   {
      if(registeredFactory == null)
      {
         // Find the factory class name
         String factoryClassName = System.getProperty(PROPERTY_FACTORY);
         if(factoryClassName == null) factoryClassName = "com.noelios.restlet.Engine";
         
         // Instantiate the factory
         try
         {
            registeredFactory = (Factory)Class.forName(factoryClassName).newInstance();
            return registeredFactory;
         }
         catch(Exception e)
         {
            logger.log(Level.SEVERE, "Unable to register the Restlet API implementation", e);
            throw new RuntimeException("Unable to register the Restlet API implementation");
         }
      }
      else
      {
         return registeredFactory;
      }
   }

}
