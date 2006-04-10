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

package org.restlet.connector;

import org.restlet.Restlet;

/**
 * Connector used to reduce interaction latency.<br/><br/> "The cache connector, can be located on the
 * interface to a client or server connector in order to save cacheable responses to current interactions so
 * that they can be reused for later requested interactions. A cache may be used by a client to avoid
 * repetition of network communication, or by a server to avoid repeating the process of generating a
 * response, with both cases serving to reduce interaction latency. A cache is typically implemented within
 * the address space of the connector that uses it." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_2">Source
 * dissertation</a>
 */
public interface Cache extends Connector
{
   /**
    * Returns the cached target Restlet.<br/>
    * This Restlet is invoked if a call can't be processed directly by the cache. 
    * @return The cached target Restlet.
    */
   public Restlet getCachedTarget();

   /**
    * Sets the cached target Restlet.
    * This Restlet is invoked if a call can't be processed directly by the cache. 
    * @param target The cached target Restlet.
    */
   public void setCachedTarget(Restlet target);
}
