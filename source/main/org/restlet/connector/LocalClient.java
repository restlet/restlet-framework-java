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

package org.restlet.connector;

import org.restlet.Call;
import org.restlet.Restlet;

/**
 * Local client connector. Useful to call a component that resides inside the same JVM.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class LocalClient extends AbstractClient
{
   /** The target Restlet. */
   protected Restlet target;

   /**
    * Constructor.
    * @param target The target Restlet.
    */
   public LocalClient(Restlet target)
   {
      super(null);
      this.target = target;
   }

   /**
    * Returns a new client call.
    * @param method The request method.
    * @param resourceUri The requested resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @return A new client call.
    */
   public ClientCall createCall(String method, String resourceUri, boolean hasInput)
   {
      return null;
   }

   /**
    * Returns the target Restlet.
    * @return The target Restlet.
    */
   public Restlet getTarget()
   {
      return this.target;
   }
   
   /**
    * Sets the target Restlet.
    * @param target The target Restlet.
    */
   public void setTarget(Restlet target)
   {
      this.target = target;
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      getTarget().handle(call);
   }

}
