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

package com.noelios.restlet.data;

import org.restlet.data.Reference;

/**
 * Reference to a contextual resource. Exemple URI: context://class/org/restlet/Restlet.class
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ClapReference extends Reference
{
	/**
	 * The resources will be resolved from the classloader associated with the local class. Examples:
	 * clap://class/rootPkg/subPkg/myClass.class or clap://class/rootPkg/file.html
	 * @see java.lang.Class#getClassLoader() 
	 */
	public static final int CLASS = 2; 

	/**
	 * The resources will be resolved from the system's classloader. Examples:
	 * clap://system/rootPkg/subPkg/myClass.class or clap://system/rootPkg/file.html
	 * @see java.lang.ClassLoader#getSystemClassLoader() 
	 */
	public static final int SYSTEM = 3; 

	/**
	 * The resources will be resolved from the current thread's classloader. Examples:
	 * clap://thread/rootPkg/subPkg/myClass.class or clap://thread/rootPkg/file.html
	 * @see java.lang.Thread#getContextClassLoader() 
	 */
	public static final int THREAD = 4; 
	
	/**
	 * Constructor.
	 * @param contextUri The context URI.
	 */
	public ClapReference(String contextUri)
	{
		super(contextUri);
	}
	
	/**
	 * Constructor.
	 * @param contextRef The context reference.
	 */
	public ClapReference(Reference contextRef)
	{
		super(contextRef.toString());
	}

	/**
	 * Constructor.
	 * @param authorityType The authority type for the resource path.
	 * @param path The resource path.
	 */
	public ClapReference(int authorityType, String path)
	{
		super("context://" + getAuthorityName(authorityType) + path);
	}

	/**
	 * Returns the type of authority.
	 * @return The type of authority.
	 */
	public int getAuthorityType()
	{
		int result = 0;
		String authority = getAuthority();
		
		if(authority != null)
		{
			if(authority.equalsIgnoreCase(getAuthorityName(CLASS)))
			{
				result = CLASS;
			}
			else if(authority.equalsIgnoreCase(getAuthorityName(SYSTEM)))
			{
				result = SYSTEM;
			}
			else if(authority.equalsIgnoreCase(getAuthorityName(THREAD)))
			{
				result = THREAD;
			}
		}
		
		return result;
	}
	
	/**
	 * Returns an authority name. 
	 * @param authority The authority.
	 * @return The name.
	 */
	public static String getAuthorityName(int authority)
	{
		String result = null;
		
		switch(authority)
		{
			case CLASS:
				result = "class";
				break;
			case SYSTEM:
				result = "system";
				break;
			case THREAD:
				result = "thread";
				break;
		}
		
		return result;
	}
	
}
