/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.xml;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;
import org.w3c.dom.Document;

/**
 * Converter between the XML APIs and XML Representation classes.
 * 
 * @author Jerome Louvel
 */
public class XmlConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_APPLICATION_ALL_XML = new VariantInfo(
            MediaType.APPLICATION_ALL_XML);

    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(
            MediaType.APPLICATION_XML);

    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(
            MediaType.TEXT_XML);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_APPLICATION_ALL_XML.isCompatible(source)
                || VARIANT_APPLICATION_XML.isCompatible(source)
                || VARIANT_TEXT_XML.isCompatible(source)) {
            result = addObjectClass(result, Document.class);
            result = addObjectClass(result, DomRepresentation.class);
            result = addObjectClass(result, SaxRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (Document.class.isAssignableFrom(source)
                || DomRepresentation.class.isAssignableFrom(source)
                || SaxRepresentation.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_APPLICATION_ALL_XML);
            result = addVariant(result, VARIANT_APPLICATION_XML);
            result = addVariant(result, VARIANT_TEXT_XML);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if (source instanceof Document) {
            if (target == null) {
                result = 0.5F;
            } else if (MediaType.APPLICATION_ALL_XML.isCompatible(target
                    .getMediaType())) {
                result = 0.8F;
            } else if (MediaType.APPLICATION_XML.isCompatible(target
                    .getMediaType())) {
                result = 0.9F;
            } else if (MediaType.TEXT_XML.isCompatible(target.getMediaType())) {
                result = 0.9F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            UniformResource resource) {
        float result = -1.0F;

        if ((target != null)
                && (Document.class.isAssignableFrom(target)
                        || DomRepresentation.class.isAssignableFrom(target) || SaxRepresentation.class
                        .isAssignableFrom(target))) {
            if (MediaType.APPLICATION_ALL_XML.isCompatible(source
                    .getMediaType())) {
                result = 0.8F;
            } else if (MediaType.APPLICATION_XML.isCompatible(source
                    .getMediaType())) {
                result = 0.9F;
            } else if (MediaType.TEXT_XML.isCompatible(source.getMediaType())) {
                result = 0.9F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        Object result = null;

        if (Document.class.isAssignableFrom(target)) {
            result = new DomRepresentation(source).getDocument();
        } else if (DomRepresentation.class.isAssignableFrom(target)) {
            result = new DomRepresentation(source);
        } else if (SaxRepresentation.class.isAssignableFrom(target)) {
            result = new SaxRepresentation(source);
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) throws IOException {
        Representation result = null;

        if (source instanceof Document) {
            result = new DomRepresentation(target.getMediaType(),
                    (Document) source);
        } else if (source instanceof Representation) {
            result = (Representation) source;
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (Document.class.isAssignableFrom(entity)
                || DomRepresentation.class.isAssignableFrom(entity)
                || SaxRepresentation.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_ALL_XML, 0.8F);
            updatePreferences(preferences, MediaType.APPLICATION_XML, 0.9F);
            updatePreferences(preferences, MediaType.TEXT_XML, 0.9F);
        }
    }
}
