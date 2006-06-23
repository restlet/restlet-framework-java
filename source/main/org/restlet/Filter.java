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
 * Filter of calls to a target Restlet. Interception or filtering can be done in the beforeHandle() and
 * afterHandle() methods.<br/>
 * Note that during this handling, the call's context path and resource path are not expected to be modified.<br/>
 * Also note that you can attach and detach targets while handling incoming calls as the delegation code 
 * is ensured to be thread-safe and atomic.
 * @see <a href="http://www.restlet.org/tutorial#part07">Tutorial: Filters and call logging</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Filter extends Handler
{
	/**
    * Attaches a target Restlet shared by all calls.
    * @param target The target instance to attach.
    */
   public void attach(Restlet target);

   /**
    * Indicates if a target Restlet has been attached.
    * @return True if a target Restlet has been attached.
    */
   public boolean hasTarget();

   /**
    * Detaches the current target.
    */
   public void detach();

   /**
    * Handles a call by first invoking the beforeHandle() method for pre-filtering, then distributing the call 
    * to the target Restlet via the doHandle() method. When the target handling is completed, it finally 
    * invokes the afterHandle() method for post-filtering.
    * @param call The call to handle.
    */
	public void handle(Call call);

   /**
    * Allows filtering before its handling by the target Restlet. Does nothing by default.
    * @param call The call to filter.
    */
   public void beforeHandle(Call call);

   /**
    * Handles the call by distributing it to the target handler. 
    * @param call The call to handle.
    */
   public void doHandle(Call call);

   /**
    * Allows filtering after its handling by the target Restlet. Does nothing by default.
    * @param call The call to filter.
    */
   public void afterHandle(Call call);
	
}
