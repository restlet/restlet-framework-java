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

package com.noelios.restlet.component;

import org.restlet.Manager;
import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;

import com.noelios.restlet.MapletImpl;

/**
 * Component acting as a container for call handlers named restlets. Calls are first intercepted by the
 * container which can do various checks before effectively delegating it to one of the registered root
 * maplets or restlets.
 */
public class RestletContainerImpl extends ComponentImpl implements RestletContainer
{
   /** The parent container who delegates. */
   protected RestletContainer parent;

   /** Delegate maplet handling root restlets. */
   protected Maplet delegate;

   /**
    * Constructor.
    * @param parent The parent restlet container.
    * @param name The unique name of the container.
    */
   public RestletContainerImpl(RestletContainer parent, String name)
   {
      super(name);
      this.parent = parent;
      this.delegate = new MapletImpl(this, parent);
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
    * @param restlet The restlet to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Restlet restlet)
   {
      delegate.attach(pathPattern, restlet);
   }

   /**
    * Attaches a restlet class. A new instance will be created for each call.
    * @param pathPattern The path pattern used to map calls.
    * @param restletClass The restlet class to attach (must have a constructor taking a RestletContainer
    * parameter).
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
    * @param restletClass The restlet class to detach.
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
