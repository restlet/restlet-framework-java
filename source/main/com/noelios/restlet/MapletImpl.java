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
import org.restlet.Restlet;
import org.restlet.RestletException;
import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.RestletTarget;

/**
 * Implementation of a mapper of calls to other restlets.
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
    * Adds a mapping with a path beginning with the given pattern.
    * @param pathPattern The path pattern used to match objects.
    * @param restlet The restlet.
    */
   public void attach(String pathPattern, Restlet restlet)
   {
      add(new Mapping(pathPattern, restlet));
   }

   /**
    * Adds a mapping with a path beginning with the given pattern.
    * @param pathPattern The path pattern used to match objects.
    * @param restletClass The restlet class.
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
      for(Iterator iter = iterator(); iter.hasNext();)
      {
         mapping = (Mapping)iter.next();
         if(mapping.getRestlet() == restlet) remove(mapping);
      }
   }

   /**
    * Removes all mappings to a given restlet class.
    * @param restletClass The restlet class to look for.
    */
   public void detach(Class<? extends Restlet> restletClass)
   {
      Mapping mapping;
      for(Iterator iter = iterator(); iter.hasNext();)
      {
         mapping = (Mapping)iter.next();
         if(mapping.getRestletClass() == restletClass) remove(mapping);
      }
   }

   /**
    * Handles a call to a resource or a set of resources.<br/>
    * Default behavior to be overriden: delegation to attached handlers.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(UniformCall call)
   {
      delegate(call);
   }

   /**
    * Delegates a call to attached restlets.<br/>
    * If no delegation is possible, an error status (406, not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(UniformCall call)
   {
      Mapping mapping = null;
      Matcher matcher = null;
      boolean found = false;
      String resourcePath = call.getPath(0, false);

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
         call.getPaths().set(0, restletPath);
         call.getPaths().add(0, resourcePath);

         // Updates the matches
         call.getMatches().clear();
         for(int i = 0; i < matcher.groupCount(); i++)
         {
            call.getMatches().add(matcher.group(i + 1));
         }

         // Find and prepare the call handler
         Restlet restlet = null;

         try
         {
            if(mapping.getRestlet() != null)
            {
               restlet = mapping.getRestlet();
            }
            else if(mapping.isSetContainer())
            {
               restlet = (Restlet)mapping.getRestletConstructor().newInstance(getContainer());
            }
            else
            {
               restlet = (Restlet)mapping.getRestletClass().newInstance();
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
         restlet.handle(call);
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
 * Represents a mapping between a path pattern and a restlet.
 * @see java.util.regex.Pattern
 */
class Mapping extends RestletTarget
{
   /** The path pattern. */
   Pattern pathPattern;

   /**
    * Constructor.
    * @param pathPattern The path pattern.
    * @param restlet The restlet.
    */
   public Mapping(String pathPattern, Restlet restlet)
   {
      super(restlet);
      this.pathPattern = Pattern.compile(pathPattern, Pattern.CASE_INSENSITIVE);
   }

   /**
    * Constructor.
    * @param pathPattern The path pattern.
    * @param restletClass The restlet class.
    */
   Mapping(String pathPattern, Class<? extends Restlet> restletClass)
   {
      super(restletClass);
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
