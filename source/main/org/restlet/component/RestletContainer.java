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

package org.restlet.component;

import org.restlet.Chainlet;
import org.restlet.Factory;
import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.Call;

/**
 * Container for Maplets, Chainlets or Restlets. Note that a container is also a Chainlet and Maplet by itself.
 * It can also be part of a larger RestletServer.<br/>
 * If you chain a Restlet using one of the attach() methods with no URI pattern, then all the calls will be
 * directed to it. In other words, the Chainlet role has a higher priority than the Maplet role.
 * Calls are first intercepted by the container which can do various checks before effectively delegating it 
 * to one of the registered root Restlets. Restlet containers can also be contained within a Restlet server.
 * @see <a href="http://www.restlet.org/tutorial#part05">Tutorial: Restlets servers and containers</a>
 */
public class RestletContainer extends AbstractComponent implements Chainlet, Maplet
{
   /** The parent container who delegates. */
   protected Component parent;

   /** Delegate Chainlet handling root Restlets. */
   protected Chainlet delegateChainlet;

   /** Delegate Maplet handling root Restlets. */
   protected Maplet delegateMaplet;

   /**
    * Constructor.
    * @param name The unique name of the container.
    */
   public RestletContainer(String name)
   {
   	this(null, name);
   }

   /**
    * Constructor.
    * @param parent The parent component.
    * @param name The unique name of the container.
    */
   public RestletContainer(Component parent, String name)
   {
      super(name);
      this.parent = parent;
      this.delegateChainlet = Factory.getInstance().createChainlet(parent);
      this.delegateMaplet = Factory.getInstance().createMaplet(parent);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param target The target instance to attach.
    */
   public void attach(Restlet target)
   {
   	delegateChainlet.attach(target);
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer
    * parameter).
    */
   public void attach(Class<? extends Restlet> targetClass)
   {
   	delegateChainlet.attach(targetClass);
   }

   /**
    * Attaches a target instance shared by all calls.
    * @param pattern The URI pattern used to map calls.
    * @param target The target instance to attach.
    * @return The current Maplet for further attachments.
    * @see java.util.regex.Pattern
    */
   public Maplet attach(String pattern, Restlet target)
   {
      delegateMaplet.attach(pattern, target);
      return this;
   }

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pattern The URI pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    * @return The current Maplet for further attachments.
    * @see java.util.regex.Pattern
    */
   public Maplet attach(String pattern, Class<? extends Restlet> targetClass)
   {
      delegateMaplet.attach(pattern, targetClass);
      return this;
   }

   /**
    * Indicates if a target Restlet instance or class has been attached.
    * @return True if a target Restlet instance or class has been attached.
    */
   public boolean hasTarget()
   {
   	return delegateChainlet.hasTarget();
   }

   /**
    * Detaches the chained target.
    */
   public void detach()
   {
   	delegateChainlet.detach();
   }

   /**
    * Detaches a target instance.
    * @param target The target instance to detach.
    */
   public void detach(Restlet target)
   {
      delegateMaplet.detach(target);
   }

   /**
    * Detaches a target class.
    * @param targetClass The Restlet class to detach.
    */
   public void detach(Class<? extends Restlet> targetClass)
   {
      delegateMaplet.detach(targetClass);
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	if(delegateChainlet.hasTarget())
   	{
   		delegateChainlet.handle(call);
   	}
   	else
   	{
   		delegateMaplet.handle(call);
   	}
   }

   /**
    * Delegates a call to one of the attached targets.<br/>
    * If no delegation is possible, a 404 error status (Client error, Not found) will be returned.
    * @param call The call to delegateMaplet.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(Call call)
   {
      return delegateMaplet.delegate(call);
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Restlet container";
   }

}
