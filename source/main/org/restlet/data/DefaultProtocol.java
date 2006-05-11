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
	/** The scheme name. */
	protected String schemeName;
	
	/** The default port if known or -1. */
	protected int defaultPort;
	
	/** The unique protocol name. */
	protected String uniqueName;
	
	/**
	 * Constructor.
	 * @param schemeName The scheme name.
	 * @param uniqueName The unique protocol name.
	 * @param defaultPort The default port.
	 */
	public DefaultProtocol(String schemeName, String uniqueName, int defaultPort)
	{
		this.schemeName = schemeName;
		this.uniqueName = uniqueName;
		this.defaultPort = defaultPort;
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
   
   /**
    * Returns the name of this REST element.
    * @return The name of this REST element.
    */
   public String getName()
   {
   	return this.uniqueName;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
   	return "Protocol named " + getName();
   }
}
