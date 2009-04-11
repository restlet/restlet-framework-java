/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gae.engine.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import org.restlet.gae.data.MediaType;
import org.restlet.gae.representation.DomRepresentation;
import org.restlet.gae.representation.EmptyRepresentation;
import org.restlet.gae.representation.FileRepresentation;
import org.restlet.gae.representation.InputRepresentation;
import org.restlet.gae.representation.ReaderRepresentation;
import org.restlet.gae.representation.Representation;
import org.restlet.gae.representation.SaxRepresentation;
import org.restlet.gae.representation.StringRepresentation;
import org.restlet.gae.representation.Variant;
import org.restlet.gae.representation.XmlRepresentation;
import org.restlet.gae.resource.UniformResource;
import org.w3c.dom.Document;

/**
 * Converter for the built-in Representation classes.
 * 
 * @author Jerome Louvel
 */
public class DefaultConverter extends ConverterHelper {

    @Override
    public List<Class<?>> getObjectClasses(Variant variant) {
        List<Class<?>> result = null;

        result = addObjectClass(result, String.class);
        result = addObjectClass(result, InputStream.class);
        result = addObjectClass(result, Reader.class);
        result = addObjectClass(result, ReadableByteChannel.class);

        if (variant.getMediaType() != null) {
            MediaType mediaType = variant.getMediaType();

            if (MediaType.APPLICATION_ALL_XML.equals(mediaType)
                    || MediaType.TEXT_XML.equals(mediaType)
                    || MediaType.APPLICATION_ATOMPUB_SERVICE.equals(mediaType)
                    || MediaType.APPLICATION_ATOM.equals(mediaType)
                    || MediaType.APPLICATION_RDF_XML.equals(mediaType)
                    || MediaType.APPLICATION_WADL.equals(mediaType)
                    || MediaType.APPLICATION_XHTML.equals(mediaType)) {
                result = addObjectClass(result, Document.class);
                result = addObjectClass(result, XmlRepresentation.class);
            }

        }

        return result;
    }

    @Override
    public List<Variant> getVariants(Class<?> objectClass) {
        List<Variant> result = null;

        if (String.class.isAssignableFrom(objectClass)
                || StringRepresentation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(MediaType.TEXT_PLAIN));
        } else if (Document.class.isAssignableFrom(objectClass)
                || DomRepresentation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(MediaType.APPLICATION_XML));
            result = addVariant(result, new Variant(MediaType.TEXT_XML));
        } else if (File.class.isAssignableFrom(objectClass)
                || FileRepresentation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(
                    MediaType.APPLICATION_OCTET_STREAM));
        } else if (InputStream.class.isAssignableFrom(objectClass)
                || InputRepresentation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(
                    MediaType.APPLICATION_OCTET_STREAM));
        } else if (Reader.class.isAssignableFrom(objectClass)
                || ReaderRepresentation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(MediaType.TEXT_PLAIN));
        } else if (Representation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(
                    MediaType.APPLICATION_OCTET_STREAM));
        } else if (SaxRepresentation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(MediaType.APPLICATION_XML));
            result = addVariant(result, new Variant(MediaType.TEXT_XML));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation representation, Class<T> targetClass,
            UniformResource resource) throws IOException {
        Object result = null;

        if (representation != null) {
            if (targetClass != null) {
                if (targetClass.isAssignableFrom(representation.getClass())) {
                    result = (T) representation;
                } else {
                    if (String.class.isAssignableFrom(targetClass)) {
                        result = representation.getText();
                    } else if (StringRepresentation.class
                            .isAssignableFrom(targetClass)) {
                        result = new StringRepresentation(representation
                                .getText(), representation.getMediaType());
                    } else if (EmptyRepresentation.class
                            .isAssignableFrom(targetClass)) {
                        result = null;
                    } else if (Document.class.isAssignableFrom(targetClass)) {
                        result = new DomRepresentation(representation)
                                .getDocument();
                    } else if (DomRepresentation.class
                            .isAssignableFrom(targetClass)) {
                        result = new DomRepresentation(representation);
                    } else if (File.class.isAssignableFrom(targetClass)) {
                        if (representation instanceof FileRepresentation) {
                            result = ((FileRepresentation) representation)
                                    .getFile();
                        }
                    } else if (InputStream.class.isAssignableFrom(targetClass)) {
                        result = representation.getStream();
                    } else if (InputRepresentation.class
                            .isAssignableFrom(targetClass)) {
                        result = new InputRepresentation(representation
                                .getStream());
                    } else if (Reader.class.isAssignableFrom(targetClass)) {
                        result = representation.getReader();
                    } else if (ReaderRepresentation.class
                            .isAssignableFrom(targetClass)) {
                        result = new ReaderRepresentation(representation
                                .getReader());
                    } else if (SaxRepresentation.class
                            .isAssignableFrom(targetClass)) {
                        result = new SaxRepresentation(representation);
                    }
                }
            }

            if (result instanceof Representation) {
                Representation resultRepresentation = (Representation) result;

                // Copy the variant metadata
                resultRepresentation.setCharacterSet(representation
                        .getCharacterSet());
                resultRepresentation
                        .setMediaType(representation.getMediaType());
                resultRepresentation
                        .setEncodings(representation.getEncodings());
                resultRepresentation
                        .setLanguages(representation.getLanguages());
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object object,
            Variant targetVariant, UniformResource resource) throws IOException {
        Representation result = null;

        if (object instanceof String) {
            result = new StringRepresentation((String) object);
        } else if (object instanceof Document) {
            result = new DomRepresentation(targetVariant == null ? null
                    : targetVariant.getMediaType(), (Document) object);
        } else if (object instanceof File) {
            result = new FileRepresentation((File) object,
                    targetVariant == null ? null : targetVariant.getMediaType());
        } else if (object instanceof InputStream) {
            result = new InputRepresentation((InputStream) object,
                    targetVariant == null ? null : targetVariant.getMediaType());
        } else if (object instanceof Reader) {
            result = new ReaderRepresentation((Reader) object,
                    targetVariant == null ? null : targetVariant.getMediaType());
        } else if (object instanceof Representation) {
            result = (Representation) object;
        }

        if ((result != null) && (targetVariant != null)) {
            // Copy the variant metadata
            result.setCharacterSet(targetVariant.getCharacterSet());
            result.setMediaType(targetVariant.getMediaType());
            result.setEncodings(targetVariant.getEncodings());
            result.setLanguages(targetVariant.getLanguages());
        }

        return result;
    }
}
