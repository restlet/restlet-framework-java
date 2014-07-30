package org.restlet.ext.odata.batch.util;

/**
 * Interface to hold constants for Restlet batch implementation.
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 * 
 */
public interface BatchConstants {

	/** The Constant BATCH_ENDPOINT_URI. */
	String BATCH_ENDPOINT_URI = "$batch";

	String ODATA_VERSION_V3 = "V3";

	String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

	String GET_METADATA = "getMetadata";

	String BATCH_BOUNDARY = "boundary";
	
	/** The Constant STATUS. */
	int HTTP_STATUS_OK = 200;
	
	String CHANGESET_UNDERSCORE = "changeset_";

	String BATCH_UNDERSCORE = "batch_";

	String NEW_LINE = System.getProperty("line.separator");
	
	String NEW_LINE_BATCH_START = new StringBuilder().append(NEW_LINE).append("--").toString();
	
	String NEW_LINE_BATCH_END = new StringBuilder().append("--").append(NEW_LINE).toString();
	
	String FORMAT_TYPE_CHARSET_UTF8 = ";charset=utf-8";
}
