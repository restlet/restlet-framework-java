package org.restlet.ext.odata.batch.request;

import java.util.List;

import org.restlet.ext.odata.batch.request.impl.GetEntityRequest;
import org.restlet.ext.odata.batch.response.BatchResponse;

/**
 * The Interface BatchRequest is the base interface for batch. <br>
 * It has methods to add requests like changeset requests and CRUD requets.
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 * 
 */
public interface BatchRequest {

	/**
	 * Used for Reading the entities.
	 * 
	 * @param getEntityRequest
	 *            the get entity request
	 * @return the batch request impl
	 */
	public BatchRequest addRequest(GetEntityRequest getEntityRequest);

	/**
	 * Used for adding changeSetRequest.
	 * 
	 * @param changeSetRequest
	 *            the change set request
	 * @return the batch request impl
	 */
	public BatchRequest addRequest(ChangeSetRequest changeSetRequest);

	/**
	 * To execute the batch request.
	 * 
	 * @return the list of batch response
	 */
	public List<BatchResponse> execute();

}
