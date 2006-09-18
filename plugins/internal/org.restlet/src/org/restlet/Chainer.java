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

package org.restlet;

/**
 * Restlet part of a processing chain. In addition to handling incoming calls like any Restlet, a handler 
 * can also resolve, either statically or dynamically, the next Restlet that will continue the processing chain.
 * Subclasses only have to implement the findNext(Call) method.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Chainer extends Restlet 
{
   /**
    * Constructor.
    */
   public Chainer()
   {
   	this(null);
   }

   /**
    * Constructor.
    * @param context The context.
    */
   public Chainer(Context context)
   {
   	super(context);
   }
   
   /**
    * Default implementation for all the handle*() methods that calls the next handler if it is available. 
    * @param call The call to handle.
    */
   protected void defaultHandle(Call call)
   {
   	Restlet next = getNext(call);
   	
   	if(next != null)
   	{
      	call.handle(next);
   	}
   	else
   	{
   		super.defaultHandle(call);
   	}
   }

   /**
	 * Returns the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet getNext(Call call)
	{
		return null;
	}

}
