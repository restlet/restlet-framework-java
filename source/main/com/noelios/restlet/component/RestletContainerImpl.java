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

package com.noelios.restlet.component;

import org.restlet.Maplet;
import org.restlet.UniformCall;
import org.restlet.UniformInterface;
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
      this.delegate = new MapletImpl(parent);
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
    * Attaches a target instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, UniformInterface target)
   {
      delegate.attach(pathPattern, target);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pathPattern The path pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer
    * parameter).
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Class<? extends UniformInterface> targetClass)
   {
      delegate.attach(pathPattern, targetClass);
   }

   /**
    * Detaches a target instance.
    * @param target The target instance to detach.
    */
   public void detach(UniformInterface target)
   {
      delegate.detach(target);
   }

   /**
    * Detaches a target class.
    * @param targetClass The restlet class to detach.
    */
   public void detach(Class<? extends UniformInterface> targetClass)
   {
      delegate.detach(targetClass);
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      delegate.handle(call);
   }

   /**
    * Delegates a call to one of the attached targets.<br/>
    * If no delegation is possible, a 404 error status (Client error, Not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(UniformCall call)
   {
      return delegate.delegate(call);
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
