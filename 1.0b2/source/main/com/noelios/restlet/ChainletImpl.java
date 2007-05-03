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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractRestlet;
import org.restlet.Chainlet;
import org.restlet.Restlet;
import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.component.RestletContainer;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.UniformTarget;

/**
 * Implementation of a chainer of calls to a target handler.
 */
public class ChainletImpl extends AbstractRestlet implements Chainlet
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.ChainletImpl");

   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The target handler. */
   private UniformTarget target;

   /**
    * Constructor.
    * @param container The restlet container.
    */
   public ChainletImpl(RestletContainer container)
   {
      super(container);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param target The target instance to attach.
    */
   public void attach(UniformInterface target)
   {
      this.target = new UniformTarget(target);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer
    * parameter).
    */
   public void attach(Class<? extends Restlet> targetClass)
   {
      this.target = new UniformTarget(targetClass);
   }

   /**
    * Detaches the current target.
    */
   public void detach()
   {
      this.target = null;
   }

   /**
    * Handles a call to a resource or a set of resources.
    * Default behavior to be overriden: delegation to the attached target.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      if(this.target != null)
      {
         // Find and prepare the call handler
         UniformInterface handler = null;

         try
         {
            if(this.target.getHandler() != null)
            {
               handler = this.target.getHandler();
            }
            else if(this.target.isSetContainer())
            {
               handler = (Restlet)this.target.getHandlerConstructor().newInstance(getContainer());
            }
            else
            {
               handler = (Restlet)this.target.getHandlerClass().newInstance();
            }
         }
         catch(InstantiationException ie)
         {
            call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
            logger.log(Level.WARNING, "Restlet can't be instantiated", ie);
         }
         catch(IllegalAccessException iae)
         {
            call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
            logger.log(Level.WARNING, "Restlet can't be accessed", iae);
         }
         catch(InvocationTargetException ite)
         {
            call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
            logger.log(Level.WARNING, "Restlet can't be invoked", ite);
         }

         // Handle the call
         handler.handle(call);
      }
      else
      {
         // No delegate was found
         call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
   }

}
