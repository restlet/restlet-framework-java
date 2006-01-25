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

package com.noelios.restlet;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Chainlet;
import org.restlet.Factory;
import org.restlet.Manager;
import org.restlet.Maplet;
import org.restlet.UniformCall;
import org.restlet.UniformInterface;
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
import org.restlet.data.Representation;
import org.restlet.data.Status;
import org.restlet.data.Tag;

import com.noelios.restlet.component.RestletContainerImpl;
import com.noelios.restlet.component.RestletServerImpl;
import com.noelios.restlet.connector.HttpClientImpl;
import com.noelios.restlet.data.CharacterSetImpl;
import com.noelios.restlet.data.CookieImpl;
import com.noelios.restlet.data.CookieSettingImpl;
import com.noelios.restlet.data.EncodingImpl;
import com.noelios.restlet.data.FormImpl;
import com.noelios.restlet.data.LanguageImpl;
import com.noelios.restlet.data.MediaTypeImpl;
import com.noelios.restlet.data.MethodImpl;
import com.noelios.restlet.data.ParameterImpl;
import com.noelios.restlet.data.ReferenceImpl;
import com.noelios.restlet.data.StatusImpl;
import com.noelios.restlet.data.TagImpl;

/**
 * Noelios Restlet Engine.<br/>
 * Also acts as a factory implementation.
 */
public class Engine implements Factory
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.Engine");

   public static final String VERSION_LONG = "1.0 beta 1";
   public static final String VERSION_SHORT = "1.0b1";
   public static final String VERSION_HEADER = "Noelios-Restlet-Engine/" + VERSION_SHORT;
   
   /** 
    * The HTTP server class name.<br/>
    * Uses Jetty 5.1 extension by default. 
    */ 
   protected String httpServerClassName = "com.noelios.restlet.ext.jetty.JettyServer";
   
   /**
    * Registers the Noelios Restlet Engine
    */
   public static void register()
   {
      Manager.registerFactory(new Engine());
   }
   
   /**
    * Create a new HTTP client connector.
    * @param name The unique connector name.
    * @return The new HTTP client.
    */
   public HttpClient createHttpClient(String name)
   {
      return new HttpClientImpl(name);
   }

   /**
    * Create a new HTTP server connector.
    * @param name The unique connector name.
    * @param target The target handler.
    * @param protocolVariant The protocol variant (HTTP or HTTPS or AJP).
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new HTTP server.
    */
   public synchronized HttpServer createHttpServer(String name, UniformInterface target, int protocolVariant, String address, int port)
   {
      try
      {
         Class httpServerClass = Class.forName(httpServerClassName);
         Constructor constr = httpServerClass.getConstructor(String.class, UniformInterface.class, int.class, String.class, int.class);
         return (HttpServer)constr.newInstance(name, target, protocolVariant, address, port);
      }
      catch(Exception e)
      {
         logger.log(Level.SEVERE, "Unable to create the HTTP server", e);
         return null;
      }
   }

   /**
    * Sets the class name of the HTTP server to use in createHttpServer method.
    * @param className The class name of the HTTP server to use.
    */
   public void setHttpServerClassName(String className)
   {
      this.httpServerClassName = className;
   }
   
   /**
    * Creates a new uniform call.
    * @return A new uniform call.
    */
   public UniformCall createCall()
   {
      return new UniformCallImpl();
   }

   /**
    * Creates a delegate restlet server.
    * @param parent The parent restlet server.
    * @param name The server's name.
    * @return The new restlet server.
    */
   public RestletServer createRestletServer(RestletServer parent, String name)
   {
      return new RestletServerImpl(name);
   }

   /**
    * Creates a delegate restlet container.
    * @param parent The parent restlet container.
    * @param name The container's name.
    * @return The new restlet container.
    */
   public RestletContainer createRestletContainer(RestletContainer parent, String name)
   {
      return new RestletContainerImpl(parent, name);
   }

   /**
    * Creates a delegate maplet.
    * @param container The restlet container.
    * @return A new maplet.
    */
   public Maplet createMaplet(RestletContainer container)
   {
      return new MapletImpl(container);
   }

   /**
    * Creates a delegate chainlet.
    * @param container The restlet container.
    * @return A new chainlet.
    */
   public Chainlet createChainlet(RestletContainer container)
   {
      return new ChainletImpl(container);
   }

   /**
    * Returns a new cookie.
    * @param name The name.
    * @param value The value.
    * @return A new cookie.
    */
   public Cookie createCookie(String name, String value)
   {
      return new CookieImpl(name, value);
   }

   /**
    * Returns a new cookie setting.
    * @param name The name.
    * @param value The value.
    * @return A new cookie setting.
    */
   public CookieSetting createCookieSetting(String name, String value)
   {
      return new CookieSettingImpl(name, value);
   }

   /**
    * Creates a new form able to process the given form content.
    * @param content The form content to process.
    * @return A new form with the given content.
    */
   public Form createForm(Representation content) throws IOException
   {
      return new FormImpl(content);
   }

   /**
    * Creates a new reference from a URI reference.
    * @param uriReference The URI reference.
    * @return The new URI reference.
    */
   public Reference createReference(String uriReference)
   {
      return new ReferenceImpl(uriReference);
   }

   /**
    * Creates a new character set from its standard name.
    * @param name The standard character set name.
    * @return The new character set.
    */
   public CharacterSet createCharacterSet(String name)
   {
      return new CharacterSetImpl(name);
   }

   /**
    * Creates a new encoding from its standard name.
    * @param name The standard encoding name.
    * @return The new encoding.
    */
   public Encoding createEncoding(String name)
   {
      return new EncodingImpl(name);
   }

   /**
    * Creates a new language from its standard name.
    * @param name The standard language name.
    * @return The new language.
    */
   public Language createLanguage(String name)
   {
      return new LanguageImpl(name);
   }

   /**
    * Creates a new media type from its standard name.
    * @param name The standard media type name.
    * @return The new media type.
    */
   public MediaType createMediaType(String name)
   {
      return new MediaTypeImpl(name);
   }

   /**
    * Creates a new method from its standard name.
    * @param name The standard method name.
    * @return The new method.
    */
   public Method createMethod(String name)
   {
      return new MethodImpl(name);
   }

   /**
    * Creates a new status from its standard code.
    * @param code The standard status code.
    * @return The new status.
    */
   public Status createStatus(int code)
   {
      return new StatusImpl(code);
   }

   /**
    * Creates a new tag.
    * @param name The tag name.
    * @return The new tag.
    */
   public Tag createTag(String name)
   {
      return new TagImpl(name);
   }

   /**
    * Creates a new parameter.
    * @param name The parameter's name.
    * @param value The parameter's value.
    * @return The new parameter.
    */
   public Parameter createParameter(String name, String value)
   {
      return new ParameterImpl(name, value);
   }

}
