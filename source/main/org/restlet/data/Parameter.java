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

package org.restlet.data;

/**
 * Multi-usage parameter.
 */
public interface Parameter extends Data, Comparable<Parameter>
{
   /**
    * Returns the name.
    * @return The name.
    */
   public String getName();

   /**
    * Sets the name.
    * @param name The name.
    */
   public void setName(String name);

   /**
    * Returns the value.
    * @return The value.
    */
   public String getValue();

   /**
    * Sets the value.
    * @param value The value.
    */
   public void setValue(String value);

   /**
    * Compares two parameters.
    * @param otherParam The other parameter to compare.
    * @return True if the parameters are identical (name and value).
    */
   public boolean equals(Parameter otherParam);
}
