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

package org.restlet;

/**
 * Chainer of calls to a target restlet. Automatic delegation is provided to the attached restlet.
 * Filtering work can be done in the handle() method, just remember to call the delegate() method before or
 * after your custom handling.<br/>
 * Note that during this handling, the restlet call paths are not supposed to be modified.
 */
public interface Chainlet extends Restlet
{
   /**
    * Attaches a restlet instance shared by all calls.
    * @param restlet The restlet to attach.
    */
   public void attach(Restlet restlet);

   /**
    * Attaches a restlet class. A new instance will be created for each call.
    * @param restletClass The restlet class to attach (must have a constructor taking a RestletContainer
    * parameter).
    */
   public void attach(Class<? extends Restlet> restletClass);

   /**
    * Detaches the current target restlet.
    */
   public void detach();

}
