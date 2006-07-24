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
import org.restlet.component.Component;
import org.restlet.data.ParameterList;

/**
 * Abstract server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractServer extends AbstractConnector implements Server
{
	/** The target Restlet. */
	protected Restlet target;

   /** The listening address if specified. */
   protected String address;

   /** The listening port if specified. */
   protected int port;
   
   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public AbstractServer(Component owner, ParameterList parameters, String address, int port)
   {
   	super(owner, parameters);
      this.address = address;
      this.port = port;
   }
   
   /**
    * Handles a call.<br/>
    * The default behavior is to ask the target Restlet to handle the call.
    * @param call The call to handle.
    */
	public void handle(Call call)
   {
      getTarget().handle(call);
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

}
