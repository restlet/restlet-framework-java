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

import org.restlet.data.ParameterList;

/**
 * Context client connector call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class ContextCall extends ClientCallImpl 
{
   /** The resource name. */
   protected String resourceName;

   /** The status code. */
   protected int statusCode;
   
   /** The reason phrase. */
   protected String reasonPhrase;
   
   /**
    * Constructor.
    * @param method The method name.
    * @param requestUri The request URI.
    */
   public ContextCall(String method, String requestUri)
   {
      super(method, requestUri);
   }
   
   /**
    * Returns the resource name.
    * @return The resource name.
    */
   public String getResourceName()
   {
   	return this.resourceName;
   }
   
   /**
    * Returns the response address.<br/>
    * Corresponds to the IP address of the responding server.
    * @return The response address.
    */
   public String getResponseAddress()
   {
      return getLocalAddress();
   }

   /**
    * Returns the modifiable list of response headers.
    * @return The modifiable list of response headers.
    */
   public ParameterList getResponseHeaders()
   {
      // Ignore, not applicable to local files
      return null;
   }
   
   /**
    * Returns the response status code.
    * @return The response status code.
    */
   public int getResponseStatusCode()
   {
      return this.statusCode;
   }

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getResponseReasonPhrase()
   {
   	return this.reasonPhrase;
   }

}
