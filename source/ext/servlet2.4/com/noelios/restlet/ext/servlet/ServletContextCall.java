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

package com.noelios.restlet.ext.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import com.noelios.restlet.data.ContextReference;
import com.noelios.restlet.impl.ContextCall;

/**
 * Client connector call based on resources available in the Servlet context.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServletContextCall extends ContextCall
{
   /** The wrapped Servlet context. */
   protected ServletContext context;
   
   /**
    * Constructor.
    * @param method The method name.
    * @param requestUri The request URI.
    * @param context The Servlet context to wrap.
    * @throws IOException
    */
   public ServletContextCall(String method, String requestUri, ServletContext context) throws IOException
   {
      super(method, requestUri);
      this.context = context;
      this.resourceName = new ContextReference(requestUri).getPath();
   }
   
   /**
    * Returns the Servlet context.
    * @return The Servlet context.
    */
   public ServletContext getContext()
   {
      return this.context;
   }
   
   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public InputStream getResponseStream()
   {
      return getContext().getResourceAsStream(getResourceName());
   }
}
