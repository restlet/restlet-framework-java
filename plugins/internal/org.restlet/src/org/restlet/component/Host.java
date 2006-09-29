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

package org.restlet.component;

import java.util.List;

import org.restlet.data.Protocol;

/**
 * Virtual host that mediates between server connectors and attached applications.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Host
{
	/** The list of allowed IP addresses. */
	private List<String> allowedAddresses;

	/** The list of allowed host names. */
	private List<String> allowedNames;

	/** The list of allowed port numbers. */
	private List<Integer> allowedPorts;

	/** The list of allowed protocols. */
	private List<Protocol> allowedProtocols;
	
	/** The list of attached applications. */
	private List<Application> applications;

	/**
	 * Constructor.
	 */
	public Host()
	{
		
	}
	
	public List<String> getAllowedAddresses()
	{
		return this.allowedAddresses;
	}
	
	public List<String> getAllowedNames()
	{
		return this.allowedNames;
	}
	
	public List<Integer> getAllowedPorts()
	{
		return this.allowedPorts;
	}
	
	public List<Protocol> getAllowedProtocols()
	{
		return this.allowedProtocols;
	}

   /**
    * Returns the modifiable list of applications.
    * @return The modifiable list of applications.
    */
   public List<Application> getApplications()
   {
		return this.applications;
   }
	
}
