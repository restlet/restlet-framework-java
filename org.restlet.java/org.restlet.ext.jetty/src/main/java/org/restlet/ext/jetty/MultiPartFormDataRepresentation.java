/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;

import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPart.Part;
import org.eclipse.jetty.http.MultiPartConfig;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.Promise;
import org.restlet.data.MediaType;
import org.restlet.engine.header.ContentType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

/**
 * Input representation that can either parse or generate a multipart form data
 * representation depending on which constructor is invoked.
 * 
 * @author Jerome Louvel
 */
public class MultiPartFormDataRepresentation extends InputRepresentation {

	/**
	 * Adds a randomly generated boundary to the media type parameters.
	 * 
	 * @param mediaType The media type to update.
	 * @return The updated media type.
	 */
	public static MediaType addBoundary(MediaType mediaType) {
		return new MediaType(mediaType, "boundary", MultiPart.generateBoundary(null, 24));
	}

	/**
	 * Returns the value of the first mediatype parameter with "boundary" name.
	 * 
	 * @param mediaType The media type that might contain a "boundary" parameter.
	 * @return The value of the first mediatype parameter with "boundary" name.
	 */
	public static String getBoundary(MediaType mediaType) {
		final String result;

		if (mediaType != null) {
			result = mediaType.getParameters().getFirstValue("boundary");
		} else {
			result = null;
		}

		return result;
	}

	/**
	 * The boundary used to separate each part for the parsed or generated form.
	 */
	private volatile String boundary;

	/** The wrapped multipart form data either parsed or to be generated. */
	private volatile List<Part> parts;

	/**
	 * Constructor that wraps multiple parts and generates the content via
	 * {@link #write(OutputStream)} as a {@link MediaType#MULTIPART_FORM_DATA}.
	 * 
	 * @param parts The source parts to use when generating the representation.
	 */
	public MultiPartFormDataRepresentation(Part... parts) {
		super(null, MediaType.MULTIPART_FORM_DATA);
		this.boundary = getMediaType().getParameters().getFirstValue("boundary");
		this.parts = Arrays.asList(parts);
	}

	// TODO Should we support such constructor?
	/**
	 * Constructor that wraps multiple parts and generates the content via
	 * {@link #write(OutputStream)} as a {@link MediaType#MULTIPART_FORM_DATA}.
	 *
	 * @param representations The source parts to use when generating the
	 *                        representation.
	 */
	public MultiPartFormDataRepresentation(Representation... representations) {
		this(MultiPart.generateBoundary(null, 24), representations); // TODO should we generate a random boundary?
	}

	// TODO Should we support such constructor?
	/**
	 * Constructor that wraps multiple parts and generates the content via
	 * {@link #write(OutputStream)} as a {@link MediaType#MULTIPART_FORM_DATA}.
	 *
	 * @param representations The source parts to use when generating the
	 *                        representation.
	 */
	public MultiPartFormDataRepresentation(final String boundary, final Representation... representations) {
		super(null, MediaType.MULTIPART_FORM_DATA);

		final String b = Objects.requireNonNullElse(boundary, getBoundary(getMediaType()));
		this.boundary = Objects.requireNonNullElse(b, MultiPart.generateBoundary(null, 24)); // TODO should we generate
																								// a random boundary?

		this.parts = parts;
	}

	/**
	 * Constructor that parses the content based on a given configuration into
	 * {@link #getParts()}. Uses a default {@link MultiPartConfig}.
	 * 
	 * @param content  The multipart entity to parse which should have a media type
	 *                 based on {@link MediaType#MULTIPART_FORM_DATA}, with a
	 *                 "boundary" parameter.
	 * @param location The location where parsed files are stored for easier access.
	 * @throws IOException
	 */
	public MultiPartFormDataRepresentation(Representation content, Path location) throws IOException {
		this(content, new MultiPartConfig.Builder().location(location).build());
	}

	/**
	 * Constructor that parses the content based on a given configuration into
	 * {@link #getParts()}.
	 * 
	 * @param content The multipart entity to parse which should have a media type
	 *                based on {@link MediaType#MULTIPART_FORM_DATA}, with a
	 *                "boundary" parameter.
	 * @param config  The multipart configuration.
	 * @throws IOException
	 */
	public MultiPartFormDataRepresentation(Representation content, MultiPartConfig config) throws IOException {
		this(ContentType.writeHeader(content), content.getStream(), config);
	}

	/**
	 * Constructor that parses the content based on a given configuration into
	 * {@link #getParts()}.
	 * 
	 * @param contentType The media type that should be based on
	 *                    {@link MediaType#MULTIPART_FORM_DATA}, with a "boundary"
	 *                    parameter.
	 * @param content     The multipart entity to parse.
	 * @param config      The multipart configuration.
	 * @throws IOException
	 */
	public MultiPartFormDataRepresentation(String contentType, InputStream content, MultiPartConfig config)
			throws IOException {
		super(null, MediaType.MULTIPART_FORM_DATA);
		this.boundary = boundary;

		if (content != null) {
			Content.Source contentSource = Content.Source.from(content);
			Attributes.Mapped attributes = new Attributes.Mapped();

			// Convert the request content into parts.
			MultiPartFormData.onParts(contentSource, attributes, contentType, config, new Promise.Invocable<>() {
				@Override
				public void failed(Throwable failure) {
					throw new IllegalStateException("Unable to parse the multipart form data representation", failure);
				}

				@Override
				public InvocationType getInvocationType() {
					return InvocationType.BLOCKING;
				}

				@Override
				public void succeeded(MultiPartFormData.Parts parts) {
					// Store the resulting parts
					MultiPartFormDataRepresentation.this.parts = new ArrayList<>();
					parts.iterator().forEachRemaining(part -> MultiPartFormDataRepresentation.this.parts.add(part));
				}
			});
		}
	}

	/**
	 * Returns the boundary used to separate each part for the parsed or generated
	 * form.
	 * 
	 * @return The boundary used to separate each part for the parsed or generated
	 *         form.
	 */
	public String getBoundary() {
		return boundary;
	}

	/**
	 * Returns the wrapped multipart form data either parsed or to be generated.
	 *
	 * @return The wrapped multipart form data either parsed or to be generated.
	 */
	public List<Part> getParts() {
		return parts;
	}

	/**
	 * Returns an input stream that generates the multipart form data serialization
	 * for the wrapped {@link #getParts()} object.
	 * 
	 * @return An input stream that generates the multipart form data.
	 */
	@Override
	public InputStream getStream() throws IOException {
		MultiPartFormData.ContentSource content = new MultiPartFormData.ContentSource(getBoundary());

		for (Part part : this.parts) {
			content.addPart(part);
		}

		content.close();
		setStream(null);
		return Content.Source.asInputStream(content);
	}

	/**
	 * Sets the boundary used to separate each part for the parsed or generated
	 * form.
	 * 
	 * @param boundary The boundary used to separate each part for the parsed or
	 *                 generated form.
	 */
	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}

}
