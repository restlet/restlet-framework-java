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

import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.connector.Client;
import org.restlet.connector.Server;

/**
 * Default Restlet container that can be easily subclassed.<br/> <br/> Component acting as a container for
 * call handlers (Restlets, Chainlets, Maplets, etc.). Calls are first intercepted by the container which can do various checks
 * before effectively delegating it to one of the registered root handlers.
 */
public class DefaultRestletContainer implements RestletContainer
{
   /** The delegate Restlet container. */
   protected RestletContainer delegate;

   /**
    * Constructor.
    * @param name The unique name of the container.
    */
   public DefaultRestletContainer(String name)
   {
      this.delegate = Manager.createRestletContainer(this, name);
   }

   /**
    * Returns the container.
    * @return The container.
    */
   public RestletContainer getContainer()
   {
      return this;
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
   public void callClient(String name, UniformCall call) throws IOException
   {
      delegate.callClient(name, call);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, UniformInterface target)
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
   public void attach(String pathPattern, Class<? extends UniformInterface> targetClass)
   {
      delegate.attach(pathPattern, targetClass);
   }

   /**
    * Detaches a target instance.
    * @param target The target instance to detach.
    */
   public void detach(UniformInterface target)
   {
      delegate.detach(target);
   }

   /**
    * Detaches a target class.
    * @param targetClass The target class to detach.
    */
   public void detach(Class<? extends UniformInterface> targetClass)
   {
      delegate.detach(targetClass);
   }

   /**
    * Delegates a call to one of the attached targets.<br/>
    * If no delegation is possible, a 404 error status (Client error, Not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(UniformCall call)
   {
      return delegate.delegate(call);
   }

   /** Start hook. */
   public void start() throws Exception
   {
      delegate.start();
   }

   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call)
   {
      delegate.handle(call);
   }

   /** Stop hook. */
   public void stop() throws Exception
   {
      delegate.stop();
   }

   /**
    * Indicates if the component is started.
    * @return True if the component is started.
    */
   public boolean isStarted()
   {
      return delegate.isStarted();
   }

   /**
    * Indicates if the component is stopped.
    * @return True if the component is stopped.
    */
   public boolean isStopped()
   {
      return delegate.isStopped();
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
