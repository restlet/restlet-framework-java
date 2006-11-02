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

package org.restlet.util;

import java.util.Map;
import java.util.TreeMap;

/**
 * Simple model based on a map.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class MapModel implements DataModel
{
   /**
    * The map used to store model variables.
    */
   private Map<String, String> map;
   
   /**
    * Indicates if the wrapped map shouldn't be modified.
    */
   private boolean readOnly;

   /**
    * Constructor.
    */
   public MapModel()
   {
      this.map = new TreeMap<String, String>();
      this.readOnly = false;
   }

   /**
    * Constructor.
    * @param map The map to wrap.
    * @param readOnly True if the wrapped map shouldn't be modified.
    */
   public MapModel(Map<String, String> map, boolean readOnly)
   {
      this.map = new TreeMap<String, String>();
      this.readOnly = readOnly;
   }

   /**
    * Removes all the model values.
    * @throws UnsupportedOperationException if the map is read-only.
    */
   public void clear()
   {
   	if(isReadOnly())
   	{
   		throw new UnsupportedOperationException();
   	}
   	else
   	{
   		this.map.clear();
   	}
   }

   /**
    * Indicates if the model contains a value for a given key.
    * @param key The key to look-up.
    * @return True if the model contains a value for the given key.
    */
   public boolean containsKey(String key)
   {
      return this.map.containsKey(key);
   }

   /**
    * Returns the model value for a given key.
    * @param key The key to look-up.
    * @return The model value for the given key.
    */
   public String get(String key)
   {
      return this.map.get(key);
   }
   
   /**
    * Indicates if this model cannot be modified.
    * @return True if this model cannot be modified.
    */
   public boolean isReadOnly()
   {
   	return this.readOnly;
   }

   /**
    * Puts the model value for a given name.
    * @param key The key to look-up.
    * @param value The value to put.
    * @throws UnsupportedOperationException if the map is read-only.
    */
   public void put(String key, String value)
   {
   	if(isReadOnly())
   	{
   		throw new UnsupportedOperationException();
   	}
   	else
   	{
   		this.map.put(key, value);
   	}
   }

   /**
    * Removes a model value for a given key.
    * @param key The key to look-up.
    * @return The old value removed.
    * @throws UnsupportedOperationException if the map is read-only.
    */
   public String remove(String key)
   {
   	if(isReadOnly())
   	{
   		throw new UnsupportedOperationException();
   	}
   	else
   	{
   		return this.map.remove(key);
   	}
   }
}
