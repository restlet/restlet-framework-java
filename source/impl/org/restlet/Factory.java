/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.restlet;

import java.io.IOException;

import org.restlet.component.RestletContainer;
import org.restlet.component.RestletServer;
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
    * Creates a new uniform call.
    * @return A new uniform call.
    */
   public UniformCall createCall();

}
