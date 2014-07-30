package org.restlet.ext.odata.batch.response;

import javax.ws.rs.core.MultivaluedMap;

/**
 * The Interface BatchResponse is the top level response interface for a batch.
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public interface BatchResponse {

	/**
	 * Gets the entity.
	 * 
	 * @return the entity
	 */
	Object getEntity();

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	int getStatus();

	/**
	 * Gets the headers.
	 * 
	 * @return the headers
	 */
	MultivaluedMap<String, String> getHeaders();

}
