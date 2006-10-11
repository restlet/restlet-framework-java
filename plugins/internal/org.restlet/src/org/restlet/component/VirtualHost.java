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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Application;
import org.restlet.Router;
import org.restlet.data.Protocol;

/**
 * Router dispatching calls from server connectors to application delegates.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class VirtualHost extends Router
{
	public static final String ALL_ADDRESSES = "0.0.0.0";
	public static final String ALL_NAMES = "*";
	public static final Integer ALL_PORTS = -1;
	
	/** The parent container. */
	private Container container;
	
	/** The display name. */
	private String name;
	
	/** 
	 * The modifiable list of allowed IP addresses. 
	 * You can add the ALL_ADDRESSES (-1) to allow any IP address. 
	 */
	private List<String> allowedAddresses;

	/** 
	 * The modifiable list of allowed domain names. 
	 * You can add the ALL_NAMES ("*") to allow any domain name. 
	 */
	private List<String> allowedNames;
	
	/**
	 * The modifiable list of allowed port numbers.
	 * You can add the ALL_PORTS (-1) to allow any port number. 
	 */
	private List<Integer> allowedPorts;

	/** 
	 * The modifiable list of allowed protocols. 
	 * You can add the Protocol.ALL to allow any protocol. 
	 */
	private List<Protocol> allowedProtocols;

	/**
    * Constructor.
    * @param container The parent container.
    */
	public VirtualHost(Container container)
   {
		super(container.getContext());
		this.container = container;
		this.allowedAddresses = new ArrayList<String>();
		this.allowedNames = new ArrayList<String>();
		this.allowedPorts = new ArrayList<Integer>();
		this.allowedProtocols = new ArrayList<Protocol>();
	}
	
	/**
	 * Deploys an application to the parent container and attaches it to this virtual host. 
	 * @param uriPattern The URI pattern that must match the relative part of the resource URI. 
	 * @param application The application to deploy and attach.
	 * @return The deployed application.
	 */
	public ApplicationDelegate attach(String uriPattern, Application application)
	{
		ApplicationDelegate result = new ApplicationDelegate(getContainer(), application, null);
		getScorers().add(uriPattern, result);
		return result;
	}

	/**
	 * Returns the modifiable list of allowed IP addresses. 
	 * You can add the ALL_ADDRESSES (-1) to allow any IP address. 
	 * @return The modifiable list of allowed IP addresses.
	 */
	public List<String> getAllowedAddresses()
	{
		return this.allowedAddresses;
	}

	/**
	 * Returns the modifiable list of allowed domain names. 
	 * You can add the ALL_NAMES ("*") to allow any domain name. 
	 * @return The modifiable list of allowed domain names.
	 */
	public List<String> getAllowedNames()
	{
		return this.allowedNames;
	}
	
	/**
	 * Returns the modifiable list of allowed port numbers.
	 * You can add the ALL_PORTS (-1) to allow any port number. 
	 * @return The modifiable list of allowed port numbers.
	 */
	public List<Integer> getAllowedPorts()
	{
		return this.allowedPorts;
	}
	
	/**
	 * Returns the modifiable list of allowed protocols. 
	 * You can add the Protocol.ALL to allow any protocol. 
	 * @return The modifiable list of allowed protocols.
	 */
	public List<Protocol> getAllowedProtocols()
	{
		return this.allowedProtocols;
	}

	/**
	 * Returns the display name.
	 * @return The display name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the display name.
	 * @param name The display name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Returns the local host name.
	 * @return The local host name.
	 */
	public static String getLocalHostName()
	{
		String result = null;
		
		try
		{
			result = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
		}
		
		return result;
	}
	
	/**
	 * Returns the local host IP address.
	 * @return The local host IP address.
	 */
	public static String getLocalHostAddress()
	{
		String result = null;
		
		try
		{
			result = InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException e)
		{
		}
		
		return result;
	}
	
	/**
	 * Returns the IP address of a given domain name.
	 * @param domain The domain name.
	 * @return The IP address.
	 */
	public static String getIpAddress(String domain)
	{
		String result = null;
		
		try
		{
			result = InetAddress.getByName(domain).getHostAddress();
		}
		catch (UnknownHostException e)
		{
		}
		
		return result;
	}

	/**
	 * Returns the parent container.
	 * @return the parent container.
	 */
	public Container getContainer()
	{
		return container;
	}

	/**
	 * Sets the parent container.
	 * @param container The parent container.
	 */
	public void setContainer(Container container)
	{
		this.container = container;
	}
	
}
