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

import java.util.ArrayList;
import java.util.List;

import org.restlet.AbstractRestlet;
import org.restlet.component.Component;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;

/**
 * Abstract connector implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractConnector extends AbstractRestlet implements Connector
{
	/** The modifiable list of parameters. */
   protected ParameterList parameters;

   /** The protocols supported by the connector. */
   protected List<Protocol> protocols;
   
   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    */
   public AbstractConnector(Component owner, ParameterList parameters)
   {
   	super(owner);
   	this.parameters = parameters;
   }
   
	/**
	 * Returns the modifiable list of parameters.
	 * @return The modifiable list of parameters.
	 */
	public ParameterList getParameters()
	{
		if(this.parameters == null) this.parameters = new ParameterList();
		return this.parameters;
	}

   /**
    * Returns the protocols supported by the connector.
    * @return The protocols supported by the connector.
    */
   public List<Protocol> getProtocols()
   {
      if(this.protocols == null)
      {
      	this.protocols = new ArrayList<Protocol>();
      }
      
      return this.protocols;
   }

}
