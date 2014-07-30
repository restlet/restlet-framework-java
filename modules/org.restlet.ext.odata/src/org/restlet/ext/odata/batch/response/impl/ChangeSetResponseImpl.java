package org.restlet.ext.odata.batch.response.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.ext.odata.batch.response.BatchResponse;
import org.restlet.ext.odata.batch.response.ChangeSetResponse;
import org.restlet.ext.odata.batch.util.BatchConstants;

/**
 * The Class ChangeSetResponseImpl is implementation class for Changeset
 * response. <br>
 * The changeset response can have a list of multiple responses within,based on
 * the requests sent in a chnageset of a batch.<br>
 * 
 * copyright 2014 Halliburton<br>
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class ChangeSetResponseImpl implements ChangeSetResponse {

	/** The responses. */
	List<BatchResponse> responses = new ArrayList<BatchResponse>();

	/** The status. */
	int status = BatchConstants.HTTP_STATUS_OK;

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.response.BatchResponse#getEntity()
	 */
	@Override
	public Object getEntity() {
		return responses;
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
		return null;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.response.ChangeSetResponse#add(org.restlet.ext.odata.batch.response.BatchResponse)
	 */
	@Override
	public void add(BatchResponse singleResponse) {
		responses.add(singleResponse);
	}

}
