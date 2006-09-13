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
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Status;

/**
 * Connector that listens for connections and responds to requests. By default, the handle(UniformCall)
 * method delegates the call received to the target restlet<br/><br/>"The primary connector types are
 * client and server. The essential difference between the two is that a client initiates communication by
 * making a request, whereas a server listens for connections and responds to requests in order to supply
 * access to its services. A component may include both client and server connectors." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_2">Source
 * dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Server extends Connector
{
   /** The chained Restlet. */
   private Restlet next;
   
   /**
    * Constructor.
    * @param context The context to use.
    */
   public Server(Context context)
   {
   	super(context);
   }
   
   /**
    * Constructor.
    * @param loggerName The logger name to use in the context.
    */
   public Server(String loggerName)
   {
   	super(loggerName);
   }
   
   /**
    * Constructor that uses the class name as the logger name.
    */
   public Server()
   {
   	super();
   }

   /**
    * Handles a call.<br/>
    * The default behavior is to ask the target Restlet to handle the call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	if(getNext() != null) 
   	{
   		getNext().handle(call);
   	}
   	else
   	{
   		call.setStatus(Status.SERVER_ERROR_INTERNAL);
   		getContext().getLogger().warning("Unable to find a chained Restlet for the Server connector.");
   	}
   }

   /**
    * Returns the chained Restlet.
    * @return The chained Restlet.
    */
   public Restlet getNext()
   {
      return this.next;
   }

   /**
    * Indicates if a chained Restlet is set.
    * @return True if a chained Restlet is set.
    */
   public boolean hasNext()
   {
      return getNext() != null;
   }

   /**
    * Sets the chained Restlet.
    * @param next The chained Restlet.
    */
   public void setNext(Restlet next)
   {
      this.next = next;
   }

}
