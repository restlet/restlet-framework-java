package org.restlet.ext.odata.batch.request.impl;

import org.restlet.data.Method;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.batch.request.BatchProperty;
import org.restlet.ext.odata.batch.util.RestletBatchRequestHelper;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.resource.ClientResource;


/**
 * Abstract implementation of the RestletBatchRequest interface to handle the
 * mechanism for inferring the entity type from the entity class using the
 * service, for CRUD requests.<br>
 * 
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public abstract class RestletBatchRequest implements BatchProperty {

	/** The service. */
	private Service service;

	/** The entity class. */
	private Class<?> entityClass;

	/** The entity type. */
	private EntityType entityType;

	/** The method. */
	private Method method;

	/** The entity set name. */
	private String entitySetName;

	/**
	 * Instantiates a new restlet batch request impl.
	 *
	 * @param service the service
	 * @param entityClass the entity class
	 * @param method the method
	 * @throws Exception the exception
	 */
	public RestletBatchRequest(Service service, Class<?> entityClass,
			Method method) throws Exception {
		this.setService(service);
		this.setEntityClass(entityClass);
		this.entityType = inferEntityType(entityClass);
		this.method = method;
		this.setEntitySetName(RestletBatchRequestHelper
				.validateAndReturnEntitySetName(service, entityClass));
	}

	/**
	 * Method to get the entity type from the entity class.
	 * 
	 * @param entityClass
	 *            the entity class
	 * @return the entity type
	 */
	private EntityType inferEntityType(Class<?> entityClass) {
		Metadata metadata = (Metadata) service.getMetadata();
		return metadata.getEntityType(entityClass);
	}
	
	/**
	 * Gets the client resource.
	 *
	 * @param relativePath the relative path
	 * @return the client resource
	 */
	public ClientResource getClientResource(String relativePath) {
		ClientResource cr = this.getService().createResource(relativePath);
		cr.getRequest().setMethod(this.getMethod());
		return cr;
	}

	/**
	 * Gets the entity class.
	 * 
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * Sets the entity class.
	 * 
	 * @param entityClass
	 *            the entityClass to set
	 */
	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * Gets the service.
	 * 
	 * @return the service
	 */
	public Service getService() {
		return service;
	}

	/**
	 * Sets the service.
	 * 
	 * @param service
	 *            the service to set
	 */
	public void setService(Service service) {
		this.service = service;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.BatchProperty#getEntityType()
	 */
	@Override
	public EntityType getEntityType() {
		return entityType;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.BatchProperty#getMethod()
	 */
	@Override
	public Method getMethod() {
		return method;
	}

	/**
	 * Sets the method.
	 * 
	 * @param method
	 *            the new method
	 */
	protected void setMethod(Method method) {
		this.method = method;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.BatchProperty#getEntitySetName()
	 */
	@Override
	public String getEntitySetName() {
		return entitySetName;
	}

	/**
	 * Sets the entity set name.
	 * 
	 * @param entitySetName
	 *            the new entity set name
	 */
	public void setEntitySetName(String entitySetName) {
		this.entitySetName = entitySetName;
	}
}
