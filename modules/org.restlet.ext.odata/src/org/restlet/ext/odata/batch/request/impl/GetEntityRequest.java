package org.restlet.ext.odata.batch.request.impl;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.odata.Query;
import org.restlet.ext.odata.batch.util.RestletBatchRequestHelper;
import org.restlet.resource.ClientResource;

/**
 * The Class GetEntityRequest is specifically used within a batch request,to
 * fetch the entity.
 * 
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class GetEntityRequest extends RestletBatchRequest {

	/** The query. */
	private Query<?> query;

	/**
	 * Instantiates a new gets the entity request.
	 * 
	 * @param service
	 *            the service
	 * @param query
	 *            the query
	 * @throws Exception
	 */
	public GetEntityRequest(Query<?> query) throws Exception {
		super(query.getService(), query.getEntityClass(), Method.GET);
		this.query = query;
	}

	/**
	 * Gets the query.
	 * 
	 * @return the query
	 */
	public Query<?> getQuery() {
		return query;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.ClientBatchRequest#format(org.restlet.data.MediaType)
	 */
	@Override
	public String format(MediaType formatType) {
		ClientResource cr = getClientResource(this.query.getSubpath());
		return RestletBatchRequestHelper.formatSingleRequest(cr.getRequest(),
				formatType);
	}

	
}
