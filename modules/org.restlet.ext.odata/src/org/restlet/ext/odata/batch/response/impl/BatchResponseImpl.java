package org.restlet.ext.odata.batch.response.impl;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.ext.odata.batch.response.BatchResponse;

/**
 * The Class BatchResponseImpl is the implementation class for hold the state of
 * the response. <br>
 * It contains properties like status returned by the http
 * response,headers,object entity in case of Create and Get entity requests.
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class BatchResponseImpl implements BatchResponse {

	/** The status. */
	private int status;

	/** The headers. */
	final MultivaluedMap<String, String> headers;

	/** The entity. */
	private Object entity;

	/**
	 * Instantiates a new batch response impl.
	 * 
	 * @param statusCode
	 *            the status code
	 * @param batchHeaders
	 *            the batch headers
	 * @param entity
	 *            the entity
	 */
	public BatchResponseImpl(int statusCode,
			MultivaluedMap<String, String> batchHeaders, Object entity) {
		this.entity = entity;
		this.status = statusCode;
		this.headers = batchHeaders;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.response.BatchResponse#getEntity()
	 */
	@Override
	public Object getEntity() {
		return entity;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.response.BatchResponse#getStatus()
	 */
	@Override
	public int getStatus() {
		return status;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.response.BatchResponse#getHeaders()
	 */
	@Override
	public MultivaluedMap<String, String> getHeaders() {
		return headers;
	}

}
