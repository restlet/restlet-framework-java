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

package com.noelios.restlet.util;

import java.lang.reflect.Constructor;

import org.restlet.UniformInterface;
import org.restlet.component.RestletContainer;

/**
 * Uniform interface target for maplet or chainlet attachments.
 */
public class UniformTarget
{
   /** The handler interface. */
   UniformInterface handler;

   /** The handler class. */
   Class<? extends UniformInterface> handlerClass;

   /** The handler constructor. */
   Constructor handlerConstructor;

   /** The container class to set in the constructor. */
   Class containerClass;

   /** Indicates if the container can be set in the constructor. */
   boolean setContainer;

   /**
    * Constructor.
    * @param handler The handler interface.
    */
   public UniformTarget(UniformInterface handler)
   {
      this.handler = handler;
      this.handlerClass = null;
      this.handlerConstructor = null;
      this.setContainer = false;
   }

   /**
    * Constructor.
    * @param handlerClass The handler class.
    */
   public UniformTarget(Class<? extends UniformInterface> handlerClass)
   {
      this.handler = null;
      this.handlerClass = handlerClass;
      this.setContainer = false;

      // Try to find a constructor that accepts a RestletContainer parameter
      Constructor[] constructors = handlerClass.getConstructors();
      Class[] parameters;

      for(int i = 0; (this.handlerConstructor == null) && (i < constructors.length); i++)
      {
         parameters = constructors[i].getParameterTypes();

         if(parameters.length == 1)
         {
            if(RestletContainer.class.isAssignableFrom(parameters[0]))
            {
               this.handlerConstructor = constructors[i];
               this.setContainer = true;
            }
         }
      }

      if(this.handlerConstructor == null)
      {
         // Try to find an empty constructor
         try
         {
            this.handlerConstructor = handlerClass.getConstructor(new Class[]{});
         }
         catch(NoSuchMethodException nsme)
         {
         }
      }
   }

   /**
    * Returns the handler interface.
    * @return The handler interface.
    */
   public UniformInterface getHandler()
   {
      return this.handler;
   }

   /**
    * Returns the handler class.
    * @return The handler class.
    */
   public Class<? extends UniformInterface> getHandlerClass()
   {
      return this.handlerClass;
   }

   /**
    * Returns the handler constructor.
    * @return The handler constructor.
    */
   public Constructor getHandlerConstructor()
   {
      return this.handlerConstructor;
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
    * Indicates if the container can be set in the constructor.
    * @return True if the container can be set in the constructor.
    */
   public boolean isSetContainer()
   {
      return this.setContainer;
   }

}
