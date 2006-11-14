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

import java.io.IOException;

/**
 * Multi-usage parameter.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Parameter implements Comparable<Parameter>
{
   /** The name. */
	private String name;
	
   /** The value. */
	private String value;

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
    * @param object The object to compare.
    * @return True if the parameters are identical (name and value).
    */
   public boolean equals(Object object)
	{
		boolean result = false;
		if (object instanceof Parameter && object != null) //avoid findbugs correctness warning
		{
			final Parameter that = (Parameter) object;
			final String thisName = this.getName();
			final String thatName = that.getName();
			if(!(thisName == thatName)) { // compare names taking care of nulls
				result = (thisName != null && thisName.equals(thatName));				
			}
			//compare values if names are equal
			if (result)
			{
				final String thisValue = this.getValue();
				final String thatValue = that.getValue();
				if (!(thisValue == thatValue)) // consider equality with nulls
				{ 
					result = (thisValue != null && thisValue.equals(thatValue));
				}
			}

		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		int nameHashCode = 0;
		int valueHashCode = 0;

		if (getName() != null)
		{
			nameHashCode = getName().hashCode();
		}
		if (getValue() != null)
		{
			valueHashCode = getValue().hashCode();
		}
		// todo could we exceed INTEGER.MAX_VALUE?
		return nameHashCode + valueHashCode;
	}

   /**
    * Returns the name of this parameter.
    * @return The name of this parameter.
    */
   public String getName()
   {
   	return this.name;
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
    * Compares this object with the specified object for order.
    * @param o The object to be compared.
    * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or
    * greater than the specified object.
    */
   public int compareTo(Parameter o)
   {
      return getName().compareTo(o.getName());
   }

   /**
    * Encodes the parameter.
    * @return The encoded string.
    * @throws IOException
    */
   public String urlEncode() throws IOException
   {
      StringBuilder sb = new StringBuilder();
      urlEncode(sb);
      return sb.toString();
   }

   /**
    * Encodes the parameter and append the result to the given buffer.
    * @param buffer The buffer to append.
    * @throws IOException
    */
   public void urlEncode(Appendable buffer) throws IOException
   {
   	if(getName() != null)
   	{
      	buffer.append(Reference.encode(getName()));
            
         if(getValue() != null)
         {
         	buffer.append('=');
            buffer.append(Reference.encode(getValue()));
         }
      }
   }
   
}
