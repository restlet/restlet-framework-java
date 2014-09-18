package org.restlet.ext.odata.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.ext.odata.Service;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * This class stores the Stream data reference and its contentType.One can read,
 * Create and update stream data by using this class.<br>
 * <br>
 * 
 * The {@link InputStream} to be read as Media Link Entry(MLE)/Stream from the
 * server is fetched lazily using a call to the {@link #getInputStream(Service)}
 * method. Please note that it is essential to pass the {@link Service} to the
 * method as the service instance holds the authenticated credentials that needs
 * to be passed as part of this client request.<br>
 * <br>
 * 
 * <b>DO NOT</b> use the {@link #getInputStream()} method for lazy fetching a
 * stream; this method is used internally by the framework while saving the
 * stream.When saving a MLE, please use the {@link #setInputStream(InputStream)}
 * method to set the reference to the {@link InputStream} that you want to
 * persist on the server.<br>
 * <br>
 * 
 * <b>NOTE</b>
 * <ol>
 * <li>If you need to use blocking {@link InputStream} continous inputstream as
 * in case of protocol buffers; please use the <code>org.restlet.ext.net</code>
 * connector by placing the <i>org.restlet.ext.net.jar</i> file in the
 * classpath.</li>
 * <li>If you need to use non-blocking I/O to read the stream using buffered
 * data reader as in the case you are reading files/documents; please use the
 * <code>org.restlet.ext.httpclient</code> connector by placing the
 * <i>org.restlet.ext.httpclient.jar</i> file and it's dependencies in the
 * classpath.</li>
 * <li>Note that at any point of time the client can use only 1 connector and
 * they have to be mutually exclusive.</li>
 * </ol>
 */
public class StreamReference extends Reference {

	/** The input stream. */
	private InputStream inputStream;

	/** The content type. */
	private String contentType;

	/** isUpdateStreamData needs to be set to true for Stream Update */
	private boolean isUpdateStreamData;

	/**
	 * Instantiates a new stream reference. This method is used to create
	 * StreamReference with the URL.
	 * 
	 * @param baseRef
	 *            the base ref
	 * @param uriRef
	 *            the uri ref
	 */
	public StreamReference(Reference baseRef, String uriRef) {
		super(baseRef, uriRef);
	}

	/**
	 * Instantiates a new stream reference. Use this to instantiates stream
	 * reference for doing create.
	 * 
	 * @param contentType
	 *            the content type
	 * @param inputStream
	 *            the input stream
	 * @param isCreate
	 *            the is create
	 */
	public StreamReference(String contentType, InputStream inputStream) {
		this.contentType = contentType;
		this.inputStream = inputStream;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * This method returns the stream that provides the handle to the data being
	 * pushed back from the server.<br>
	 * <br>
	 * The client program should use the <b>org.restlet.ext.net</b> connector
	 * when the client depends on the entire inputstream being available for
	 * processing say in case of processing Google Protocol Buffer streams.<br>
	 * <br>
	 * 
	 * The client program should use the <b>org.restlet.ext.httpclient</b>
	 * connector when the client is downloading file or document based streams
	 * that don't need to be available for client program to process as
	 * continuous input stream.
	 * 
	 * @param service
	 *            the service
	 * @return the inputStream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public InputStream getInputStream(Service service) throws IOException {

		try {
			ClientResource clientResource = service.createResource(this);
			Representation representation = clientResource.get();
			if (representation != null) {
				// Provide a stream based on what is returned by the server
				this.inputStream = representation.getStream();
			}
		} catch (IOException ioException) {
			Context.getCurrentLogger().log(
					Level.WARNING,
					"Exception while retrieving the non blocking streaming data: "
							+ ioException.getMessage(), ioException);
			throw ioException;
		}
		return this.inputStream;
	}

	/**
	 * @param inputStream
	 *            the inputStream to set
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	/**
	 * Gets the input stream.
	 * 
	 * @return the input stream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Checks if is update stream data.
	 * 
	 * @return the isUpdateStreamData
	 */
	public boolean isUpdateStreamData() {
		return isUpdateStreamData;
	}

	/**
	 * Sets the update stream data.
	 * 
	 * @param isUpdateStreamData
	 *            the isUpdateStreamData to set
	 */
	public void setUpdateStreamData(boolean isUpdateStreamData) {
		this.isUpdateStreamData = isUpdateStreamData;
	}

}
