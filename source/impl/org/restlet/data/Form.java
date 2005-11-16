/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

import java.util.List;
import java.util.Map;

import org.restlet.RestletException;

/**
 * Submission form containing a list of parameters.
 */
public interface Form extends Representation
{
   /**
    * Reads the parameters with the given name.
    * If multiple values are found, a list is returned created.
    * @param name The parameter name to match.
    * @return 		The parameter value or list of values.
    */
   public Object readParameter(String name) throws RestletException;

   /**
    * Reads the parameters whose name is a key in the given map.
    * If a matching parameter is found, its value is put in the map.
    * If multiple values are found, a list is created and set in the map.
    * @param parameters The parameters map controlling the reading.
    */
   public void readParameters(Map<String, Object> parameters) throws RestletException;

   /**
    * Returns a new form reader to read the list.
    * @return A new form reader to read the list.
    */
   public FormReader getFormReader() throws RestletException;

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    */
   public List<Parameter> getParameters() throws RestletException;

}




