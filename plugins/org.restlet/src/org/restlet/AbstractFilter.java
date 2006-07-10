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
 * Abstract Filter that can easily be subclassed. Subclasses should do their actual filtering in either
 * the beforeHandle() method or the afterHandle() method.
 * @see <a href="http://www.restlet.org/tutorial#part07">Tutorial: Filters and call logging</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractFilter extends AbstractHandler implements Filter
{
   /** The target Restlet. */
   protected Restlet target;

   /**
    * Constructor.
    */
   public AbstractFilter()
   {
      this(null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
   public AbstractFilter(Component owner)
   {
      super(owner);
      this.target = null;
   }
   
	/**
	 * Finds the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet findNext(Call call)
	{
		return getTarget();
	}

	/**
    * Sets the target Restlet shared by all calls going through this filter.
    * @param target The target Restlet.
    */
   public void setTarget(Restlet target)
   {
   	this.target = target;
   }

   /**
    * Returns the target Restlet.
    * @return The target Restlet or null.
    */
   public Restlet getTarget()
   {
   	return this.target;
   }

   /**
    * Indicates if there is a target Restlet.
    * @return True if there is a target Restlet.
    */
   public boolean hasTarget()
   {
   	return getTarget() != null;
   }

   /**
    * Handles a call by first invoking the beforeHandle() method for pre-filtering, then distributing the call 
    * to the target Restlet via the doHandle() method. When the target handling is completed, it finally 
    * invokes the afterHandle() method for post-filtering.
    * @param call The call to handle.
    */
	public void handle(Call call)
   {
		beforeHandle(call);
		doHandle(call);
      afterHandle(call);
   }

   /**
    * Allows filtering before its handling by the target Restlet. Does nothing by default.
    * @param call The call to filter.
    */
   protected void beforeHandle(Call call)
   {
   	// To be overriden
   }

   /**
    * Handles the call by distributing it to the target handler. 
    * @param call The call to handle.
    */
   protected void doHandle(Call call)
   {
      super.handle(call);
   }
   
   /**
    * Allows filtering after its handling by the target Restlet. Does nothing by default.
    * @param call The call to filter.
    */
   protected void afterHandle(Call call)
   {
   	// To be overriden
   }

}
