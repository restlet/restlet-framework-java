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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.Maplet;
import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.component.RestletContainer;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.UniformTarget;

/**
 * Implementation of a mapper of calls to attached handlers.
 */
public class MapletImpl extends ArrayList<Mapping> implements Maplet
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.MapletImpl");

   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The parent container. */
   private RestletContainer container;

   /**
    * Constructor.
    * @param container The restlet container.
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
    * Attaches a target instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, UniformInterface target)
   {
      add(new Mapping(pathPattern, target));
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pathPattern The path pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer
    * parameter).
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Class<? extends UniformInterface> targetClass)
   {
      add(new Mapping(pathPattern, targetClass));
   }

   /**
    * Detaches a target instance.
    * @param target The target instance to detach.
    */
   public void detach(UniformInterface target)
   {
      Mapping mapping;
      for(Iterator iter = iterator(); iter.hasNext();)
      {
         mapping = (Mapping)iter.next();
         if(mapping.getHandler() == target) remove(mapping);
      }
   }

   /**
    * Detaches a target class.
    * @param targetClass The restlet class to detach.
    */
   public void detach(Class<? extends UniformInterface> targetClass)
   {
      Mapping mapping;
      for(Iterator iter = iterator(); iter.hasNext();)
      {
         mapping = (Mapping)iter.next();
         if(mapping.getHandlerClass() == targetClass) remove(mapping);
      }
   }

   /**
    * Handles a call to a resource or a set of resources.<br/>
    * Default behavior to be overriden: delegation to attached handlers.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      delegate(call);
   }

   /**
    * Delegates a call to one of the attached targets.<br/>
    * If no delegation is possible, a 404 error status (Client error, Not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(UniformCall call)
   {
      Mapping mapping = null;
      Matcher matcher = null;
      boolean found = false;
      String resourcePath = call.getResourcePath(0, false);

      // Match the path in the call context with one of the child restlet
      for(Iterator iter = iterator(); !found && iter.hasNext();)
      {
         mapping = (Mapping)iter.next();
         matcher = mapping.getPathPattern().matcher(resourcePath);
         found = matcher.lookingAt();
      }

      if(found)
      {
         // Updates the paths
         String restletPath = resourcePath.substring(0, matcher.end());
         resourcePath = resourcePath.substring(matcher.end());
         call.getResourcePaths().set(0, restletPath);
         call.getResourcePaths().add(0, resourcePath);

         // Updates the matches
         call.getResourceMatches().clear();
         for(int i = 0; i < matcher.groupCount(); i++)
         {
            call.getResourceMatches().add(matcher.group(i + 1));
         }

         // Find and prepare the call handler
         UniformInterface target = null;

         try
         {
            if(mapping.getHandler() != null)
            {
               target = mapping.getHandler();
            }
            else if(mapping.isSetContainer())
            {
               target = (UniformInterface)mapping.getHandlerConstructor().newInstance(getContainer());
            }
            else
            {
               target = (UniformInterface)mapping.getHandlerClass().newInstance();
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
         target.handle(call);
      }
      else
      {
         // No delegate was found
         call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }

      return found;
   }

}

/**
 * Represents a mapping between a path pattern and a target uniform interface.
 * @see java.util.regex.Pattern
 */
class Mapping extends UniformTarget
{
   /** The path pattern. */
   Pattern pathPattern;

   /**
    * Constructor.
    * @param pathPattern The path pattern.
    * @param target The target interface.
    */
   public Mapping(String pathPattern, UniformInterface target)
   {
      super(target);
      this.pathPattern = Pattern.compile(pathPattern, Pattern.CASE_INSENSITIVE);
   }

   /**
    * Constructor.
    * @param pathPattern The path pattern.
    * @param targetClass The target class.
    */
   Mapping(String pathPattern, Class<? extends UniformInterface> targetClass)
   {
      super(targetClass);
      this.pathPattern = Pattern.compile(pathPattern, Pattern.CASE_INSENSITIVE);
   }

   /**
    * Returns the path pattern.
    * @return The path pattern.
    */
   public Pattern getPathPattern()
   {
      return this.pathPattern;
   }

}
