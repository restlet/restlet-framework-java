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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Protocol;

/**
 * Router of calls from server Connectors to attached Restlets. The attached Restlets are typically
 * Applications.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class VirtualHost extends Router
{
	public static final String ALL_ADDRESSES = "0.0.0.0";
	public static final String ALL_DOMAINS = "*";
	public static final Integer ALL_PORTS = -1;
	
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
	private List<String> allowedDomains;
	
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
    */
	public VirtualHost()
   {
		this(null);
   }
	
	/**
    * Constructor.
    * @param context The context.
    */
	public VirtualHost(Context context)
   {
		super(context);
		this.allowedAddresses = new ArrayList<String>();
		this.allowedDomains = new ArrayList<String>();
		this.allowedPorts = new ArrayList<Integer>();
		this.allowedProtocols = new ArrayList<Protocol>();
	}

	/**
	 * Creates a new default virtual host. Accepts all incoming calls. 
	 * @param context The context.
	 * @return A new default virtual host.
	 */
	public static VirtualHost createDefaultHost(Context context)
	{
		VirtualHost result = new VirtualHost(context);

		// Add allowed IP addresses
		result.getAllowedAddresses().add(ALL_ADDRESSES);

		// Add allowed domain names
		result.getAllowedDomains().add(ALL_DOMAINS);

		// Add allowed port numbers (all by default)
		result.getAllowedPorts().add(ALL_PORTS);

		// Add allowed protocols (all by default)
		result.getAllowedProtocols().add(Protocol.ALL);
		
		return result;
	}

	/**
	 * Creates a new local virtual host. Accepts incoming calls to the local host name or IP address. 
	 * @param context The context.
	 * @return A new local virtual host.
	 */
	public static VirtualHost createLocalHost(Context context)
	{
		VirtualHost result = new VirtualHost(context);

		// Add allowed IP addresses
		result.getAllowedAddresses().add("127.0.0.1");
		result.getAllowedAddresses().add(getLocalHostAddress());

		// Add allowed domain names
		result.getAllowedDomains().add("localhost");
		result.getAllowedDomains().add("127.0.0.1");
		result.getAllowedDomains().add(getLocalHostName());

		// Add allowed port numbers (all by default)
		result.getAllowedPorts().add(ALL_PORTS);

		// Add allowed protocols (all by default)
		result.getAllowedProtocols().add(Protocol.ALL);
		
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
	public List<String> getAllowedDomains()
	{
		return this.allowedDomains;
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

}
