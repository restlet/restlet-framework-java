/*
 * Copyright 2005 Jérôme LOUVEL
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

package com.noelios.restlet.data;

import org.restlet.data.Metadata;

/**
 * Default metadata implementation.
 */
public class MetadataImpl implements Metadata
{
   /** The unique name. */
   private String name;

   /**
    * Constructor.
    * @param name The unique name.
    */
   public MetadataImpl(String name)
   {
      this.name = name;
   }

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Metadata or range of metadata";
   }

   /**
    * Returns the string representation.
    * @return The string representation.
    */
   public String toString()
   {
      return name;
   }

   /**
    * Returns a hash code value for the object.
    * @return A hash code value for the object.
    */
   public int hashCode()
   {
      return getName().hashCode(); // No parameters taken into account!!
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    * @param object The reference object with which to compare.
    */
   public boolean equals(Object object)
   {
      return (object.hashCode() == this.hashCode());
   }

}
