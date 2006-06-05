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
 * Reference to a JAR entry resource. Exemple URI: jar:http://www.foo.com/bar/baz.jar!/COM/foo/Quux.class
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @see java.net.JarURLConnection
 */
public class JarReference extends Reference
{
	/**
	 * Constructor.
	 * @param ref JAR reference.
	 */
	public JarReference(String jarUri)
	{
		super(jarUri);
	}
	
	/**
	 * Constructor.
	 * @param ref JAR reference.
	 */
	public JarReference(Reference ref)
	{
		super(ref.toString());
	}
	
	/**
	 * Constructor.
	 * @param jarFile The JAR file reference.
	 * @param entryPath The entry path inside the JAR file.
	 */
	public JarReference(Reference jarFile, String entryPath)
	{
		super("jar:" + jarFile.toString() + "!/" + entryPath);
	}
	
	
	public Reference getJarFileRef()
	{
		Reference result = null;
		String ssp = getSchemeSpecificPart();
		
		if(ssp != null)
		{
			int separatorIndex = ssp.indexOf("!/");
			
			if(separatorIndex != -1)
			{
				result = new Reference(ssp.substring(0, separatorIndex));
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the JAR entry path. 
	 * @return The JAR entry path.
	 */
	public String getEntryPath()
	{
		String result = null;
		String ssp = getSchemeSpecificPart();
		
		if(ssp != null)
		{
			int separatorIndex = ssp.indexOf("!/");
			
			if(separatorIndex != -1)
			{
				result = ssp.substring(separatorIndex + 2);
			}
		}
		
		return result;
	}
	
}
