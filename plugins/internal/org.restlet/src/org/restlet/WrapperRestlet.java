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

package org.restlet;


/**
 * Wrapper for Restlet instances. Useful for application developer who need to enrich the Restlet with
 * some additional state or logic.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperRestlet extends Restlet
{
   /** Wrapped resource. */
   private Restlet wrappedRestlet;

   /**
    * Constructor.
    * @param wrappedRestlet The wrapped Restlet.
    */
   public WrapperRestlet(Restlet wrappedRestlet)
   {
      this.wrappedRestlet = wrappedRestlet;
   }

   /**
    * Returns the wrapped Restlet.
    * @return The wrapped Restlet.
    */
   public Restlet getWrappedRestlet()
   {
      return this.wrappedRestlet;
   }

   /**
    * Returns the context.
    * @return The context.
    */
   public Context getContext()
   {
      return this.wrappedRestlet.getContext();
   }

   /** Starts the Restlet. */
   public void start() throws Exception
   {
      this.wrappedRestlet.start();
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
      return this.wrappedRestlet.isStarted();
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      this.wrappedRestlet.handle(call);
   }

   /** Stops the Restlet. */
   public void stop() throws Exception
   {
      this.wrappedRestlet.stop();
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
      return this.wrappedRestlet.isStopped();
   }
}
