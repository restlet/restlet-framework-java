/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

import java.io.IOException;
import java.util.Map;

import org.restlet.RestletException;

/**
 * Submission form reader.
 */
public interface FormReader
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
    * Reads the next parameter available or null.
    * @return The next parameter available or null.
    */
   public Parameter readParameter() throws RestletException;

   /** Closes the reader. */
   public void close() throws IOException;
}



