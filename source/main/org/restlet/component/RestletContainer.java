/*
 * Copyright 2005-2006 Jerome LOUVEL
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

package org.restlet.component;

import org.restlet.DefaultMaplet;
import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.RestletCall;

/**
 * Container for Maplets, Chainlets or Restlets.<br/>
 * Note that a container is a Maplet itself and can be part of a larger RestletServer.<br/>
 * Calls are first intercepted by the container which can do various checks before effectively delegating it to one of the registered root Restlets.
 * Restlet containers can also be contained within a Restlet server.
 * @see <a href="http://www.restlet.org/tutorial#part05">Tutorial: Restlets servers and containers</a>
 */
public class RestletContainer extends AbstractComponent implements Maplet
{
   /** The parent container who delegates. */
   protected Component parent;

   /** Delegate Maplet handling root Restlets. */
   protected Maplet delegate;

   /**
    * Constructor.
    * @param name The unique name of the container.
    */
   public RestletContainer(String name)
   {
   	this(null, name);
   }

   /**
    * Constructor.
    * @param parent The parent component.
    * @param name The unique name of the container.
    */
   public RestletContainer(Component parent, String name)
   {
      super(name);
      this.parent = parent;
      this.delegate = new DefaultMaplet(parent);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Restlet target)
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
   public void attach(String pathPattern, Class<? extends Restlet> targetClass)
   {
      delegate.attach(pathPattern, targetClass);
   }

   /**
    * Detaches a target instance.
    * @param target The target instance to detach.
    */
   public void detach(Restlet target)
   {
      delegate.detach(target);
   }

   /**
    * Detaches a target class.
    * @param targetClass The Restlet class to detach.
    */
   public void detach(Class<? extends Restlet> targetClass)
   {
      delegate.detach(targetClass);
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(RestletCall call)
   {
      delegate.handle(call);
   }

   /**
    * Delegates a call to one of the attached targets.<br/>
    * If no delegation is possible, a 404 error status (Client error, Not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(RestletCall call)
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
