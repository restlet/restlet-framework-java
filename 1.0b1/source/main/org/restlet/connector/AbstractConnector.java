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

/**
 * Abstract connector implementation.
 */
public abstract class AbstractConnector implements Connector
{
   /** Indicates if the connector was started. */
   protected boolean started;

   /** The name of this REST connector. */
   protected String name;

   /**
    * Constructor.
    * @param name The name of this REST connector.
    */
   public AbstractConnector(String name)
   {
      this.started = false;
      this.name = name;
   }

   /**
    * Returns the name of this REST connector.
    * @return The name of this REST connector.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Default connector";
   }

   /** Start hook. */
   public void start()
   {
      this.started = true;
   }

   /** Stop hook. */
   public void stop()
   {
      this.started = false;
   }

   /**
    * Indicates if the connector is started.
    * @return True if the connector is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the connector is stopped.
    * @return True if the connector is stopped.
    */
   public boolean isStopped()
   {
      return !isStarted();
   }

}
