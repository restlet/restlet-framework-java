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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.restlet.AbstractRestlet;
import org.restlet.Maplet;
import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.data.Statuses;

/**
 * Implementation of a mapper of calls to attached Restlets. 
 */
public class MapletImpl extends AbstractRestlet implements Maplet
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The list of mappings. */
   protected List<RestletMapping> mappings;

   /**
    * Constructor.
    * @param container The Restlet container.
    */
   public MapletImpl(Component container)
   {
      super(container);
      this.mappings = null;
   }

   /**
    * Returns the list of mappings.
    * @return The list of mappings.
    */
   private List<RestletMapping> getMappings()
   {
      if(this.mappings == null) this.mappings = new ArrayList<RestletMapping>();
      return this.mappings;
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param pattern The URI pattern used to map calls.
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public synchronized void attach(String pattern, Restlet target)
   {
      getMappings().add(new RestletMapping(pattern, target));
   }

   /**
    * Attaches at a specific a target instance shared by all calls.
    * @param pattern The URI pattern used to map calls.
    * @param target The target instance to attach.
    * @param override Indicates if this attachment should have a higher priority that existing ones.
    * @see java.util.regex.Pattern
    */
   public synchronized void attach(String pattern, Restlet target, boolean override)
   {
      getMappings().add(0, new RestletMapping(pattern, target));
   }
   
   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pattern The URI pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @see java.util.regex.Pattern
    */
   public synchronized void attach(String pattern, Class<? extends Restlet> targetClass)
   {
      getMappings().add(new RestletMapping(pattern, targetClass));
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pattern The URI pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @param override Indicates if this attachment should have a higher priority that existing ones.
    * @see java.util.regex.Pattern
    */
   public synchronized void attach(String pattern, Class<? extends Restlet> targetClass, boolean override)
   {
      getMappings().add(0, new RestletMapping(pattern, targetClass));
   }

   /**
    * Detaches a target instance.
    * @param target The target instance to detach.
    */
   public synchronized void detach(Restlet target)
   {
      RestletMapping mapping;
      for(Iterator<RestletMapping> iter = getMappings().iterator(); iter.hasNext();)
      {
         mapping = iter.next();
         if(mapping.getRestlet() == target) iter.remove();
      }

      if(getMappings().size() == 0) this.mappings = null;
   }

   /**
    * Detaches a target class.
    * @param targetClass The target class to detach.
    */
   public synchronized void detach(Class<? extends Restlet> targetClass)
   {
      RestletMapping mapping;
      for(Iterator<RestletMapping> iter = getMappings().iterator(); iter.hasNext();)
      {
         mapping = iter.next();
         if(mapping.getRestletClass() == targetClass) iter.remove();
      }

      if(getMappings().size() == 0) this.mappings = null;
   }

   /**
    * Detaches all targets.
    */
   public synchronized void detachAll()
   {
   	getMappings().clear();
   }

   /**
    * Handles a call to a resource or a set of resources.<br/>
    * Default behavior to be overriden: delegation to attached handlers.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      delegate(call);
   }

   /**
    * Delegates a call to one of the attached targets.<br/>
    * If no delegation is possible, a 404 error status (Client error, Not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(Call call)
   {
      RestletMapping mapping = null;
      Matcher matcher = null;
      boolean found = false;
      String resourcePath = call.getResourcePath();

      // Match the path in the call context with one of the child restlet
      synchronized(this)
      {
	      for(Iterator<RestletMapping> iter = getMappings().iterator(); !found && iter.hasNext();)
	      {
	         mapping = iter.next();
	         matcher = mapping.getPattern().matcher(resourcePath);
	         found = matcher.lookingAt();
	      }
      }

      if(found)
      {
         // Updates the paths
         String oldRestletPath = call.getContextPath();
         String restletPath = resourcePath.substring(0, matcher.end());

         if(oldRestletPath == null)
         {
            call.setContextPath(restletPath);
         }
         else
         {
            call.setContextPath(oldRestletPath + restletPath);
         }

         // Updates the matches
         call.getContextMatches().clear();
         for(int i = 0; i < matcher.groupCount(); i++)
         {
            call.getContextMatches().add(matcher.group(i + 1));
         }

         // Invoke the call restlet
         mapping.handle(call, getOwner());
      }
      else
      {
         // No delegateMaplet was found
         call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }

      return found;
   }

}
