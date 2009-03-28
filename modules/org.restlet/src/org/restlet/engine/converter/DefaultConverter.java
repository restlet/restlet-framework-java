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

package org.restlet.engine.converter;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.representation.DomRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ReaderRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;
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
        MediaType mediaType = variant.getMediaType();

        if (mediaType != null) {

        }

        return result;
    }

    @Override
    public List<Variant> getVariants(Class<?> objectClass) {
        List<Variant> result = null;

        if (String.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(MediaType.TEXT_ALL));
        } else if (Document.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(
                    MediaType.APPLICATION_ALL_XML));
            result = addVariant(result, new Variant(MediaType.TEXT_XML));
        } else if (File.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(
                    MediaType.APPLICATION_OCTET_STREAM));
        } else if (InputStream.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(
                    MediaType.APPLICATION_OCTET_STREAM));
        } else if (Reader.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, new Variant(MediaType.TEXT_ALL));
        }

        return result;
    }

    private List<Variant> addVariant(List<Variant> variants, Variant variant) {
        if (variants == null) {
            variants = new ArrayList<Variant>();
        }

        variants.add(variant);
        return variants;
    }

    @Override
    public <T> T toObject(Representation representation, Class<T> targetClass,
            UniformResource resource) {

        return null;
    }

    @Override
    public Representation toRepresentation(Object object,
            Variant targetVariant, UniformResource resource) {
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
