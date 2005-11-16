/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

import java.util.List;

/**
 * Metadata preference definition.
 */
public interface Preference extends ControlData
{
   /**
    * Returns the metadata associated with this preference.
    * @return The metadata associated with this preference.
    */
   public Metadata getMetadata();

   /**
    * Returns the quality/preference level.
    * @return The quality/preference level.
    */
   public float getQuality();

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




