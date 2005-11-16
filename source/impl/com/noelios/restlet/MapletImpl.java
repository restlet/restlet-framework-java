/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.component.RestletContainer;
import org.restlet.data.StatusEnum;

/** 
 * Represents a list of mappings for a parent maplet or for the container itself. 
 */
public class MapletImpl extends ArrayList<Mapping> implements Maplet
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The parent container. */
   private RestletContainer container;

   /**
    * Constructor.
    * @param container The parent container.
    */
   public MapletImpl(RestletContainer container)
   {
      this.container = container;
   }
   
   /**
    * Returns the container.
    * @return The container.
    */
   public RestletContainer getContainer()
   {
      return container;
   }
   
   /**
    * Adds a mapping with a path beginning with the given pattern.
    * @param pathPattern 	The path pattern used to match objects.
    * @param restlet       The restlet.
    */
   public void attach(String pathPattern, Restlet restlet)
   {
      add(new Mapping(pathPattern, restlet));
   }

   /**
    * Adds a mapping with a path beginning with the given pattern.
    * @param pathPattern 	The path pattern used to match objects.
    * @param restletClass	The restlet class.
    */
   public void attach(String pathPattern, Class<? extends Restlet> restletClass)
   {
      add(new Mapping(pathPattern, restletClass));
   }

   /**
    * Removes all mappings to a given restlet.
    * @param restlet The restlet to look for.
    */
   public void detach(Restlet restlet)
   {
      Mapping mapping;
      for (Iterator iter = iterator(); iter.hasNext(); )
      {
         mapping = (Mapping) iter.next();
         if (mapping.getRestlet() == restlet)
            remove(mapping);
      }
   }

   /**
    * Removes all mappings to a given restlet class.
    * @param restletClass The restlet class to look for.
    */
   public void detach(Class<? extends Restlet> restletClass)
   {
      Mapping mapping;
      for (Iterator iter = iterator(); iter.hasNext(); )
      {
         mapping = (Mapping) iter.next();
         if (mapping.getRestletClass() == restletClass)
            remove(mapping);
      }
   }

   /**
    * Handles a call to a resource or a set of resources.
    * Default behavior to be overriden: delegation to attached handlers.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(RestletCall call) throws RestletException
   {
      delegate(call);
   }

   /**
    * Delegates a call to attached restlets.
    * @param call The call to delegate.
    */
   public void delegate(RestletCall call) throws RestletException
   {
      Mapping mapping = null;
      Matcher matcher = null;
      boolean found = false;
      String resourcePath = call.getPath(0, false);

      // Match the path in the call context with one of the child restlet
      for (Iterator iter = iterator(); !found && iter.hasNext(); )
      {
         mapping = (Mapping) iter.next();
         matcher = mapping.getPathPattern().matcher(resourcePath);
         found = matcher.lookingAt();
      }

      if (found)
      {
         String restletPath = resourcePath.substring(0, matcher.end());
         resourcePath = resourcePath.substring(matcher.end());
         call.getPaths().set(0, restletPath);
         call.getPaths().add(0, resourcePath);

         // Find and prepare the call handler
         Restlet restlet = null;

         try
         {
            if (mapping.getRestlet() != null)
            {
               restlet = mapping.getRestlet();
            }
            else if (mapping.isSetContainer())
            {
               restlet = (Restlet)mapping.getRestletConstructor().newInstance(
                   new Object[]
                   {
                      getContainer()
                   });
            }
            else
            {
               restlet = (Restlet)mapping.getRestletClass().newInstance();
            }
         }
         catch (InstantiationException ie)
         {
            throw new RestletException("Restlet can't be instantiated", ie);
         }
         catch (IllegalAccessException iae)
         {
            throw new RestletException("Restlet can't be accessed", iae);
         }
         catch (InvocationTargetException ite)
         {
            throw new RestletException("Restlet can't be invoked", ite);
         }

         // Handle the call
         restlet.handle(call);
      }
      else
      {
         // No delegate was found
         call.setStatus(StatusEnum.CLIENT_ERROR_NOT_FOUND);
      }
   }

}

/**
 * Represents a mapping between a path pattern and a restlet.
 * @see <{Pattern}>
 */
class Mapping
{
   /** The path pattern. */
   Pattern pathPattern;

   /** The restlet. */
   Restlet restlet;

   /** The restlet class. */
   Class<? extends Restlet> restletClass;

   /** The restlet constructor. */
   Constructor restletConstructor;

   /** Indicates if the container can be set in the constructor. */
   boolean setContainer;

   /**
    * Constructor.
    * @param pathPattern The path pattern.
    * @param restlet     The restlet.
    */
   public Mapping(String pathPattern, Restlet restlet)
   {
      this.pathPattern = Pattern.compile(pathPattern,
            Pattern.CASE_INSENSITIVE);
      this.restlet = restlet;
      this.restletClass = null;
      this.restletConstructor = null;
      this.setContainer = false;
   }

   /**
    * Constructor.
    * @param pathPattern   The path pattern.
    * @param restletClass  The restlet class.
    */
   Mapping(String pathPattern,
         Class<? extends Restlet> restletClass)
   {
      this.pathPattern = Pattern.compile(pathPattern,
            Pattern.CASE_INSENSITIVE);
      this.restlet = null;
      this.restletClass = restletClass;
      this.setContainer = false;

      // Try to find a constructor that accepts a RestletContainer parameter
      Constructor[] constructors = restletClass.getConstructors();
      Class[] parameters;

      for (int i = 0; (this.restletConstructor == null)
            && (i < constructors.length); i++)
      {
         parameters = constructors[i].getParameterTypes();

         if (parameters.length == 1)
         {
            if (RestletContainer.class.isAssignableFrom(parameters[0]))
            {
               this.restletConstructor = constructors[i];
               this.setContainer = true;
            }
         }
      }

      if (this.restletConstructor == null)
      {
         // Try to find an empty constructor
         try
         {
            this.restletConstructor = restletClass
                  .getConstructor(new Class[]
                  {});
         }
         catch (NoSuchMethodException nsme)
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
    * Returns the path pattern.
    * @return The path pattern.
    */
   public Pattern getPathPattern()
   {
      return this.pathPattern;
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
