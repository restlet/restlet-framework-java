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

package org.restlet.data;

/**
 * Method to execute when handling a call.
 */
public class DefaultMethod implements Method
{
	/** The technical name of this method. */
	protected String name;

	/** The description. */
	protected String description;

   /** The URI of the specification describing the method. */
	protected String uri;

   /**
    * Constructor.
    * @param name The technical name of the method.
    * @see org.restlet.data.Methods#create(String)
    */
   public DefaultMethod(String name)
   {
      this(name, null, null);
   }

   /**
    * Constructor.
    * @param name The technical name of the method.
    * @param description The description.
    * @see org.restlet.data.Methods#create(String)
    */
   public DefaultMethod(String name, String description)
   {
      this(name, description, null);
   }

   /**
    * Constructor.
    * @param name The technical name.
    * @param description The description.
    * @param uri The URI of the specification describing the method.
    * @see org.restlet.data.Methods#create(String)
    */
   public DefaultMethod(String name, String description, String uri)
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
   	return this.name;
   }

   /**
    * Returns the description.
    * @return The description.
    */
   public String getDescription()
   {
   	return this.description;
   }

   /**
    * Returns the URI of the specification describing the method.
    * @return The URI of the specification describing the method.
    */
   public String getUri()
   {
      return this.uri;
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
