package org.restlet.ext.odata.batch.request.impl;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.batch.util.BatchConstants;
import org.restlet.ext.odata.batch.util.RestletBatchRequestHelper;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

/**
 * The Class CreateEntityRequest is used with POST method to create an entity
 * using batch.
 * 
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class CreateEntityRequest extends RestletBatchRequest {

	/** The entry. */
	private Entry entry;

	/**
	 * Instantiates a new creates the entity request.
	 * 
	 * @param service
	 *            the service
	 * @param entity
	 *            the entity
	 * @throws Exception
	 *             the exception
	 */
	public CreateEntityRequest(Service service, Object entity) throws Exception {
		super(service, (Class<?>) entity.getClass(), Method.POST);
		this.entry = service.toEntry(entity);
	}

	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.ClientBatchRequest#format(org.restlet.data.MediaType)
	 */
	@Override
	public String format(MediaType formatType) {
		ClientResource cr = getClientResource(this.getEntitySetName());
		StringRepresentation strRepresent = RestletBatchRequestHelper
				.getStringRepresentation(this.getService(),
						this.getEntitySetName(), this.entry,formatType);
		StringBuilder sb = new StringBuilder();
		sb.append(RestletBatchRequestHelper.formatSingleRequest(
				cr.getRequest(), formatType));
		// set content-length
		sb.append(HeaderConstants.HEADER_CONTENT_LENGTH).append(": ")
				.append(strRepresent.getSize()).append(BatchConstants.NEW_LINE);
		sb.append(BatchConstants.NEW_LINE).append(BatchConstants.NEW_LINE);
		sb.append(strRepresent.getText()).append(BatchConstants.NEW_LINE);
		return sb.toString();
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
