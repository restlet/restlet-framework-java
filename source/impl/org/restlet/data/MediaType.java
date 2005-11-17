/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

import java.util.List;

/**
 * The data format of a representation.
 * @see org.restlet.data.MediaTypes
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia<a/>
 */
public interface MediaType extends Metadata
{
   /**
    * Returns the main type.
    * @return The main type.
    */
   public String getMainType();

   /**
    * Returns the sub-type.
    * @return The sub-type.
    */
   public String getSubtype();

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    */
   public List<Parameter> getParameters();

   /**
    * Returns the value of a parameter with a given name.
    * @param name The name of the parameter to return.
    * @return 		The value of the parameter with a given name.
    */
   public String getParameterValue(String name);

}




