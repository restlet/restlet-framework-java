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
public class ContextReference extends Reference
{
	public enum AuthorityType 
	{ 
		/**
		 * The resources will be resolved from the Web Application context, which is similar to the notion of 
		 * ServletContext in the Servlet specification. If the application is packaged as a WAR file, then 
		 * the resources correspond to files at the root of the archive, excluding the META-INF and WEB-INF
		 * directories. Examples: context://webapp/rootDir/subDir/file.html or context://webapp/rootFile.gif 
		 */
		WEB_APPLICATION,

		/**
		 * The resources will be resolved from the classloader associated with the local class. Examples:
		 * context://class/rootPkg/subPkg/myClass.class or context://class/rootPkg/file.html
		 * @see java.lang.Class#getClassLoader() 
		 */
		CLASS, 
		
		/**
		 * The resources will be resolved from the system's classloader. Examples:
		 * context://system/rootPkg/subPkg/myClass.class or context://system/rootPkg/file.html
		 * @see java.lang.ClassLoader#getSystemClassLoader() 
		 */
		SYSTEM, 
		
		/**
		 * The resources will be resolved from the current thread'classloader. Examples:
		 * context://thread/rootPkg/subPkg/myClass.class or context://thread/rootPkg/file.html
		 * @see java.lang.Thread#getContextClassLoader() 
		 */
		THREAD 
	}; 
	
	/**
	 * Constructor.
	 * @param contextUri The context URI.
	 */
	public ContextReference(String contextUri)
	{
		super(contextUri);
	}
	
	/**
	 * Constructor.
	 * @param contextRef The context reference.
	 */
	public ContextReference(Reference contextRef)
	{
		super(contextRef.toString());
	}

	/**
	 * Constructor.
	 * @param authorityType The authority type for the resource path.
	 * @param path The resource path.
	 */
	public ContextReference(AuthorityType authorityType, String path)
	{
		super("context://" + getAuthorityName(authorityType) + path);
	}

	/**
	 * Returns the type of authority.
	 * @return The type of authority.
	 */
	public AuthorityType getAuthorityType()
	{
		AuthorityType result = null;
		String authority = getAuthority();
		
		if(authority != null)
		{
			if(authority.equalsIgnoreCase(getAuthorityName(AuthorityType.WEB_APPLICATION)))
			{
				result = AuthorityType.WEB_APPLICATION;
			}
			else if(authority.equalsIgnoreCase(getAuthorityName(AuthorityType.WEB_APPLICATION)))
			{
				result = AuthorityType.CLASS;
			}
			else if(authority.equalsIgnoreCase(getAuthorityName(AuthorityType.WEB_APPLICATION)))
			{
				result = AuthorityType.SYSTEM;
			}
			else if(authority.equalsIgnoreCase(getAuthorityName(AuthorityType.WEB_APPLICATION)))
			{
				result = AuthorityType.THREAD;
			}
		}
		
		return result;
	}
	
	/**
	 * Returns an authority name. 
	 * @param authority The authority.
	 * @return The name.
	 */
	public static String getAuthorityName(AuthorityType authority)
	{
		String result = null;
		
		switch(authority)
		{
			case WEB_APPLICATION:
				result = "application";
				break;
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
