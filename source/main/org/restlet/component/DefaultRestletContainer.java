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

package org.restlet.component;

import java.io.IOException;

import org.restlet.AbstractRestlet;
import org.restlet.Manager;
import org.restlet.Restlet;
import org.restlet.RestletCall;
import org.restlet.connector.Client;
import org.restlet.connector.Server;

/**
 * Default Restlet container that can be easily subclassed.<br/> <br/> Component acting as a container for
 * Restlets, Chainlets and Maplets. Calls are first intercepted by the container which can do various checks
 * before effectively delegating it to one of the registered root Restlets.
 */
public class DefaultRestletContainer extends AbstractRestlet implements RestletContainer
{
   /** The delegate Restlet container. */
   protected RestletContainer delegate;

   /**
    * Constructor.
    * @param name The unique name of the container.
    */
   public DefaultRestletContainer(String name)
   {
      this(null, name);
   }

   /**
    * Constructor.
    * @param server The parent Restlet server.
    * @param name The unique name of the container.
    */
   public DefaultRestletContainer(RestletServer server, String name)
   {
      this.delegate = Manager.createRestletContainer(server, name);
   }

   /**
    * Adds a server connector to this component.
    * @param server The server connector to add.
    * @return The server connector added.
    */
   public Server addServer(Server server)
   {
      return delegate.addServer(server);
   }

   /**
    * Removes a server connector from this component.
    * @param name The name of the server connector to remove.
    */
   public void removeServer(String name)
   {
      delegate.removeServer(name);
   }

   /**
    * Adds a client connector to this component.
    * @param client The client connector to add.
    * @return The client connector added.
    */
   public Client addClient(Client client)
   {
      return delegate.addClient(client);
   }

   /**
    * Removes a client connector from this component.
    * @param name The name of the client connector to remove.
    */
   public void removeClient(String name)
   {
      delegate.removeClient(name);
   }

   /**
    * Calls a client connector.
    * @param name The name of the client connector.
    * @param call The call to handle.
    */
   public void callClient(String name, RestletCall call) throws IOException
   {
      delegate.callClient(name, call);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Restlet target)
   {
      delegate.attach(pathPattern, target);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pathPattern The path pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer
    * parameter).
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Class<? extends Restlet> targetClass)
   {
      delegate.attach(pathPattern, targetClass);
   }

   /**
    * Detaches a target instance.
    * @param target The target instance to detach.
    */
   public void detach(Restlet target)
   {
      delegate.detach(target);
   }

   /**
    * Detaches a target class.
    * @param targetClass The target class to detach.
    */
   public void detach(Class<? extends Restlet> targetClass)
   {
      delegate.detach(targetClass);
   }

   /**
    * Delegates a call to one of the attached targets.<br/>
    * If no delegation is possible, a 404 error status (Client error, Not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(RestletCall call)
   {
      return delegate.delegate(call);
   }

   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(RestletCall call)
   {
      delegate.handle(call);
   }

   /**
    * Returns the name of this REST component.
    * @return The name of this REST component.
    */
   public String getName()
   {
      return delegate.getName();
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return delegate.getDescription();
   }

}
