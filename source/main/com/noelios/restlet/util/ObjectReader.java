/*
 * Copyright 2005-2006 Jerome LOUVEL
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Object instance reader.<br/>
 * Based on Java object serialization.
 */
public class ObjectReader extends BufferedInputStream
{
   /**
    * Constructor.
    * @param objectStream The object stream to deserialize.
    */
   public ObjectReader(InputStream objectStream)
   {
      super(objectStream);
   }

   /**
    * Returns the representation as a Java object.
    * @return The representation as a Java object.
    * @throws IOException 
    * @throws ClassNotFoundException 
    */
   public Object readObject() throws IOException, ClassNotFoundException
   {
      Object result = null;
      ObjectInputStream ois = new ObjectInputStream(this);
      result = ois.readObject();
      ois.close();
      return result;
   }

}
