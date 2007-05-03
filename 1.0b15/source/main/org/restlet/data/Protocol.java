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

package org.restlet.data;


/**
 * Protocol used by a client and a server connector to enable the communication
 * between two distributed components.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Protocol extends ControlData
{
	/**
	 * Returns the name.
	 * @return The name.
	 */
	public String getName();

	/**
	 * Returns the description.
	 * @return The description.
	 */
   public String getDescription();
	
	/**
	 * Returns the URI scheme name. 
	 * @return The URI scheme name.
	 */
	public String getSchemeName();
	
	/**
	 * Returns the default port number.
	 * @return The default port number.
	 */
	public int getDefaultPort();
	
   /**
    * Indicates if the protocol is equal to a given one.
    * @param protocol The protocol to compare to.
    * @return True if the protocol is equal to a given one.
    */
   public boolean equals(Protocol protocol);
}
