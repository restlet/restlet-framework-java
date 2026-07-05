/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.converter;

import static org.restlet.data.MediaType.APPLICATION_OCTET_STREAM;
import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
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
    private static final VariantInfo VARIANT_ALL = new VariantInfo(MediaType.ALL);

    /** Web form variant. */
    private static final VariantInfo VARIANT_FORM = new VariantInfo(MediaType.APPLICATION_WWW_FORM);

    /** Octet stream variant. */
    private static final VariantInfo VARIANT_OBJECT =
            new VariantInfo(MediaType.APPLICATION_JAVA_OBJECT);

    /** Octet stream variant. */
    private static final VariantInfo VARIANT_OBJECT_XML =
            new VariantInfo(MediaType.APPLICATION_JAVA_OBJECT_XML);

    /** List of types able to generate all kinds of variants. */
    private static final List<Class<?>> VARIANT_ALL_TYPES =
            List.of(
                    String.class,
                    StringRepresentation.class,
                    File.class,
                    FileRepresentation.class,
                    InputStream.class,
                    InputRepresentation.class,
                    Reader.class,
                    ReaderRepresentation.class,
                    Representation.class);

    private static final List<Class<?>> SCORE_ONE_TARGET_TYPES =
            List.of(
                    String.class,
                    StringRepresentation.class,
                    EmptyRepresentation.class,
                    InputStream.class,
                    InputRepresentation.class,
                    Reader.class,
                    ReaderRepresentation.class);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;
        result = addObjectClass(result, String.class);
        result = addObjectClass(result, InputStream.class);
        result = addObjectClass(result, Reader.class);

        if (source.getMediaType() != null) {
            MediaType mediaType = source.getMediaType();

            if ((ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                            && MediaType.APPLICATION_JAVA_OBJECT.equals(mediaType))
                    || (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                            && MediaType.APPLICATION_JAVA_OBJECT_XML.equals(mediaType))) {
                result = addObjectClass(result, Object.class);
            } else if (MediaType.APPLICATION_WWW_FORM.equals(mediaType)) {
                result = addObjectClass(result, Form.class);
            }
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        if (source == null) {
            return List.of();
        }

        List<VariantInfo> result = null;

        if (VARIANT_ALL_TYPES.stream().anyMatch(t -> t.isAssignableFrom(source))) {
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

        if (result == null) {
            result = List.of();
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        return switch (source) {
            case String s -> 1.0F;
            case File f -> 1.0F;
            case InputStream is -> 1.0F;
            case Reader r -> 1.0F;
            case Representation rep -> 1.0F;
            case Form form
                    when target != null
                            && MediaType.APPLICATION_WWW_FORM.isCompatible(target.getMediaType()) ->
                    1.0F;
            case Form form -> 0.6F;
            case Serializable s -> scoreSerializable(target);
            case null, default -> -1.0F;
        };
    }

    private float scoreSerializable(Variant target) {
        if (target == null) {
            return ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED ? 0.5F : -1.0F;
        }
        MediaType mt = target.getMediaType();
        if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                && MediaType.APPLICATION_JAVA_OBJECT.equals(mt)) return 1.0F;
        if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                && MediaType.APPLICATION_JAVA_OBJECT.isCompatible(mt)) return 0.6F;
        if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                && MediaType.APPLICATION_JAVA_OBJECT_XML.equals(mt)) return 1.0F;
        if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                && MediaType.APPLICATION_JAVA_OBJECT_XML.isCompatible(mt)) return 0.6F;
        return -1.0F;
    }

    @Override
    public <T> float score(Representation source, Class<T> target, Resource resource) {
        if (target == null) {
            return source instanceof ObjectRepresentation<?> ? 1.0F : -1.0F;
        }
        if (target.isAssignableFrom(source.getClass())
                || SCORE_ONE_TARGET_TYPES.stream().anyMatch(t -> t.isAssignableFrom(target))) {
            return 1.0F;
        }
        if (File.class.isAssignableFrom(target)) {
            return source instanceof FileRepresentation ? 1.0F : -1.0F;
        }
        if (Form.class.isAssignableFrom(target)) {
            return MediaType.APPLICATION_WWW_FORM.isCompatible(source.getMediaType()) ? 1.0F : 0.5F;
        }
        if (Serializable.class.isAssignableFrom(target) || target.isPrimitive()) {
            return scoreSerializableRepresentation(source.getMediaType());
        }
        return -1.0F;
    }

    private float scoreSerializableRepresentation(MediaType mt) {
        if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                && MediaType.APPLICATION_JAVA_OBJECT.equals(mt)) return 1.0F;
        if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED
                && MediaType.APPLICATION_JAVA_OBJECT.isCompatible(mt)) return 0.6F;
        if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                && MediaType.APPLICATION_JAVA_OBJECT_XML.equals(mt)) return 1.0F;
        if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED
                && MediaType.APPLICATION_JAVA_OBJECT_XML.isCompatible(mt)) return 0.6F;
        return 0.5F;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target, Resource resource)
            throws IOException {
        if (target == null) {
            return (source instanceof ObjectRepresentation<?> objectRepresentation)
                    ? (T) objectRepresentation.getObject()
                    : null;
        }

        if (target.isAssignableFrom(source.getClass())) {
            return (T) source;
        } else if (String.class.isAssignableFrom(target)) {
            return (T) source.getText();
        } else if (StringRepresentation.class.isAssignableFrom(target)) {
            return (T) new StringRepresentation(source.getText(), source.getMediaType());
        } else if (File.class.isAssignableFrom(target)) {
            return toFile(source);
        } else if (Form.class.isAssignableFrom(target)) {
            return (T) new Form(source);
        } else if (InputStream.class.isAssignableFrom(target)) {
            return (T) source.getStream();
        } else if (InputRepresentation.class.isAssignableFrom(target)) {
            return (T) new InputRepresentation(source.getStream());
        } else if (Reader.class.isAssignableFrom(target)) {
            return (T) source.getReader();
        } else if (ReaderRepresentation.class.isAssignableFrom(target)) {
            return (T) new ReaderRepresentation(source.getReader());
        } else if (Serializable.class.isAssignableFrom(target)) {
            return (T) toObjectFromSerialized(source);
        } else if (target.isPrimitive()) {
            return (T) toObjectFromSerialized(source);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T toFile(final Representation source) {
        return (source instanceof FileRepresentation fileRepresentation)
                ? (T) fileRepresentation.getFile()
                : null;
    }

    private Object toObjectFromSerialized(Representation source) throws IOException {
        if (source instanceof ObjectRepresentation<?> o) return o.getObject();
        try {
            return new ObjectRepresentation<>(source).getObject();
        } catch (Exception e) {
            throw new IOException("Unable to create the Object representation", e);
        }
    }

    @Override
    public Representation toRepresentation(Object source, Variant target, Resource resource)
            throws IOException {
        final Representation result;

        switch (source) {
            case String string -> {
                final MediaType mediaType =
                        MediaType.getMostSpecific(target.getMediaType(), TEXT_PLAIN);
                result = new StringRepresentation(string, mediaType);
            }
            case File file -> {
                final MediaType mediaType =
                        MediaType.getMostSpecific(target.getMediaType(), APPLICATION_OCTET_STREAM);
                result = new FileRepresentation(file, mediaType);
            }
            case Form form -> result = form.getWebRepresentation();
            case InputStream inputStream -> {
                final MediaType mediaType =
                        MediaType.getMostSpecific(target.getMediaType(), APPLICATION_OCTET_STREAM);
                result = new InputRepresentation(inputStream, mediaType);
            }
            case Reader reader -> {
                final MediaType mediaType =
                        MediaType.getMostSpecific(target.getMediaType(), TEXT_PLAIN);
                result = new ReaderRepresentation(reader, mediaType);
            }
            case Representation representation -> result = representation;
            case Serializable serializable -> {
                final MediaType mediaType =
                        MediaType.getMostSpecific(target.getMediaType(), APPLICATION_OCTET_STREAM);
                result = new ObjectRepresentation<>(serializable, mediaType);
            }
            case null, default -> result = null;
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences, Class<T> entity) {
        if (Form.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_WWW_FORM, 1.0F);
        } else if (String.class.isAssignableFrom(entity) || Reader.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, TEXT_PLAIN, 1.0F);
            updatePreferences(preferences, MediaType.TEXT_ALL, 0.5F);
        } else if (Serializable.class.isAssignableFrom(entity)) {
            if (ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED) {
                updatePreferences(preferences, MediaType.APPLICATION_JAVA_OBJECT, 1.0F);
            }
            if (ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED) {
                updatePreferences(preferences, MediaType.APPLICATION_JAVA_OBJECT_XML, 1.0F);
            }
        } else if (InputStream.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, APPLICATION_OCTET_STREAM, 1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_ALL, 0.5F);
        }
    }
}
