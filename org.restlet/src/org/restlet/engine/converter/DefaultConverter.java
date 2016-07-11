/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.ReaderRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Converter for the built-in Representation classes.
 * 
 * @author Jerome Louvel
 */
public class DefaultConverter extends ConverterHelper {

    /** Neutral variant. */
    private static final VariantInfo VARIANT_ALL = new VariantInfo(
            MediaType.ALL);

    /** Web form variant. */
    private static final VariantInfo VARIANT_FORM = new VariantInfo(
            MediaType.APPLICATION_WWW_FORM);

    /** Octet stream variant. */
    private static final VariantInfo VARIANT_OBJECT = new VariantInfo(
            MediaType.APPLICATION_JAVA_OBJECT);

    /** Octet stream variant. */
    private static final VariantInfo VARIANT_OBJECT_XML = new VariantInfo(
            MediaType.APPLICATION_JAVA_OBJECT_XML);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;
        result = addObjectClass(result, String.class);
        result = addObjectClass(result, InputStream.class);
        result = addObjectClass(result, Reader.class);
        result = addObjectClass(result, ReadableByteChannel.class);

        if (source.getMediaType() != null) {
            MediaType mediaType = source.getMediaType();

            if ((ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED && MediaType.APPLICATION_JAVA_OBJECT
                    .equals(mediaType))
                    || (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED && MediaType.APPLICATION_JAVA_OBJECT_XML
                            .equals(mediaType))) {
                result = addObjectClass(result, Object.class);
            } else if (MediaType.APPLICATION_WWW_FORM.equals(mediaType)) {
                result = addObjectClass(result, Form.class);
            }
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null) {
            if (String.class.isAssignableFrom(source)
                    || StringRepresentation.class.isAssignableFrom(source)) {
                result = addVariant(result, VARIANT_ALL);
            } else if (File.class.isAssignableFrom(source)
                    || FileRepresentation.class.isAssignableFrom(source)) {
                result = addVariant(result, VARIANT_ALL);
            } else if (InputStream.class.isAssignableFrom(source)
                    || InputRepresentation.class.isAssignableFrom(source)) {
                result = addVariant(result, VARIANT_ALL);
            } else if (Reader.class.isAssignableFrom(source)
                    || ReaderRepresentation.class.isAssignableFrom(source)) {
                result = addVariant(result, VARIANT_ALL);
            } else if (Representation.class.isAssignableFrom(source)) {
                result = addVariant(result, VARIANT_ALL);
            } else if (Form.class.isAssignableFrom(source)) {
                result = addVariant(result, VARIANT_FORM);
            } else if (Serializable.class.isAssignableFrom(source)) {
                if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED) {
                    result = addVariant(result, VARIANT_OBJECT);
                }
                if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED) {
                    result = addVariant(result, VARIANT_OBJECT_XML);
                }
            }
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof String) {
            result = 1.0F;
        } else if (source instanceof File) {
            result = 1.0F;
        } else if (source instanceof Form) {
            if ((target != null)
                    && MediaType.APPLICATION_WWW_FORM.isCompatible(target
                            .getMediaType())) {
                result = 1.0F;
            } else {
                result = 0.6F;
            }
        } else if (source instanceof InputStream) {
            result = 1.0F;
        } else if (source instanceof Reader) {
            result = 1.0F;
        } else if (source instanceof Representation) {
            result = 1.0F;
        } else if (source instanceof Serializable) {
            if (target != null) {
                if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                        && MediaType.APPLICATION_JAVA_OBJECT.equals(target
                                .getMediaType())) {
                    result = 1.0F;
                } else if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                        && MediaType.APPLICATION_JAVA_OBJECT
                                .isCompatible(target.getMediaType())) {
                    result = 0.6F;
                } else if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                        && MediaType.APPLICATION_JAVA_OBJECT_XML.equals(target
                                .getMediaType())) {
                    result = 1.0F;
                } else if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                        && MediaType.APPLICATION_JAVA_OBJECT_XML
                                .isCompatible(target.getMediaType())) {
                    result = 0.6F;
                }
            } else if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED) {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        float result = -1.0F;

        if (target != null) {
            if (target.isAssignableFrom(source.getClass())) {
                result = 1.0F;
            } else if (String.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (StringRepresentation.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (EmptyRepresentation.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (File.class.isAssignableFrom(target)) {
                if (source instanceof FileRepresentation) {
                    result = 1.0F;
                }
            } else if (Form.class.isAssignableFrom(target)) {
                if (MediaType.APPLICATION_WWW_FORM.isCompatible(source
                        .getMediaType())) {
                    result = 1.0F;
                } else {
                    result = 0.5F;
                }
            } else if (InputStream.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (InputRepresentation.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (Reader.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (ReaderRepresentation.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (Serializable.class.isAssignableFrom(target)
                    || target.isPrimitive()) {
                if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                        && MediaType.APPLICATION_JAVA_OBJECT.equals(source
                                .getMediaType())) {
                    result = 1.0F;
                } else if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                        && MediaType.APPLICATION_JAVA_OBJECT
                                .isCompatible(source.getMediaType())) {
                    result = 0.6F;
                } else if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                        && MediaType.APPLICATION_JAVA_OBJECT_XML.equals(source
                                .getMediaType())) {
                    result = 1.0F;
                } else if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                        && MediaType.APPLICATION_JAVA_OBJECT_XML
                                .isCompatible(source.getMediaType())) {
                    result = 0.6F;
                } else {
                    result = 0.5F;
                }
            }
        } else if (source instanceof ObjectRepresentation<?>) {
            result = 1.0F;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        Object result = null;

        if (target != null) {
            if (target.isAssignableFrom(source.getClass())) {
                result = source;
            } else if (String.class.isAssignableFrom(target)) {
                result = source.getText();
            } else if (StringRepresentation.class.isAssignableFrom(target)) {
                result = new StringRepresentation(source.getText(),
                        source.getMediaType());
            } else if (EmptyRepresentation.class.isAssignableFrom(target)) {
                result = null;
            } else if (File.class.isAssignableFrom(target)) {
                if (source instanceof FileRepresentation) {
                    result = ((FileRepresentation) source).getFile();
                } else {
                    result = null;
                }
            } else if (Form.class.isAssignableFrom(target)) {
                result = new Form(source);
            } else if (InputStream.class.isAssignableFrom(target)) {
                result = source.getStream();
            } else if (InputRepresentation.class.isAssignableFrom(target)) {
                result = new InputRepresentation(source.getStream());
            } else if (Reader.class.isAssignableFrom(target)) {
                result = source.getReader();
            } else if (ReaderRepresentation.class.isAssignableFrom(target)) {
                result = new ReaderRepresentation(source.getReader());
            } else if (Serializable.class.isAssignableFrom(target)
                    || target.isPrimitive()) {
                if (source instanceof ObjectRepresentation<?>) {
                    result = ((ObjectRepresentation<?>) source).getObject();
                } else {
                    try {
                        result = new ObjectRepresentation<Serializable>(source)
                                .getObject();
                    } catch (Exception e) {
                        IOException ioe = new IOException(
                                "Unable to create the Object representation");
                        ioe.initCause(e);
                        throw ioe;
                    }
                }
            }
        } else if (source instanceof ObjectRepresentation<?>) {
            result = ((ObjectRepresentation<?>) source).getObject();
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) throws IOException {
        Representation result = null;

        if (source instanceof String) {
            result = new StringRepresentation((String) source,
                    MediaType.getMostSpecific(target.getMediaType(),
                            MediaType.TEXT_PLAIN));
        } else if (source instanceof File) {
            result = new FileRepresentation((File) source,
                    MediaType.getMostSpecific(target.getMediaType(),
                            MediaType.APPLICATION_OCTET_STREAM));
        } else if (source instanceof Form) {
            result = ((Form) source).getWebRepresentation();
        } else if (source instanceof InputStream) {
            result = new InputRepresentation((InputStream) source,
                    MediaType.getMostSpecific(target.getMediaType(),
                            MediaType.APPLICATION_OCTET_STREAM));
        } else if (source instanceof Reader) {
            result = new ReaderRepresentation((Reader) source,
                    MediaType.getMostSpecific(target.getMediaType(),
                            MediaType.TEXT_PLAIN));
        } else if (source instanceof Representation) {
            result = (Representation) source;
        } else if (source instanceof Serializable) {
            result = new ObjectRepresentation<Serializable>(
                    (Serializable) source, MediaType.getMostSpecific(
                            target.getMediaType(),
                            MediaType.APPLICATION_OCTET_STREAM));
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (Form.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_WWW_FORM, 1.0F);
        } else if (Serializable.class.isAssignableFrom(entity)) {
            if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED) {
                updatePreferences(preferences,
                        MediaType.APPLICATION_JAVA_OBJECT, 1.0F);
            }
            if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED) {
                updatePreferences(preferences,
                        MediaType.APPLICATION_JAVA_OBJECT_XML, 1.0F);
            }
        } else if (String.class.isAssignableFrom(entity)
                || Reader.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.TEXT_PLAIN, 1.0F);
            updatePreferences(preferences, MediaType.TEXT_ALL, 0.5F);
        } else if (InputStream.class.isAssignableFrom(entity)
                || ReadableByteChannel.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_OCTET_STREAM,
                    1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_ALL, 0.5F);
        }
    }

}
