package org.restlet.ext.odata.batch.response;

/**
 * The Interface ChangeSetResponse exposes methods to hold the CUD responses
 * within a batch response.
 * 
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public interface ChangeSetResponse extends BatchResponse {

	/**
	 * Adds the.
	 * 
	 * @param singleResponse
	 *            the single response
	 */
	void add(BatchResponse singleResponse);

}
