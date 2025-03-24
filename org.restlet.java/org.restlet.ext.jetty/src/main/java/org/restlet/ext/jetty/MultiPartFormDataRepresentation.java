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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPart.Part;
import org.eclipse.jetty.http.MultiPartConfig;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.io.content.InputStreamContentSource;
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
     * Creates a #{@link Part} object based on a {@link Representation} plus
     * metadata.
     * 
     * @param name        The name of the part.
     * @param fileName    The client suggest file name for storing the part.
     * @param partContent The part content.
     * @return The Jetty #{@link Part} object created.
     * @throws IOException
     */
    public static Part createPart(String name, String fileName,
            Representation partContent) throws IOException {
        return new MultiPart.ContentSourcePart(name, fileName, HttpFields.EMPTY,
                new InputStreamContentSource(partContent.getStream()));
    }

    /**
     * Returns the value of the first mediatype parameter with "boundary" name.
     * 
     * @param mediaType The media type that might contain a "boundary"
     *                  parameter.
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
     * Constructor that wraps multiple parts and GENERATES the content via
     * {@link #getStream()} as a {@link MediaType#MULTIPART_FORM_DATA}.
     * 
     * Unless a boundary is manually set via {@link #setBoundary(String)}, one
     * will be randomly generated when {@link #getStream()} is invoked.
     * 
     * @param parts The source parts to use when generating the representation.
     */
    public MultiPartFormDataRepresentation(Part... parts) {
        this(Arrays.asList(parts));
    }

    /**
     * Constructor that wraps multiple parts and GENERATES the content via
     * {@link #getStream()} as a {@link MediaType#MULTIPART_FORM_DATA}.
     * 
     * Unless a boundary is manually set via {@link #setBoundary(String)}, one
     * will be randomly generated when {@link #getStream()} is invoked.
     * 
     * @param parts The source parts to use when generating the representation.
     */
    public MultiPartFormDataRepresentation(List<Part> parts) {
        super(null, MediaType.MULTIPART_FORM_DATA);
        this.boundary = null;
        this.parts = parts;
    }

    /**
     * Constructor that PARSES the content based on a given configuration into
     * {@link #getParts()}.
     * 
     * @param multiPartEntity The multipart entity to parse which should have a
     *                        media type based on
     *                        {@link MediaType#MULTIPART_FORM_DATA}, with a
     *                        "boundary" parameter.
     * @param config          The multipart configuration.
     * @throws IOException
     */
    public MultiPartFormDataRepresentation(Representation multiPartEntity,
            MultiPartConfig config) throws IOException {
        this(ContentType.writeHeader(multiPartEntity),
                multiPartEntity.getStream(), config);
    }

    /**
     * Constructor that PARSES the content based on a given configuration into
     * {@link #getParts()}. Uses a default {@link MultiPartConfig}.
     * 
     * @param multiPartEntity The multipart entity to parse which should have a
     *                        media type based on
     *                        {@link MediaType#MULTIPART_FORM_DATA}, with a
     *                        "boundary" parameter.
     * @param storeLocation   The location where parsed files are stored for
     *                        easier access.
     * @throws IOException
     */
    public MultiPartFormDataRepresentation(Representation multiPartEntity,
            Path storeLocation) throws IOException {
        this(multiPartEntity,
                new MultiPartConfig.Builder().location(storeLocation).build());
    }

    /**
     * Constructor that PARSES the content based on a given configuration into
     * {@link #getParts()}.
     * 
     * @param contentType     The media type that should be based on
     *                        {@link MediaType#MULTIPART_FORM_DATA}, with a
     *                        "boundary" parameter.
     * @param multiPartEntity The multipart entity to parse.
     * @param config          The multipart configuration.
     * @throws IOException
     */
    public MultiPartFormDataRepresentation(String contentType,
            InputStream multiPartEntity, MultiPartConfig config)
            throws IOException {
        super(null, MediaType.MULTIPART_FORM_DATA);
        this.boundary = boundary;

        if (multiPartEntity != null) {
            Content.Source contentSource = Content.Source.from(multiPartEntity);
            Attributes.Mapped attributes = new Attributes.Mapped();

            // Convert the request content into parts.
            MultiPartFormData.onParts(contentSource, attributes, contentType,
                    config, new Promise.Invocable<>() {
                        @Override
                        public void failed(Throwable failure) {
                            throw new IllegalStateException(
                                    "Unable to parse the multipart form data representation",
                                    failure);
                        }

                        @Override
                        public InvocationType getInvocationType() {
                            return InvocationType.BLOCKING;
                        }

                        @Override
                        public void succeeded(MultiPartFormData.Parts parts) {
                            // Store the resulting parts
                            MultiPartFormDataRepresentation.this.parts = new ArrayList<>();
                            parts.iterator().forEachRemaining(
                                    part -> MultiPartFormDataRepresentation.this.parts
                                            .add(part));
                        }
                    });
        }
    }

    /**
     * Returns the boundary used to separate each part for the parsed or
     * generated form.
     * 
     * @return The boundary used to separate each part for the parsed or
     *         generated form.
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
     * Returns an input stream that generates the multipart form data
     * serialization for the wrapped {@link #getParts()} object.
     * 
     * If the {@link #getBoundary()} is null, as random one is generated and set
     * as an attribute of the {@link #getMediaType()}.
     * 
     * 
     * @return An input stream that generates the multipart form data.
     */
    @Override
    public InputStream getStream() throws IOException {
        if (getBoundary() == null) {
            setBoundary(MultiPart.generateBoundary(null, 24));
        }

        if (getMediaType() == null) {
            setMediaType(new MediaType(MediaType.MULTIPART_FORM_DATA,
                    "boundary", getBoundary()));
        } else {
            setMediaType(
                    new MediaType(getMediaType(), "boundary", getBoundary()));
        }

        MultiPartFormData.ContentSource content = new MultiPartFormData.ContentSource(
                getBoundary());

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
