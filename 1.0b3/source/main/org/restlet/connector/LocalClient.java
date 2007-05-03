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

package org.restlet.connector;

import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.component.Component;

/**
 * Local client connector. Useful to call a component that resides inside the same JVM.
 */
public class LocalClient extends AbstractClient implements Client
{
   /** The local handler. */
   protected UniformInterface handler;

   /**
    * Constructor.
    * @param name The name of this REST client.
    * @param handler The local handler.
    */
   public LocalClient(String name, Component handler)
   {
      super(name);
      this.handler = handler;
   }

   /**
    * Returns the call handler.
    * @return The call handler.
    */
   public UniformInterface getHandler()
   {
      return this.handler;
   }
   
   /**
    * Sets the call handler.
    * @param handler The call handler.
    */
   public void setHandler(UniformInterface handler)
   {
      this.handler = handler;
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      getHandler().handle(call);
   }

}
