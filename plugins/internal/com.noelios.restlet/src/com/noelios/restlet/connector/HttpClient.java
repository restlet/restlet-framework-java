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

package com.noelios.restlet.connector;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.connector.Client;

/**
 * Base HTTP client connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class HttpClient extends Client
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(HttpClient.class.getCanonicalName());

   /** The converter from uniform calls to HTTP calls. */
   private HttpClientConverter converter;

   /**
    * Constructor.
    */
   public HttpClient()
   {
      this.converter = null;
   }

   /**
    * Creates a low-level HTTP client call from a high-level uniform call.
    * @param call The high-level uniform call.
    * @return A low-level HTTP client call.
    */
   public abstract HttpClientCall create(Call call);

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      try
      {
         HttpClientCall httpCall = getConverter().toSpecific(this, call);
         getConverter().commit(httpCall, call);
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, "Error while handling an HTTP client call: ", e.getMessage());
         logger.log(Level.INFO, "Error while handling an HTTP client call", e);
      }
   }

   /**
    * Returns the converter from uniform calls to HTTP calls.
    * @return the converter from uniform calls to HTTP calls.
    */
   public HttpClientConverter getConverter()
   {
      if(this.converter == null) this.converter = new HttpClientConverter();
      return this.converter;
   }

   /**
    * Sets the converter from uniform calls to HTTP calls.
    * @param converter The converter to set.
    */
   public void setConverter(HttpClientConverter converter)
   {
      this.converter = converter;
   }
}
