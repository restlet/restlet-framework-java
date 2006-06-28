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

import org.restlet.component.Component;

/**
 * Uniform interface for REST handlers. "The central feature that distinguishes
 * the REST architectural style from other network-based styles is its emphasis on a uniform interface between
 * components. By applying the software engineering principle of generality to the component interface, the
 * overall system architecture is simplified and the visibility of interactions is improved. Implementations
 * are decoupled from the services they provide, which encourages independent evolvability." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 * dissertation</a>
 * @see <a href="http://www.restlet.org/tutorial#part03">Tutorial: Listening to Web browsers</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Restlet
{
   /** Starts the Restlet. */
   public void start() throws Exception;

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted();

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call);

   /** Stops the Restlet. */
   public void stop() throws Exception;

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped();

   /**
    * Returns the owner component.
    * @return The owner component.
    */
   public Component getOwner();

   /**
    * Sets the owner component.
    * @param owner The owner component.
    */
   public void setOwner(Component owner);
}
