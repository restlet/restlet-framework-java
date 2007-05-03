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

/**
 * Factory implemented by Restlet implementations.
 */
public interface Factory
{
   /**
    * Creates a new uniform call.
    * @return A new uniform call.
    */
   public UniformCall createCall();

   /**
    * Creates a delegate chainlet.
    * @param container The restlet container.
    * @return A new chainlet.
    */
   public Chainlet createChainlet(RestletContainer container);

   /**
    * Creates a new character set from its standard name.
    * @param name The standard character set name.
    * @return The new character set.
    */
   public CharacterSet createCharacterSet(String name);

   /**
    * Returns a new cookie.
    * @param name The name.
    * @param value The value.
    * @return A new cookie.
    */
   public Cookie createCookie(String name, String value);

   /**
    * Returns a new cookie setting.
    * @param name The name.
    * @param value The value.
    * @return A new cookie setting.
    */
   public CookieSetting createCookieSetting(String name, String value);

   /**
    * Creates a new encoding from its standard name.
    * @param name The standard encoding name.
    * @return The new encoding.
    */
   public Encoding createEncoding(String name);

   /**
    * Creates a new form able to process the given form content.
    * @param content The form content to process.
    * @return A new form with the given content.
    * @throws IOException
    */
   public Form createForm(Representation content) throws IOException;

   /**
    * Create a new HTTP client connector.
    * @param name The unique connector name.
    * @return The new HTTP client.
    */
   public HttpClient createHttpClient(String name);

   /**
    * Create a new HTTP server connector.
    * @param name The unique connector name.
    * @param target The target handler.
    * @param protocolVariant The protocol variant (HTTP or HTTPS or AJP).
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new HTTP server.
    */
   public HttpServer createHttpServer(String name, UniformInterface target, int protocolVariant, String address, int port);

   /**
    * Creates a new language from its standard name.
    * @param name The standard language name.
    * @return The new language.
    */
   public Language createLanguage(String name);

   /**
    * Creates a delegate maplet.
    * @param container The restlet container.
    * @return A new maplet.
    */
   public Maplet createMaplet(RestletContainer container);

   /**
    * Creates a new media type from its standard name.
    * @param name The standard media type name.
    * @return The new media type.
    */
   public MediaType createMediaType(String name);

   /**
    * Creates a new method from its standard name.
    * @param name The standard method name.
    * @return The new method.
    */
   public Method createMethod(String name);

   /**
    * Creates a new parameter.
    * @param name The parameter's name.
    * @param value The parameter's value.
    * @return The new parameter.
    */
   public Parameter createParameter(String name, String value);

   /**
    * Creates a new reference from a URI reference.
    * @param uriReference The URI reference.
    * @return The new URI reference.
    */
   public Reference createReference(String uriReference);

   /**
    * Creates a delegate restlet server.
    * @param parent The parent restlet server.
    * @param name The server's name.
    * @return The new restlet server.
    */
   public RestletServer createRestletServer(RestletServer parent, String name);

   /**
    * Creates a delegate restlet container.
    * @param parent The parent restlet container.
    * @param name The container's name.
    * @return The new restlet container.
    */
   public RestletContainer createRestletContainer(RestletContainer parent, String name);

   /**
    * Creates a new status from its standard code.
    * @param code The standard status code.
    * @return The new status.
    */
   public Status createStatus(int code);

   /**
    * Creates a new tag.
    * @param name The tag name.
    * @return The new tag.
    */
   public Tag createTag(String name);
}
