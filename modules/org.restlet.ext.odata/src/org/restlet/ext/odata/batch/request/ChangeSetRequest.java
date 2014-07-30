package org.restlet.ext.odata.batch.request;

import java.util.List;

import org.restlet.ext.odata.batch.request.impl.CreateEntityRequest;
import org.restlet.ext.odata.batch.request.impl.DeleteEntityRequest;
import org.restlet.ext.odata.batch.request.impl.UpdateEntityRequest;

/**
 * The Interface ChangeSetRequest defines the methods of batch changeset
 * requests.
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public interface ChangeSetRequest extends ClientBatchRequest {

	/**
	 * Gets the list of requests.
	 * 
	 * @return the reqs
	 */
	public List<ClientBatchRequest> getReqs();

	/**
	 * Adds the request.
	 * 
	 * @param createEntityRequest
	 *            the create entity request
	 * @return the change set request
	 */
	public ChangeSetRequest addRequest(CreateEntityRequest createEntityRequest);

	/**
	 * Adds the request.
	 * 
	 * @param updateEntityRequest
	 *            the update entity request
	 * @return the change set request
	 */
	public ChangeSetRequest addRequest(UpdateEntityRequest updateEntityRequest);

	/**
	 * Adds the request.
	 * 
	 * @param deleteEntityRequest
	 *            the delete entity request
	 * @return the change set request
	 */
	public ChangeSetRequest addRequest(DeleteEntityRequest deleteEntityRequest);

}
