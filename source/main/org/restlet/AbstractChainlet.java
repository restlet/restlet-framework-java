/*
 * Copyright 2005-2006 Noelios Consulting.
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

import org.restlet.component.Component;

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
    * @param parent The parent component.
    */
   public AbstractChainlet(Component parent)
   {
      super(parent);
      this.delegate = Factory.getInstance().createChainlet(parent);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param target The target instance to attach.
    * @return The current Chainlet.
    */
   public Chainlet attach(Restlet target)
   {
      delegate.attach(target);
      return this;
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @return The current Chainlet.
    */
   public Chainlet attach(Class<? extends Restlet> targetClass)
   {
      delegate.attach(targetClass);
      return this;
   }

   /**
    * Indicates if a target Restlet instance or class has been attached.
    * @return True if a target Restlet instance or class has been attached.
    */
   public boolean hasTarget()
   {
   	return delegate.hasTarget();
   }

   /**
    * Detaches the current target.
    * @return The current Chainlet.
    */
   public Chainlet detach()
   {
      delegate.detach();
      return this;
   }

   /**
    * Handles a call to a resource or a set of resources.<br/>
    * Default behavior to be overriden: delegation to one of the attached targets.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      delegate.handle(call);
   }

}
