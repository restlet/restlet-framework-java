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

import org.restlet.AbstractRestlet;
import org.restlet.component.Component;
import org.restlet.data.Protocol;

/**
 * Abstract connector implementation.
 */
public abstract class AbstractConnector extends AbstractRestlet implements Connector
{
   /** The connector protocol. */
   protected Protocol protocol;

   /** The unique connector name. */
   protected String name;
   
   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    */
   public AbstractConnector(Protocol protocol, String name)
   {
      this(null, protocol, name);
   }
   
   /**
    * Constructor.
    * @param parent The parent component.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    */
   public AbstractConnector(Component parent, Protocol protocol, String name)
   {
   	super(parent);
      this.protocol = protocol;
      this.name = name;
      this.started = false;
   }

   /**
    * Returns the connector's protocol.
    * @return The connector's protocol.
    */
   public Protocol getProtocol()
   {
      return this.protocol;
   }

   /**
    * Returns the name of this REST connector.
    * @return The name of this REST connector.
    */
   public String getName()
   {
      return this.name;
   }

}
