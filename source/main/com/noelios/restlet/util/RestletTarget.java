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

import org.restlet.Restlet;
import org.restlet.component.RestletContainer;

/**
 * Represents a restlet target for maplet or chainlet attachments.
 */
public class RestletTarget
{
   /** The restlet. */
   Restlet restlet;

   /** The restlet class. */
   Class<? extends Restlet> restletClass;

   /** The restlet constructor. */
   Constructor restletConstructor;

   /** The container class to set in the constructor. */
   Class containerClass;

   /** Indicates if the container can be set in the constructor. */
   boolean setContainer;

   /**
    * Constructor.
    * @param restlet The restlet.
    */
   public RestletTarget(Restlet restlet)
   {
      this.restlet = restlet;
      this.restletClass = null;
      this.restletConstructor = null;
      this.setContainer = false;
   }

   /**
    * Constructor.
    * @param restletClass The restlet class.
    */
   public RestletTarget(Class<? extends Restlet> restletClass)
   {
      this.restlet = null;
      this.restletClass = restletClass;
      this.setContainer = false;

      // Try to find a constructor that accepts a RestletContainer parameter
      Constructor[] constructors = restletClass.getConstructors();
      Class[] parameters;

      for(int i = 0; (this.restletConstructor == null) && (i < constructors.length); i++)
      {
         parameters = constructors[i].getParameterTypes();

         if(parameters.length == 1)
         {
            if(RestletContainer.class.isAssignableFrom(parameters[0]))
            {
               this.restletConstructor = constructors[i];
               this.setContainer = true;
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
    * Returns the restlet.
    * @return The restlet.
    */
   public Restlet getRestlet()
   {
      return this.restlet;
   }

   /**
    * Returns the restlet class.
    * @return The restlet class.
    */
   public Class<? extends Restlet> getRestletClass()
   {
      return this.restletClass;
   }

   /**
    * Returns the restlet constructor.
    * @return The restlet constructor.
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
    * Indicates if the container can be set in the constructor.
    * @return True if the container can be set in the constructor.
    */
   public boolean isSetContainer()
   {
      return this.setContainer;
   }

}
