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

import org.restlet.data.Method;

/**
 * Method to execute when handling a call.
 */
public class MethodImpl implements Method
{
   /** The technical name of the method. */
   private String name;

   /** The description of this REST element. */
   private String description;

   /** The URI of the specification describing the method. */
   private String uri;

   /**
    * Constructor.
    * @param name The technical name of the method.
    */
   public MethodImpl(String name)
   {
      this(name, null, null);
   }

   /**
    * Constructor.
    * @param name The technical name of the method.
    * @param description The description of this REST element.
    * @param uri The URI of the specification describing the method.
    */
   public MethodImpl(String name, String description, String uri)
   {
      this.name = name;
      this.description = description;
      this.uri = uri;
   }

   /**
    * Returns the technical name of the method.
    * @return The technical name of the method.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Returns the URI of the specification describing the method.
    * @return The URI of the specification describing the method.
    */
   public String getUri()
   {
      return uri;
   }

   /**
    * Indicates if the method is equal to a given one.
    * @param method The method to compare to.
    * @return True if the method is equal to a given one.
    */
   public boolean equals(Method method)
   {
      return getName().equals(method.getName());
   }

}
