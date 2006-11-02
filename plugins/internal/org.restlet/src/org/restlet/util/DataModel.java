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

/**
 * Data model that can be used by the template engines. If this model is read-only, some methods will throw an
 * UnsupportedOperationException exception. It is similar to the JDK's Map interface but has a narrower scope
 * which makes it more suitable for dynamic models and easier to be implemented by domain classes.
 * @see com.noelios.restlet.util.StringTemplate
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface DataModel
{
   /**
    * Removes all the model values.
    * @throws UnsupportedOperationException if the map is read-only.
    */
   public void clear();

   /**
    * Indicates if the model contains a value for a given key.
    * @param key The key to look-up.
    * @return True if the model contains a value for the given key.
    */
   public boolean containsKey(String key);
   
   /**
    * Returns the model value for a given key.
    * @param key The key to look-up.
    * @return The model value for the given key.
    */
   public String get(String key);
   
   /**
    * Indicates if this model cannot be modified.
    * @return True if this model cannot be modified.
    */
   public boolean isReadOnly();

   /**
    * Puts the model value for a given name.
    * @param key The key to look-up.
    * @param value The value to put.
    * @throws UnsupportedOperationException if the map is read-only.
    */
   public void put(String key, String value);

   /**
    * Removes a model value for a given key.
    * @param key The key to look-up.
    * @return The old value removed.
    * @throws UnsupportedOperationException if the map is read-only.
    */
   public String remove(String key);
}
