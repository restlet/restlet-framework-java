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
 * Chainer of calls to a target Restlet. Interception or filtering can be done in the handle() 
 * method, just remember to call the delegateMaplet() method before or after your custom handling.<br/>
 * Note that during this handling, the call paths are not supposed to be modified.<br/>
 * Also note that you can attach and detach targets while handling incoming calls as the delegation code 
 * is ensured to be thread-safe and atomic.
 * @see <a href="http://www.restlet.org/tutorial#part07">Tutorial: Chainlets and call logging</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Chainlet extends Restlet
{
   /**
    * Attaches a target instance shared by all calls.
    * @param target The target instance to attach.
    */
   public void attach(Restlet target);

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer parameter).
    */
   public void attach(Class<? extends Restlet> targetClass);

   /**
    * Indicates if a target Restlet instance or class has been attached.
    * @return True if a target Restlet instance or class has been attached.
    */
   public boolean hasTarget();
      
   /**
    * Detaches the current target.
    */
   public void detach();

}
