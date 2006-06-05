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

import java.util.Map;
import java.util.TreeMap;

import org.restlet.AbstractRestlet;
import org.restlet.component.Component;
import org.restlet.data.Protocol;

/**
 * Abstract connector implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractConnector extends AbstractRestlet implements Connector
{
	/** The modifiable map of properties. */
   protected Map<String, String> properties;

   /** The connector protocol. */
   protected Protocol protocol;
   
   /**
    * Constructor.
    * @param protocol The connector protocol.
    */
   public AbstractConnector(Protocol protocol)
   {
      this(null, protocol);
   }
   
   /**
    * Constructor.
    * @param owner The owner component.
    * @param protocol The connector protocol.
    */
   public AbstractConnector(Component owner, Protocol protocol)
   {
   	super(owner);
   	this.properties = null;
      this.protocol = protocol;
   }
   
	/**
	 * Returns the modifiable map of properties.
	 * @return The modifiable map of properties.
	 */
	public Map<String, String> getProperties()
	{
		if(this.properties == null) this.properties = new TreeMap<String, String>();
		return this.properties;
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
