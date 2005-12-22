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

package com.noelios.restlet.util;

import java.util.Map;
import java.util.TreeMap;

/**
 * Default template model implementation.<br/>
 * Internally based on a TreeMap.
 */
public class DefaultModel implements Model
{
   /**
    * Internal map used to support the model.
    */
   protected Map<String, String> map;

   /**
    * Constructor.
    */
   public DefaultModel()
   {
      this.map = new TreeMap<String, String>();
   }
   
   /**
    * Returns the model value for a given name. 
    * @param name The name to look-up.
    * @return The model value for the given name.
    */
   public String get(String name)
   {
      return map.get(name);
   }
   
   /**
    * Indicates if the model contains a value for a given name.
    * @param name The name to look-up.
    * @return True if the model contains a value for the given name.
    */
   public boolean contains(String name)
   {
      return map.containsKey(name);
   }

   /**
    * Puts the model value for a given name. 
    * @param name The name to look-up.
    * @param value The value to put.
    */
   public void put(String name, String value)
   {
      map.put(name, value);
   }
}
