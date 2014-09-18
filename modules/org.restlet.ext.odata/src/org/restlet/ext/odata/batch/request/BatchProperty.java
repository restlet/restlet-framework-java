package org.restlet.ext.odata.batch.request;

import org.restlet.data.Method;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.internal.edm.EntityType;

/**
 * The Interface BatchProperty handles the properties of a CRUD requests.
 * 
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public interface BatchProperty extends ClientBatchRequest {

	/**
	 * Gets the entity class.
	 * 
	 * @return the entityClass
	 */
	public Class<?> getEntityClass();

	/**
	 * Gets the service.
	 * 
	 * @return the service
	 */
	public Service getService();

	/**
	 * Gets the entity type.
	 * 
	 * @return the entity type
	 */
	public EntityType getEntityType();

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public Method getMethod();

	/**
	 * Gets the entity set name.
	 * 
	 * @return the entity set name
	 */
	public String getEntitySetName();

}
