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
 * Multi-usage parameter.
 */
public class Parameter implements Data, Comparable<Parameter>
{
   /** The name. */
   protected String name;

   /** The value. */
   protected String value;

   /**
    * Default constructor.
    */
   public Parameter() 
   {
      this(null, null);
   }

   /**
    * Preferred constructor.
    * @param name The name.
    * @param value The value.
    */
   public Parameter(String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   /**
    * Returns the name.
    * @return The name.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Sets the name.
    * @param name The name.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Returns the value.
    * @return The value.
    */
   public String getValue()
   {
      return this.value;
   }

   /**
    * Sets the value.
    * @param value The value.
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * Compares two parameters.
    * @param otherParam The other parameter to compare.
    * @return True if the parameters are identical (name and value).
    */
   public boolean equals(Parameter otherParam)
   {
      boolean result = true;

      if(getName() == null)
      {
         result = (otherParam.getName() == null);
      }
      else
      {
         result = getName().equals(otherParam.getName());
      }

      if(getValue() == null)
      {
         result &= (otherParam.getValue() == null);
      }
      else
      {
         result &= getValue().equals(otherParam.getValue());
      }

      return result;
   }

   /**
    * Returns a string with the name and value of the parameter.
    * @return A string with the name and value of the parameter.
    */
   public String toString()
   {
   	return getName() + ": " + getValue();
   }
   
   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Parameter";
   }

   /**
    * Compares this object with the specified object for order.
    * @param o The object to be compared.
    * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or
    * greater than the specified object.
    */
   public int compareTo(Parameter o)
   {
      return getName().compareTo(o.getName());
   }

}
