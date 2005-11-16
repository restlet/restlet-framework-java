/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
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
    * @return 				True if the parameters are identical (name and value).
    */
   public boolean equals(Parameter otherParam);
}




