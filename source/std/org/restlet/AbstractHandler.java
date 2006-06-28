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

import org.restlet.component.Component;

/**
 * Abstract Handler that can easily be subclassed. Concrete classes only have to implement the 
 * findNext(Call) method.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractHandler extends AbstractRestlet implements Handler
{
   /**
    * Constructor.
    */
   public AbstractHandler()
   {
      super(null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
   public AbstractHandler(Component owner)
   {
   	super(owner);
   }
	
	/**
	 * Finds the next Restlet then, if it is available, asks it to handle the call.  
	 * @param call The current call.
	 */
	public void nextHandle(Call call)
	{
   	handle(call, findNext(call));
	}
   
   /**
    * Default implementation for all the handle*() methods that simply calls the nextHandle() method. 
    * @param call The call to handle.
    */
   protected void defaultHandle(Call call)
   {
   	nextHandle(call);
   }

}
