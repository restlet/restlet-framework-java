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

package com.noelios.restlet.impl;

import java.io.IOException;
import java.io.InputStream;

/**
 * Context client connector call based on a class loader.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ClassLoaderCall extends ContextCall
{
   /** The wrapped class loader. */
   protected ClassLoader classLoader;
   
   /**
    * Constructor.
    * @param method The method name.
    * @param requestUri The request URI.
    * @param classLoader The class loader to use.
    * @throws IOException
    */
   public ClassLoaderCall(String method, String requestUri, ClassLoader classLoader) throws IOException
   {
      super(method, requestUri);
   }
   
   /**
    * Returns the class loader.
    * @return The class loader.
    */
   public ClassLoader getClassLoader()
   {
      return this.classLoader;
   }
   
   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public InputStream getResponseStream()
   {
   	return getClassLoader().getResourceAsStream(getResourceName());
   }
}
