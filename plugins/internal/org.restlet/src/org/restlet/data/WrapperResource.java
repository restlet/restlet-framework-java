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

import java.util.List;



/**
 * Resource wrapper. Useful for application developer who need to enrich the resource 
 * with application related properties and behavior.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperResource implements Resource
{
	/** The wrapped resource. */
	private Resource wrappedResource;
	
   /**
    * Constructor.
    * @param wrappedResource The wrapped resource.
    */
   public WrapperResource(Resource wrappedResource)
   {
   	this.wrappedResource = wrappedResource;
   }

   /**
    * Returns the wrapped Resource.
    * @return The wrapped Resource.
    */
   protected Resource getWrappedResource()
   {
   	return this.wrappedResource;
   }
   
	/**
	 * Returns the official identifier.
	 * @return The official identifier.
	 */
	public Reference getIdentifier()
	{
		return getWrappedResource().getIdentifier();
	}
	
	/**
	 * Sets the official identifier.
	 * @param identifier The official identifier.
	 */
	public void setIdentifier(Reference identifier)
	{
		getWrappedResource().setIdentifier(identifier);
	}
	
	/**
	 * Sets the official identifier from a URI string.
	 * @param identifierUri The official identifier to parse.
	 */
	public void setIdentifier(String identifierUri)
	{
		getWrappedResource().setIdentifier(identifierUri);
	}

	/**
	 * Returns the list of all the identifiers for the resource. The list is composed of the official identifier
	 * followed by all the alias identifiers.
	 * @return The list of all the identifiers for the resource.
	 */
	public ReferenceList getIdentifiers()
	{
		return getWrappedResource().getIdentifiers();
	}
	
	/**
	 * Returns the list of variants. Each variant is described by metadata and can provide several instances 
	 * of the variant's representation.
	 * @return The list of variants.
	 */
	public List<Representation> getVariants()
	{
		return getWrappedResource().getVariants();
	}
	
	/**
	 * Puts a variant representation in the resource.
	 * @param variant A new or updated variant representation. 
	 * @return The result status.
	 */
	public Status put(Representation variant)
	{
		return getWrappedResource().put(variant);
	}
	
	/**
	 * Asks the resource to delete itself and all its representations.
	 * @return The result status. 
	 */
	public Status delete()
	{
		return getWrappedResource().delete();
	}
	
	/**
	 * Sets a new list of all the identifiers for the resource.  
	 * @param identifiers The new list of identifiers. 
	 */
	public void setIdentifiers(ReferenceList identifiers)
	{
		getWrappedResource().setIdentifiers(identifiers);
	}
	
	/**
	 * Sets a new list of variants. 
	 * @param variants The new list of variants.
	 */
	public void setVariants(List<Representation> variants)
	{
		getWrappedResource().setVariants(variants);
	}

}
