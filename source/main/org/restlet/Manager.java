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

import java.io.IOException;

import org.restlet.component.RestletContainer;
import org.restlet.component.RestletServer;
import org.restlet.data.CharacterSet;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.Security;

/**
 * The main manager that also acts as an object factory. Façade around the current Restlet API implementation.
 */
public class Manager
{
   /** The registered factory. */
   protected static Factory registeredFactory = null;

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
    * Returns a new restlet call wrapping a given uniform call. Developers who need to extend the default
    * restlet calls should override it.
    * @param call The uniform call to wrap.
    * @return A new restlet call.
    */
   public static RestletCall createRestletCall(UniformCall call)
   {
      return getRegisteredFactory().createRestletCall(call);
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
    * Creates a new form able to process the given form content.
    * @param content The form content to process.
    * @return A new form with the given content.
    * @throws IOException
    */
   public static Form createForm(Representation content) throws IOException
   {
      return getRegisteredFactory().createForm(content);
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
    * Creates a new uniform call.
    * @return A new uniform call.
    */
   public static UniformCall createCall()
   {
      return getRegisteredFactory().createCall();
   }

   /**
    * Creates new security data.
    * @return New security data.
    */
   public static Security createSecurity()
   {
      return getRegisteredFactory().createSecurity();
   }

   /**
    * Register a new restlet implementation.
    * @param factory The restlet factory to register.
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
         throw new RuntimeException("No restlet factory was registered");
      }
      else
      {
         return registeredFactory;
      }
   }

}
