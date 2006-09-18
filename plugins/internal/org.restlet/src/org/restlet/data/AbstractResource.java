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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract resource that can easily be subclassed. It automatically handles the GET calls by using server-side
 * content negotiation on the available variants. Other methods can easily be implemented using the corresponding
 * handle*() method, as for any subclass of AbstractRestlet. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractResource implements Resource
{
   /** The modifiable list of identifiers. */
	private ReferenceList identifiers;
   
   /** The modifiable list of variants. */
	private List<Representation> variants;

	/**
	 * Returns the official identifier.
	 * @return The official identifier.
	 */
	public Reference getIdentifier()
	{
		if(getIdentifiers().isEmpty())
		{
			return null;
		}
		else
		{
			return getIdentifiers().get(0);
		}
	}

	/**
	 * Sets the official identifier.
	 * @param identifier The official identifier.
	 */
	public void setIdentifier(Reference identifier)
	{
		if(getIdentifiers().isEmpty())
		{
			getIdentifiers().add(identifier);
		}
		else
		{
			getIdentifiers().set(0, identifier);
		}
	}
	
	/**
	 * Sets the official identifier from a URI string.
	 * @param identifierUri The official identifier to parse.
	 */
	public void setIdentifier(String identifierUri)
	{
		setIdentifier(new Reference(identifierUri));
	}

	/**
	 * Returns the list of all the identifiers for the resource. The list is composed of the official identifier
	 * followed by all the alias identifiers.
	 * @return The list of all the identifiers for the resource.
	 */
	public ReferenceList getIdentifiers()
	{
		if(this.identifiers == null) this.identifiers = new ReferenceList();
		return this.identifiers;
	}
	
	/**
	 * Returns the list of variants. Each variant is described by metadata and can provide several instances 
	 * of the variant's representation.
	 * @return The list of variants.
	 */
	public List<Representation> getVariants()
	{
		if(this.variants == null) this.variants = new ArrayList<Representation>();
		return this.variants;
	}
	
	/**
	 * Posts a variant representation in the resource.
	 * @param entity The posted entity. 
	 * @return The result information.
	 */
	public Result post(Representation entity)
	{
		return new Result(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
	
	/**
	 * Puts a variant representation in the resource.
	 * @param variant A new or updated variant representation. 
	 * @return The result information.
	 */
	public Result put(Representation variant)
	{
		return new Result(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
	
	/**
	 * Asks the resource to delete itself and all its representations.
	 * @return The result information. 
	 */
	public Result delete()
	{
		return new Result(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
	
	/**
	 * Sets a new list of all the identifiers for the resource.  
	 * @param identifiers The new list of identifiers. 
	 */
	public void setIdentifiers(ReferenceList identifiers)
	{
		this.identifiers = identifiers;
	}
	
	/**
	 * Sets a new list of variants. 
	 * @param variants The new list of variants.
	 */
	public void setVariants(List<Representation> variants)
	{
		this.variants = variants;
	}

}
