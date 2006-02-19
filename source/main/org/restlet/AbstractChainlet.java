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
 * Abstract Chainlet that can be easily subclassed.
 * @see <a href="http://www.restlet.org/tutorial#part07">Tutorial: Chainlets and call logging</a>
 */
public abstract class AbstractChainlet extends AbstractRestlet implements Chainlet
{
   /** Delegate Chainlet actually implementing the Chainlet methods. */
   protected Chainlet delegate;

   /**
    * Creates a new Chainlet in the given container.
    * @param container The parent container.
    */
   public AbstractChainlet(RestletContainer container)
   {
      super(container);
      this.delegate = Manager.createChainlet(container);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param target The target instance to attach.
    */
   public void attach(UniformInterface target)
   {
      delegate.attach(target);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer
    * parameter).
    */
   public void attach(Class<? extends UniformInterface> targetClass)
   {
      delegate.attach(targetClass);
   }

   /**
    * Detaches the current target.
    */
   public void detach()
   {
      delegate.detach();
   }

   /**
    * Handles a call to a resource or a set of resources.<br/>
    * Default behavior to be overriden: delegation to one of the attached targets.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      delegate.handle(call);
   }

}
