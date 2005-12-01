/*
 * Copyright 2005 Jérôme LOUVEL
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

/**
 * Factory implemented by Restlet API implementations.
 */
public interface Factory
{
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
    * Creates a delegate maplet.
    * @param parent The parent maplet.
    * @param container The restlet container.
    * @return A new maplet.
    */
   public Maplet createMaplet(Maplet parent, RestletContainer container);

   /**
    * Returns a new restlet call wrapping a given uniform call.<br/> Developers who need to extend the
    * default restlet calls should override it.
    * @param call The uniform call to wrap.
    * @return A new restlet call.
    */
   public RestletCall createRestletCall(UniformCall call);

   /**
    * Returns a new cookie setting.
    * @param name The name.
    * @param value The value.
    * @return A new cookie setting.
    */
   public CookieSetting createCookieSetting(String name, String value);

   /**
    * Creates a new form able to process the given form content.
    * @param content The form content to process.
    * @return A new form with the given content.
    * @throws IOException
    */
   public Form createForm(Representation content) throws IOException;

   /**
    * Creates a new reference from a URI reference.
    * @param uriReference The URI reference.
    * @return The new URI reference.
    */
   public Reference createReference(String uriReference);

   /**
    * Creates a new character set from its standard name.
    * @param name The standard character set name.
    * @return The new character set.
    */
   public CharacterSet createCharacterSet(String name);

   /**
    * Creates a new uniform call.
    * @return A new uniform call.
    */
   public UniformCall createCall();

}
