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

import java.util.List;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.UniformInterface;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Protocol;
import org.restlet.spi.Factory;

/**
 * Component attached to a virtual host and managed by a parent container. Applications are also guaranteed
 * to be portable between containers implementing the same Restlet API.  
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Application extends Component 
{
	/**
	 * Constructor.
	 * @param wrappedApplication The wrapped application. 
	 */
	protected Application(Application wrappedApplication)
	{
		super(wrappedApplication);
	}

   /**
    * Constructor.
    * @param container The parent container.
    */
   public Application(Container container)
   {
   	this(container, null);
   }
   
   /**
    * Constructor.
    * @param container The parent container.
    * @param root The root handler.
    */
   public Application(Container container, UniformInterface root)
   {
		this(Factory.getInstance().createApplication(container));
		setRoot(root);
   }

   /**
    * Handles a request.
    * @param request The request to handle.
    * @param response The response to update.
    */
	public void handle(Request request, Response response)
   {
   	getWrappedApplication().handle(request, response);
   }
   
   /**
	 * Returns the author(s).
	 * @return The author(s).
	 */
	public String getAuthor()
	{
		return getWrappedApplication().getAuthor();
	}
   
	/**
	 * Returns the list of client protocols used. 
	 * @return The list of client protocols used.
	 */
	public List<Protocol> getClientProtocols()
	{
		return getWrappedApplication().getClientProtocols();
	}
	
	/**
	 * Returns the default encoding for local representations.
	 * @return The default encoding for local representations.
	 */
	public Encoding getDefaultEncoding()
	{
		return getWrappedApplication().getDefaultEncoding();
	}

	/**
	 * Returns the default language for local representations.
	 * @return The default language for local representations.
	 */
	public Language getDefaultLanguage()
	{
		return getWrappedApplication().getDefaultLanguage();
	}

	/**
	 * Returns the default media type for local representations.
	 * @return The default media type for local representations.
	 */
	public MediaType getDefaultMediaType()
	{
		return getWrappedApplication().getDefaultMediaType();
	}

	/**
	 * Returns the description.
	 * @return The description
	 */
	public String getDescription()
	{
		return getWrappedApplication().getDescription();
	}

	/**
	 * Returns the list of index names (ex: index.html).
	 * @return The list of index names (ex: index.html).
	 */
	public List<String> getIndexNames()
	{
		return getWrappedApplication().getIndexNames();
	}

	/**
	 * Returns the mappings from extension names to metadata.
	 * @return The mappings from extension names to metadata.
	 */
	public Map<String, Metadata> getMetadataMappings()
	{
		return getWrappedApplication().getMetadataMappings();
	}

	/**
	 * Returns the display name.
	 * @return The display name.
	 */
	public String getName()
	{
		return getWrappedApplication().getName();
	}

	/**
	 * Returns the owner(s).
	 * @return The owner(s).
	 */
	public String getOwner()
	{
		return getWrappedApplication().getOwner();
	}

	/**
	 * Returns the root handler.
	 * @return The root handler.
	 */
	public UniformInterface getRoot()
	{
		return getWrappedApplication().getRoot();
	}

	/**
	 * Returns the list of server protocols accepted. 
	 * @return The list of server protocols accepted.
	 */
	public List<Protocol> getServerProtocols()
	{
		return getWrappedApplication().getServerProtocols();
	}

	/**
    * Returns the wrapped application.
    * @return The wrapped application.
    */
   protected Application getWrappedApplication()
   {
   	return (Application)getWrappedComponent();
   }

	/**
	 * Indicates if a root handler is set. 
	 * @return True if a root handler is set. 
	 */
	public boolean hasRoot()
	{
		return getWrappedApplication().hasRoot();
	}

	/**
	 * Sets the author(s).
	 * @param author The author(s).
	 */
	public void setAuthor(String author)
	{
		getWrappedApplication().setAuthor(author);
	}

	/**
	 * Sets the default encoding for local representations.
	 * @param defaultEncoding The default encoding for local representations.
	 */
	public void setDefaultEncoding(Encoding defaultEncoding)
	{
		getWrappedApplication().setDefaultEncoding(defaultEncoding);
	}

	/**
	 * Sets the default language for local representations.
	 * @param defaultLanguage The default language for local representations.
	 */
	public void setDefaultLanguage(Language defaultLanguage)
	{
		getWrappedApplication().setDefaultLanguage(defaultLanguage);
	}

	/**
	 * Sets the default media type for local representations.
	 * @param defaultMediaType The default media type for local representations.
	 */
	public void setDefaultMediaType(MediaType defaultMediaType)
	{
		getWrappedApplication().setDefaultMediaType(defaultMediaType);
	}

	/**
	 * Sets the description.
	 * @param description The description.
	 */
	public void setDescription(String description)
	{
		getWrappedApplication().setDescription(description);
	}

	/**
	 * Sets the display name.
	 * @param name The display name.
	 */
	public void setName(String name)
	{
		getWrappedApplication().setName(name);
	}

   /**
	 * Sets the owner(s).
	 * @param owner The owner(s).
	 */
	public void setOwner(String owner)
	{
		getWrappedApplication().setOwner(owner);
	}

	/**
	 * Sets the root handler that will receive all incoming calls. In general, instances of Restlet, Router, 
	 * Filter or Finder classes will be used as root handlers.
	 * @param root The root handler to use.
	 */
	public void setRoot(UniformInterface root)
	{
		getWrappedApplication().setRoot(root);
	}
}
