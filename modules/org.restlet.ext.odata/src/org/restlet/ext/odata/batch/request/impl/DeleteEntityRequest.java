package org.restlet.ext.odata.batch.request.impl;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.batch.util.RestletBatchRequestHelper;
import org.restlet.resource.ClientResource;

/**
 * The Class DeleteEntityRequest is used with DELETE method to delete an entity
 * using batch..
 * 
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class DeleteEntityRequest extends RestletBatchRequest {

	/** The entry. */
	private Entry entry;

	/** The entity sub path. */
	private String entitySubPath;

	/**
	 * Instantiates a new delete entity request.
	 * 
	 * @param service
	 *            the service
	 * @param entity
	 *            the entity
	 * @throws Exception
	 *             the exception
	 */
	public DeleteEntityRequest(Service service, Object entity) throws Exception {
		super(service, entity.getClass(), Method.DELETE);
		this.entry = service.toEntry(entity);
		this.entitySubPath = RestletBatchRequestHelper.getEntitySubPath(
				service, entity);

	}

	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.ClientBatchRequest#format(org.restlet.data.MediaType)
	 */
	@Override
	public String format(MediaType formatType) {
		ClientResource cr = getClientResource(this.entitySubPath);
		return RestletBatchRequestHelper.formatSingleRequest(cr.getRequest(),
				formatType);
	}
	
	/**
	 * Gets the entry.
	 * 
	 * @return the entry
	 */
	public Entry getEntry() {
		return entry;
	}

	/**
	 * Sets the entry.
	 * 
	 * @param entry
	 *            the new entry
	 */
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
}
