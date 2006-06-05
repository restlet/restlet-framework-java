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

import java.io.IOException;

import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * Abstract server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractServer extends AbstractConnector implements Server
{
	/** The target Restlet. */
	protected Restlet target;
	
   /** The delegate Server. */
   protected Server delegate;

   /** The listening address if specified. */
   protected String address;

   /** The listening port if specified. */
   protected int port;

   /** The SSL keystore path. */
   protected String keystorePath;

   /** The SSL keystore password. */
   protected String keystorePassword;

   /** The SSL key password. */
   protected String keyPassword;
   
   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param delegate The delegate Server.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public AbstractServer(Protocol protocol, Server delegate, String address, int port)
   {
      super(protocol);
      this.delegate = delegate;
      this.address = address;
      this.port = port;
   }

   /**
    * Returns the delegate server.
    * @return The delegate server.
    */
   public Server getDelegate()
   {
   	return this.delegate;
   }

   /**
    * Sets the delegate server.
    * @param delegate The delegate server.
    */
   public void setDelegate(Server delegate)
   {
   	this.delegate = delegate;
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
    * Handles the connector protocol call.<br/>
    * The default behavior is to create an REST call and delegate it to the attached Restlet.
    * @param call The connector call.
    */
   public void handle(ServerCall call) throws IOException
   {
   	getDelegate().handle(call);
   }

   /**
    * Configure the SSL properties for secure protocols like HTTPS.
    * @param keystorePath The path of the keystore file.
    * @param keystorePassword The keystore password.
    * @param keyPassword The password of the server key .
    */
   public void configureSsl(String keystorePath, String keystorePassword, String keyPassword)
   {
      this.keystorePath = keystorePath;
      this.keystorePassword = keystorePassword;
      this.keyPassword = keyPassword;
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
