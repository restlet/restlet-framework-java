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

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.component.Component;

/**
 * Abstract Resource that can easily be subclassed. It automatically handles the GET calls by using server-side
 * content negotiation on the available variants. Other methods can easily be implemented using the corresponding
 * handle*() method, as for any subclass of AbstractRestlet. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractResource extends AbstractRestlet implements Resource
{
   /** The modifiable list of identifiers. */
   protected ReferenceList identifiers;
   
   /** The modifiable list of variants. */
   protected List<Representation> variants;
   
   /** The language to use if content negotiation fails. */
   protected Language fallbackLanguage;
   
   /**
    * Constructor.
    */
   public AbstractResource()
   {
      this(null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
   public AbstractResource(Component owner)
   {
   	super(owner);
   	this.identifiers = null;
   }
   
   /**
    * Handles a GET call.
    * @param call The call to handle.
    */
   protected void handleGet(Call call)
   {
   	call.setBestOutput(this, fallbackLanguage);
   }

   /**
    * Returns the language to use if content negotiation fails.
    * @return The language to use if content negotiation fails.
    */
   public Language getFallbackLanguage()
   {
   	return this.fallbackLanguage;
   }
   
   /**
    * Sets the language to use if content negotiation fails.
    * @param fallbackLanguage The language to use if content negotiation fails.
    */
   public void setFallbackLanguage(Language fallbackLanguage)
   {
   	this.fallbackLanguage = fallbackLanguage;
   }

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

}
