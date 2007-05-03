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

package org.restlet;

/**
 * Abstract implementation of a uniform interface handler.<br/>
 * The start and stop state is managed by default but with no other action.<br/>
 * Override the start and stop methods if needed.
 */
public abstract class AbstractHandler implements UniformInterface
{
   /** Indicates if the handler was started. */
   protected boolean started;

   /**
    * Constructor.
    */
   public AbstractHandler()
   {
      this.started = false;
   }

   /** Starts the handler. */
   public void start() throws Exception
   {
      this.started = true;
   }

   /** Stops the handler. */
   public void stop() throws Exception
   {
      this.started = false;
   }

   /**
    * Indicates if the handler is started.
    * @return True if the handler is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the handler is stopped.
    * @return True if the handler is stopped.
    */
   public boolean isStopped()
   {
      return !this.started;
   }

}
