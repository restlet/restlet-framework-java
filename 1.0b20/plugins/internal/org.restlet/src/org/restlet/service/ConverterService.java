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

package org.restlet.service;

import org.restlet.resource.Representation;

/**
 * Service providing conversion between message entities and higher-level objects.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ConverterService
{
	/**
	 * Converts a representation into a higher-level object. Returns null by default. 
	 * @param representation The representation to convert. 
	 * @return A higher-level object.
	 */
	public Object toObject(Representation representation)
	{
		return null;
	}

	/**
	 * Converts a higher-level object into a representation. Returns null by default.
	 * @param object The higher-level object.
	 * @return A representation.
	 */
	public Representation toRepresentation(Object object)
	{
		return null;
	}
	
}
