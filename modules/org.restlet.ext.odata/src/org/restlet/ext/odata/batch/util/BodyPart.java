package org.restlet.ext.odata.batch.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MultivaluedMap;

import org.jvnet.mimepull.MIMEPart;
import org.restlet.data.MediaType;

/**
 * The Class BodyPart is sub part of the Multipart. <br>
 * Body part represents a single attachment from the Mime message attachments.<br>
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class BodyPart implements Closeable {

	/** The mime part. */
	private MIMEPart mimePart;

	/** The media type. */
	private MediaType mediaType;

	/** The entity. */
	private Object entity;

	/** The headers. */
	private MultivaluedMap<String, String> headers = new HeaderMap();

	/** The content disposition. */
	private String contentDisposition;

	/** The parent. */
	private Multipart parent;

	/**
	 * Instantiates a new body part.
	 * 
	 * @param mimePart
	 *            the mime part
	 */
	public BodyPart(MIMEPart mimePart) {
		this.mimePart = mimePart;
	}

	/**
	 * Instantiates a new body part.
	 */
	public BodyPart() {

	}

	/**
	 * Gets the entity.
	 * 
	 * @return the entity
	 */
	public Object getEntity() {
		return entity;
	}

	/**
	 * Sets the entity.
	 * 
	 * @param entity
	 *            the new entity
	 */
	public void setEntity(Object entity) {
		this.entity = entity;
	}

	/**
	 * Gets the headers.
	 * 
	 * @return the headers
	 */
	public MultivaluedMap<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Sets the headers.
	 * 
	 * @param headers
	 *            the headers
	 */
	public void setHeaders(MultivaluedMap<String, String> headers) {
		this.headers = headers;
	}

	/**
	 * Gets the content disposition.
	 * 
	 * @return the content disposition
	 */
	public String getContentDisposition() {
		return contentDisposition;
	}

	/**
	 * Sets the content disposition.
	 * 
	 * @param contentDisposition
	 *            the new content disposition
	 */
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	/**
	 * Gets the media type.
	 * 
	 * @return the media type
	 */
	public MediaType getMediaType() {
		return mediaType;
	}

	/**
	 * Sets the media type.
	 * 
	 * @param mediaType
	 *            the new media type
	 */
	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	/**
	 * Sets the parent and add the current bodyPart as child.
	 * 
	 * @param multipart
	 *            the new parent
	 */
	public void setParent(Multipart multipart) {
		this.parent = multipart;
	}

	/**
	 * Gets the parent.
	 * 
	 * @return the parent
	 */
	public Multipart getParent() {
		return parent;
	}

	/**
	 * Gets the input stream.
	 * 
	 * @return the input stream
	 */
	public InputStream getInputStream() {
		return this.mimePart.read();
	}

	/**
	 * Clean up temporary file(s), if any were utilized.
	 */
	public void cleanup() {
		mimePart.close();
	}

	// Closeable
	/**
	 * Defer to {@link #cleanup}.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void close() throws IOException {
		cleanup();
	}

}
