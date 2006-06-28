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
 * Restlet that is part of a processing chain. In addition to handling incoming calls like any Restlet, a handler
 * can also resolve, either statically or dynamically, the next Restlet that will continue the processing chain.  
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Handler extends Restlet
{
   /**
	 * Finds the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet findNext(Call call);
	
	/**
	 * Finds the next Restlet then, if it is available, asks it to handle the call.  
	 * @param call The current call.
	 */
	public void nextHandle(Call call);
}
