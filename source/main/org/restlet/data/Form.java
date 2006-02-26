/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import java.util.List;
import java.util.Map;

/**
 * Representation of a Web form containing submitted parameters.
 */
public interface Form extends Data
{
   /**
    * Returns the modifiable list of parameters.<br/>
    * Note that multiple parameters with the the same name may occur in any order in the list returned.
    * @return The modifiable list of parameters.
    */
   public List<Parameter> getParameters();

   /**
    * Adds a new parameter.
    * @param name The parameter name.
    * @param value The parameter value.
    */
   public void addParameter(String name, String value);

   /**
    * Removes parameters with a given name.
    * @param name The name of the parameters to remove.
    */
   public void removeParameters(String name);
   
   /**
    * Gets the list of parameters with the given name.
    * @param name The parameter name to match.
    * @return The list of parameters.
    */
   public List<Parameter> getParameters(String name);

   /**
    * Gets the first parameter with the given name.
    * @param name The parameter name to match.
    * @return The parameter value.
    */
   public Parameter getFirstParameter(String name);

   /**
    * Gets the parameters whose name is a key in the given map.<br/>
    * If a matching parameter is found, its value is put in the map.<br/>
    * If multiple values are found, a list is created and set in the map.
    * @param parameters The parameters map controlling the reading.
    */
   public void getParameters(Map<String, Object> parameters);

   /**
    * Returns the formatted query corresponding to the current list of parameters.
    * @return The formatted query.
    */
   public String getQuery();

   /**
    * Returns the formatted query corresponding to the current list of parameters.
    * @return The formatted query.
    */
   public Representation getRepresentation();

}
