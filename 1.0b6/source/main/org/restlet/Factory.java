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

import org.restlet.component.RestletContainer;
import org.restlet.component.RestletServer;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.*;

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
    * Creates a delegate Chainlet.
    * @param container The Restlet container.
    * @return A new Chainlet.
    */
   public Chainlet createChainlet(RestletContainer container);

   /**
    * Creates a challenge response for a specific scheme using a user ID and a password as the credentials.<br/>
    * @param scheme The challenge scheme to use.
    * @param userId The user identifier to use.
    * @param password The user password.
    * @return The challenge response to attach to an uniform call.
    */
   public ChallengeResponse createChallengeResponse(ChallengeScheme scheme, String userId, String password);
   
   /**
    * Creates a new character set from its standard name.
    * @param name The standard character set name.
    * @return The new character set.
    */
   public CharacterSet createCharacterSet(String name);

   /**
    * Create a new client connector for a given protocol.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @return The new client connector.
    */
   public Client createClient(Protocol protocol, String name);

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
    * Creates an empty form.
    * @return A new form.
    */
   public Form createForm();

   /**
    * Creates a new language from its standard name.
    * @param name The standard language name.
    * @return The new language.
    */
   public Language createLanguage(String name);

   /**
    * Creates a delegate Maplet.
    * @param container The Restlet container.
    * @return A new Maplet.
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
    * Creates a new representation metadata.
    * @param mediaType The representation mediatype.
    */
   public RepresentationMetadata createRepresentationMetadata(MediaType mediaType);
   
   /**
    * Creates a delegate Restlet container.
    * @param parent The parent Restlet container.
    * @param name The container's name.
    * @return The new Restlet container.
    */
   public RestletContainer createRestletContainer(RestletContainer parent, String name);

   /**
    * Creates a delegate Restlet server.
    * @param parent The parent Restlet server.
    * @param name The server's name.
    * @return The new Restlet server.
    */
   public RestletServer createRestletServer(RestletServer parent, String name);

   /**
    * Create a new server connector for a given protocol.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param target The target handler.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new server connector.
    */
   public Server createServer(Protocol protocol, String name, UniformInterface target, String address, int port);

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
