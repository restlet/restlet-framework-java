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

import org.restlet.Application;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.service.ConverterService;
import org.restlet.util.MapModel;

/**
 * Generic message exchanged between client and server connectors.
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Message
{
	/** The modifiable attributes map. */
	private MapModel attributes;

	/** The payload of the message. */
	private Representation entity;

	/**
	 * Constructor.
	 */
	public Message()
	{
		this((Representation) null);
	}

	/**
	 * Constructor.
	 * @param entity The payload of the message.
	 */
	public Message(Representation entity)
	{
		this.attributes = null;
		this.entity = entity;
	}

	/**
	 * Returns a modifiable attributes map that can be used by developers to save information relative
	 * to the message. This is an easier alternative to the creation of a wrapper instance around the 
	 * whole message.<br/>
	 * <br/>
	 * In addition, this map is a shared space between the developer and the connectors. In this case, 
	 * it is used to exchange information that is not uniform across all protocols and couldn't therefore
	 * be directly included in the API. For this purpose, all attribute names starting with "org.restlet"
	 * are reserved. Currently the following attributes are used:
	 * <table>
	 * 	<tr>
	 * 		<th>Attribute name</th>
	 * 		<th>Class name</th>
	 * 		<th>Description</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>org.restlet.http.headers</td>
	 * 		<td>org.restlet.data.ParameterList</td>
	 * 		<td>Server HTTP connectors must provide all request headers and client HTTP connectors must 
	 * provide all response headers, exactly as they were received. In addition, developers can also use this 
	 * attribute to specify <b>non-standard</b> headers that should be added to the request or to the response.
	 * </td>
	 * 	</tr>
	 *	</table>
	 * Adding standard HTTP headers is forbidden because it could conflict with the connector's internal 
	 * behavior, limit portability or prevent future optimizations.</td>
	 * @return The modifiable attributes map.
	 */
	public MapModel getAttributes()
	{
		if (this.attributes == null)
		{
			this.attributes = new MapModel();
		}

		return this.attributes;
	}

	/**
	 * Returns the entity representation.
	 * @return The entity representation.
	 */
	public Representation getEntity()
	{
		return this.entity;
	}

	/**
	 * Returns the entity as a form.<br/>
	 * Note that this triggers the parsing of the entity.<br/>
	 * This method and the related getEntity*() methods can only be invoked once.
	 * @return The entity as a form.
	 */
	public Form getEntityAsForm()
	{
		return new Form(getEntity());
	}

	/**
	 * Returns the entity as a higher-level object. This object is created by the Application's converter service.
	 * If you want to use this method to facilitate the processing of request entities, you need to provide a 
	 * custom implementation of the ConverterService class, overriding the toObject(Representation) method.
	 * <br/>
	 * Note that this triggers the parsing of the entity.<br/>
	 * This method and the related getEntity*() methods can only be invoked once.
	 * @return The entity as a form.
	 */
	public Object getEntityAsObject()
	{
		return getConverterService().toObject(getEntity());
	}

	/**
	 * Returns the metadata service.
	 * @return The metadata service.
	 */
	private ConverterService getConverterService()
	{
		ConverterService result = null;
		Application application = (Application) getAttributes().get(
				Application.class.getCanonicalName());

		if (application != null)
		{
			result = application.getConverterService();
		}
		else
		{
			result = new ConverterService();
		}

		return result;
	}

	/**
	 * Indicates if a content is available and can be sent. Several conditions must be met: the content 
	 * must exists and have some available data.
	 * @return True if a content is available and can be sent.
	 */
	public boolean isEntityAvailable()
	{
		return (getEntity() != null) && (getEntity().getSize() > 0)
				&& getEntity().isAvailable();
	}

	/**
	 * Sets the entity from a higher-level object. This object is converted to a representation using the 
	 * Application's converter service. If you want to use this method to facilitate the setting of entities,
	 * you need to provide a custom implementation of the ConverterService class, overriding the 
	 * toRepresentation(Object) method.
	 * @param object The higher-level object.
	 */
	public void setEntity(Object object)
	{
		if (object instanceof Representation)
		{
			setEntity((Representation) object);
		}
		else
		{
			setEntity(getConverterService().toRepresentation(object));
		}
	}

	/**
	 * Sets the entity representation.
	 * @param entity The entity representation.
	 */
	public void setEntity(Representation entity)
	{
		this.entity = entity;
	}

	/**
	 * Sets a textual entity.
	 * @param value The represented string.
	 * @param mediaType The representation's media type.
	 */
	public void setEntity(String value, MediaType mediaType)
	{
		setEntity(new StringRepresentation(value, mediaType));
	}

}
