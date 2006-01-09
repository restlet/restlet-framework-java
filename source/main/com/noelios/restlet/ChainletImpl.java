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

package com.noelios.restlet;

import java.lang.reflect.InvocationTargetException;

import org.restlet.AbstractRestlet;
import org.restlet.Chainlet;
import org.restlet.Restlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.component.RestletContainer;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.RestletTarget;

/**
 * Represents a list of mappings for a parent maplet or for the container itself.
 */
public class ChainletImpl extends AbstractRestlet implements Chainlet
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The target restlet. */
   private RestletTarget target;

   /**
    * Constructor.
    * @param container The restlet container.
    */
   public ChainletImpl(RestletContainer container)
   {
      super(container);
   }

   /**
    * Adds a mapping with a path beginning with the given pattern.
    * @param restlet The restlet.
    */
   public void attach(Restlet restlet)
   {
      this.target = new RestletTarget(restlet);
   }

   /**
    * Adds a mapping with a path beginning with the given pattern.
    * @param restletClass The restlet class.
    */
   public void attach(Class<? extends Restlet> restletClass)
   {
      this.target = new RestletTarget(restletClass);
   }

   /**
    * Detaches the current target restlet.
    */
   public void detach()
   {
      this.target = null;
   }

   /**
    * Handles a call to a resource or a set of resources.
    * Default behavior to be overriden: delegation to the attached restlet.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(RestletCall call) throws RestletException
   {
      if(this.target != null)
      {
         // Find and prepare the call handler
         Restlet restlet = null;

         try
         {
            if(this.target.getRestlet() != null)
            {
               restlet = this.target.getRestlet();
            }
            else if(this.target.isSetContainer())
            {
               restlet = (Restlet)this.target.getRestletConstructor().newInstance(getContainer());
            }
            else
            {
               restlet = (Restlet)this.target.getRestletClass().newInstance();
            }
         }
         catch(InstantiationException ie)
         {
            throw new RestletException("Restlet can't be instantiated", ie);
         }
         catch(IllegalAccessException iae)
         {
            throw new RestletException("Restlet can't be accessed", iae);
         }
         catch(InvocationTargetException ite)
         {
            throw new RestletException("Restlet can't be invoked", ite);
         }

         // Handle the call
         restlet.handle(call);
      }
      else
      {
         // No delegate was found
         call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
   }

}
