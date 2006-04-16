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

package com.noelios.restlet.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.RestletCall;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.data.Statuses;

/**
 * Restlet target used for Maplet or Chainlet attachments.
 */
public class RestletTarget
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.util.UniformTarget");

   /** The Restlet handler. */
   protected Restlet restlet;

   /** The Restlet class. */
   protected Class<? extends Restlet> restletClass;

   /** The Restlet constructor. */
   protected Constructor restletConstructor;

   /** The container class to set in the constructor. */
   protected Class containerClass;

   /** Indicates if the parent component can be set in the constructor. */
   protected boolean setParent;

   /**
    * Constructor.
    * @param restlet The Restlet handler.
    */
   public RestletTarget(Restlet restlet)
   {
      this.restlet = restlet;
      this.restletClass = null;
      this.restletConstructor = null;
      this.setParent = false;
   }

   /**
    * Constructor.
    * @param restletClass The Restlet class.
    */
   public RestletTarget(Class<? extends Restlet> restletClass)
   {
      this.restlet = null;
      this.restletClass = restletClass;
      this.setParent = false;

      // Try to find a constructor that accepts a RestletContainer parameter
      Constructor[] constructors = restletClass.getConstructors();
      Class[] parameters;

      for(int i = 0; (this.restletConstructor == null) && (i < constructors.length); i++)
      {
         parameters = constructors[i].getParameterTypes();

         if(parameters.length == 1)
         {
            if(Component.class.isAssignableFrom(parameters[0]))
            {
               this.restletConstructor = constructors[i];
               this.setParent = true;
            }
         }
      }

      if(this.restletConstructor == null)
      {
         // Try to find an empty constructor
         try
         {
            this.restletConstructor = restletClass.getConstructor(new Class[]{});
         }
         catch(NoSuchMethodException nsme)
         {
         }
      }
   }

   /**
    * Handles an uniform call.
    * @param call The call to handle.
    * @param parent The parent component.
    */
   public void handle(RestletCall call, Component parent)
   {
      // Find and prepare the call restlet
      Restlet handler = null;

      try
      {
         if(getRestlet() != null)
         {
            handler = getRestlet();
         }
         else if(isSetParent())
         {
            handler = (Restlet)getRestletConstructor().newInstance(parent);
         }
         else
         {
            handler = (Restlet)getRestletClass().newInstance();
         }
      }
      catch(InstantiationException ie)
      {
         call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
         logger.log(Level.SEVERE, "Handler can't be instantiated", ie);
      }
      catch(IllegalAccessException iae)
      {
         call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
         logger.log(Level.SEVERE, "Handler can't be accessed", iae);
      }
      catch(InvocationTargetException ite)
      {
         call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
         logger.log(Level.SEVERE, "Handler can't be invoked", ite);
      }

      if(handler != null)
      {
         // Check if the restlet needs to be started
         if(handler.isStopped()) 
         {
            try
            {
               handler.start();
            }
            catch(Exception e)
            {
               call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
               logger.log(Level.SEVERE, "Handler can't be started", e);
            }
         }
         
         // Handle the call
         handler.handle(call);
      }
      else
      {
         call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
         logger.log(Level.WARNING, "Handler can't be invoked");
      }
   }
   /**
    * Returns the Restlet instance.
    * @return The Restlet instance.
    */
   public Restlet getRestlet()
   {
      return this.restlet;
   }

   /**
    * Returns the Restlet class.
    * @return The Restlet class.
    */
   public Class<? extends Restlet> getRestletClass()
   {
      return this.restletClass;
   }

   /**
    * Returns the Restlet constructor.
    * @return The Restlet constructor.
    */
   public Constructor getRestletConstructor()
   {
      return this.restletConstructor;
   }

   /**
    * Returns the container class.
    * @return The container class.
    */
   public Class getContainerClass()
   {
      return this.containerClass;
   }

   /**
    * Indicates if the parent component can be set in the constructor.
    * @return True if the parent component can be set in the constructor.
    */
   public boolean isSetParent()
   {
      return this.setParent;
   }

}
