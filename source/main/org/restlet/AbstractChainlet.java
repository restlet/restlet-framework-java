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

/**
 * Abstract chainlet that can be easily subclassed.
 */
public abstract class AbstractChainlet extends AbstractRestlet implements Chainlet
{
   /** Delegate chainlet actually implementing the Chainlet methods. */
   protected Chainlet delegate;

   /**
    * Creates a new chainlet in the given container.
    * @param container The parent container.
    */
   public AbstractChainlet(RestletContainer container)
   {
      super(container);
      this.delegate = Manager.createChainlet(container);
   }

   /**
    * Attaches a restlet instance shared by all calls.
    * @param restlet The restlet to attach.
    */
   public void attach(Restlet restlet)
   {
      delegate.attach(restlet);
   }

   /**
    * Attaches a restlet class. A new instance will be created for each call.
    * @param restletClass The restlet class to attach (must have a constructor taking a RestletContainer
    * parameter).
    */
   public void attach(Class<? extends Restlet> restletClass)
   {
      delegate.attach(restletClass);
   }

   /**
    * Detaches the current target restlet.
    */
   public void detach()
   {
      delegate.detach();
   }

   /**
    * Handles a call to a resource or a set of resources.<br/>
    * Default behavior to be overriden: delegation to attached restlet.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      delegate.handle(call);
   }

}
