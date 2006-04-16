/*
 * Copyright 2005-2006 Jerome LOUVEL
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

import org.restlet.RestletCall;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * Local connector implementation.
 */
public abstract class AbstractServer extends AbstractConnector implements Server
{
   /** The target Restlet. */
   protected Restlet target;

   /** The Jetty listening address if specified. */
   protected String address;

   /** The Jetty listening port if specified. */
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
    * @param name The unique connector name.
    * @param target The target Restlet.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public AbstractServer(Protocol protocol, String name, Restlet target, String address, int port)
   {
      super(protocol, name);
      this.target = target;
      this.address = address;
      this.port = port;
   }

   /**
    * Handles a uniform call.<br/>
    * The default behavior is to ask the attached Restlet to handle the call.
    * @param call The uniform call to handle.
    */
   public void handle(RestletCall call)
   {
      getTarget().handle(call);
   }

   /**
    * Handles the server connector call.<br/> 
    * The default behavior is to create an UniformCall and invoke the target Restlet.
    * @param call The server connector call.
    */
   public void handle(ServerCall call) throws IOException
   {
      RestletCall uniformCall = call.toUniform();
      handle(uniformCall);
      call.setResponse(uniformCall);
      call.sendResponseHeaders();
      call.sendResponseOutput(uniformCall.getOutput());
   }

   /**
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

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Server connector";
   }

}
