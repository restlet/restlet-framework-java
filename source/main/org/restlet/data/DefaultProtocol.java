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
 * Default protocol to enable the communication between components..
 */
public class DefaultProtocol implements Protocol
{
	/** The name. */
	protected String name;
	
	/** The description. */
	protected String description;
	
	/** The scheme name. */
	protected String schemeName;
	
	/** The default port if known or -1. */
	protected int defaultPort;
	
	/**
	 * Constructor.
	 * @param schemeName The scheme name.
	 * @param name The unique name.
	 * @param description The description.
	 * @param defaultPort The default port.
	 */
	public DefaultProtocol(String schemeName, String name, String description, int defaultPort)
	{
		this.name = name;
		this.description = description;
		this.schemeName = schemeName;
		this.defaultPort = defaultPort;
	}

	/**
	 * Returns the name.
	 * @return The name.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Returns the description.
	 * @return The description.
	 */
   public String getDescription()
   {
   	return this.description;
   }
	
	/**
	 * Returns the URI scheme name. 
	 * @return The URI scheme name.
	 */
	public String getSchemeName()
	{
		return this.schemeName;
	}

	/**
	 * Returns the default port number.
	 * @return The default port number.
	 */
	public int getDefaultPort()
	{
		return this.defaultPort;
	}
	
   /**
    * Indicates if the protocol is equal to a given one.
    * @param protocol The protocol to compare to.
    * @return True if the protocol is equal to a given one.
    */
   public boolean equals(Protocol protocol)
   {
   	return getName().equalsIgnoreCase(protocol.getName());
   }

}
