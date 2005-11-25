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

package com.noelios.restlet.component;

import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;

import com.noelios.restlet.MapletImpl;

/**
 * Component acting as a container for call handlers named restlets.
 * Calls are first intercepted by the container which can do various checks before effectively
 * delegating it to one of the registered root maplets or restlets.
 */
public class RestletContainerImpl extends OriginServerImpl implements RestletContainer
{
   /** Delegate maplet handling root restlets. */
   protected Maplet delegate;

   /**
    * Constructor.
    * @param name The unique name of the container.
    */
   public RestletContainerImpl(String name)
   {
      super(name);
      this.delegate = createMapletDelegate();
   }

   /**
    * Returns a new maplet acting as a delegate for maplets.
    * Developers who need to extend the default maplets should override it.
    * @return A new maplet.
    */
   public Maplet createMapletDelegate()
   {
      return new MapletImpl(this);
   }

   /**
    * Returns the container.
    * @return The container.
    */
   public RestletContainer getContainer()
   {
      return this;
   }

   /**
    * Attaches a restlet instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param restlet     The restlet to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Restlet restlet)
   {
      delegate.attach(pathPattern, restlet);
   }

   /**
    * Attaches a restlet class. A new instance will be created for each call.
    * @param pathPattern   The path pattern used to map calls.
    * @param restletClass  The restlet class to attach (must have a constructor taking a RestletContainer parameter).
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Class<? extends Restlet> restletClass)
   {
      delegate.attach(pathPattern, restletClass);
   }

   /**
    * Detaches a restlet instance.
    * @param restlet The restlet to detach.
    */
   public void detach(Restlet restlet)
   {
      delegate.detach(restlet);
   }

   /**
    * Detaches a restlet class.
    * @param restletClass  The restlet class to detach.
    */
   public void detach(Class<? extends Restlet> restletClass)
   {
      delegate.detach(restletClass);
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(RestletCall call) throws RestletException
   {
      delegate.handle(call);
   }

   /**
    * Delegates a call to attached restlets.
    * @param call The call to delegate.
    */
   public void delegate(RestletCall call) throws RestletException
   {
      delegate.delegate(call);
   }

   /**
    * Asks one of the root restlets to handle a call.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      try
      {
         handle(Manager.createRestletCall(call));
      }
      catch(RestletException re)
      {
         re.printStackTrace();
      }
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Restlet container";
   }

}



