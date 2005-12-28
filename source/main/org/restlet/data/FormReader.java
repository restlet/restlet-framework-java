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

import java.io.IOException;
import java.util.Map;

/**
 * Submission form reader.
 */
public interface FormReader
{
   /**
    * Reads the parameters with the given name. If multiple values are found, a list is returned created.
    * @param name The parameter name to match.
    * @return The parameter value or list of values.
    * @throws IOException
    */
   public Object readParameter(String name) throws IOException;

   /**
    * Reads the first parameter with the given name.
    * @param name The parameter name to match.
    * @return The parameter value.
    * @throws IOException
    */
   public Parameter readFirstParameter(String name) throws IOException;

   /**
    * Reads the parameters whose name is a key in the given map. If a matching parameter is found, its value
    * is put in the map. If multiple values are found, a list is created and set in the map.
    * @param parameters The parameters map controlling the reading.
    * @throws IOException
    */
   public void readParameters(Map<String, Object> parameters) throws IOException;

   /**
    * Reads the next parameter available or null.
    * @return The next parameter available or null.
    * @throws IOException
    */
   public Parameter readNextParameter() throws IOException;

   /**
    * Closes the reader.
    * @throws IOException
    */
   public void close() throws IOException;
}
