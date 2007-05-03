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

import org.restlet.AbstractRestlet;
import org.restlet.component.Component;
import org.restlet.data.Protocol;

/**
 * Abstract connector implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractConnector extends AbstractRestlet implements Connector
{
	/** The name of this connector. */
   protected String name;

   /** The connector protocol. */
   protected Protocol protocol;
   
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
    * @param owner The owner component.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    */
   public AbstractConnector(Component owner, Protocol protocol, String name)
   {
   	super(owner);
   	this.name = name;
      this.protocol = protocol;
   }

   /**
    * Returns the name of this connector.
    * @return The name of this connector.
    */
   public String getName()
   {
   	return this.name;
   }

   /**
    * Sets the name of this connector.
    * @param name The name of this connector.
    */
   public void setName(String name)
   {
   	this.name = name;
   }

   /**
    * Returns the connector's protocol.
    * @return The connector's protocol.
    */
   public Protocol getProtocol()
   {
      return this.protocol;
   }

}
