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

package com.noelios.restlet.impl;

import org.restlet.AbstractRestlet;
import org.restlet.Chainlet;
import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.data.Statuses;

/**
 * Implementation of a chainer of calls to a target Restlet.
 */
public class ChainletImpl extends AbstractRestlet implements Chainlet
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The target Restlet. */
   protected RestletTarget target;

   /**
    * Constructor.
    * @param parent The parent component.
    */
   public ChainletImpl(Component parent)
   {
      super(parent);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param target The target instance to attach.
    * @return The current Chainlet.
    */
   public Chainlet attach(Restlet target)
   {
      this.target = new RestletTarget(target);
      return this;
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @return The current Chainlet.
    */
   public Chainlet attach(Class<? extends Restlet> targetClass)
   {
      this.target = new RestletTarget(targetClass);
      return this;
   }

   /**
    * Indicates if a target Restlet instance or class has been attached.
    * @return True if a target Restlet instance or class has been attached.
    */
   public boolean hasTarget()
   {
   	return (this.target != null);
   }
   
   /**
    * Detaches the current target.
    * @return The current Chainlet.
    */
   public Chainlet detach()
   {
      this.target = null;
      return this;
   }

   /**
    * Handles a call to a resource or a set of resources.<br/>
    * Default behavior to be overriden: delegation to the attached target.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	// First, copy the current target to prevent to
   	// need to synchronized the rest of the call as 
   	// the target could change after the NPE test.
   	RestletTarget target = this.target;
   	
      if(target != null)
      {
         // Invoke the call restlet
         target.handle(call, getParent());
      }
      else
      {
         // No delegateMaplet was found
         call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
   }

}
