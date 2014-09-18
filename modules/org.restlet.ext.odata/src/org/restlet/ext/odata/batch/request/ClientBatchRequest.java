package org.restlet.ext.odata.batch.request;

import org.restlet.data.MediaType;

/**
 * The Interface ClientBatchRequest forms the base interface for all types of
 * requests like CRUD and changeset requests.
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 * 
 */
public interface ClientBatchRequest {

	/**
	 * Format.
	 * 
	 * @param formatType
	 *            the format type
	 * @return the string
	 */
	public String format(MediaType formatType);

}
