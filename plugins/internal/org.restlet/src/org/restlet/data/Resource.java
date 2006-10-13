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
 * Intended conceptual target of a hypertext reference. "Any information that can be named can be
 * a resource: a document or image, a temporal service (e.g. "today's weather in Los Angeles"), a collection
 * of other resources, a non-virtual object (e.g. a person), and so on. In other words, any concept that might
 * be the target of an author's hypertext reference must fit within the definition of a resource."<br/><br/>
 * "The only thing that is required to be static for a resource is the semantics of the mapping, since the
 * semantics is what distinguishes one resource from another." Roy T. Fielding<br/>
 * <br/>
 * Another definition adapted from the URI standard (RFC 3986): a resource is the conceptual mapping to a 
 * representation (also known as entity) or set of representations, not necessarily the representation which 
 * corresponds to that mapping at any particular instance in time. Thus, a resource can remain constant even
 * when its content (the representations to which it currently corresponds) changes over time, provided that 
 * the conceptual mapping is not changed in the process. In addition, a resource is always identified by a URI.
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_1">Source dissertation</a>
 * @see org.restlet.data.Representation
 * @see org.restlet.data.Reference
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Resource
{
	/** The modifiable list of identifiers. */
	private ReferenceList identifiers;

	/** The modifiable list of variants. */
	private List<Representation> variants;

	/**
	 * Indicates if it is allowed to delete the resource. The default value is false. 
	 * @return True if the method is allowed.
	 */
	public boolean allowDelete()
	{
		return false;
	}

	/**
	 * Indicates if it is allowed to get the variants. The default value is true. 
	 * @return True if the method is allowed.
	 */
	public boolean allowGet()
	{
		return true;
	}

	/**
	 * Indicates if it is allowed to post to the resource. The default value is false. 
	 * @return True if the method is allowed.
	 */
	public boolean allowPost()
	{
		return false;
	}

	/**
	 * Indicates if it is allowed to put to the resource. The default value is false. 
	 * @return True if the method is allowed.
	 */
	public boolean allowPut()
	{
		return false;
	}

	/**
	 * Returns the official identifier.
	 * @return The official identifier.
	 */
	public Reference getIdentifier()
	{
		if (getIdentifiers().isEmpty())
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
		if (getIdentifiers().isEmpty())
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
		if (this.identifiers == null) this.identifiers = new ReferenceList();
		return this.identifiers;
	}

	/**
	 * Returns the list of variants. Each variant is described by metadata and can provide several instances 
	 * of the variant's representation.
	 * @return The list of variants.
	 */
	public List<Representation> getVariants()
	{
		if (this.variants == null) this.variants = new ArrayList<Representation>();
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
