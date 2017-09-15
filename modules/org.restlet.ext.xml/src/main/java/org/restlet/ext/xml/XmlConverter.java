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

package org.restlet.ext.xml;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
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
    public float score(Object source, Variant target, Resource resource) {
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
            Resource resource) {
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
            Resource resource) throws IOException {

        Object result = null;
        if (target != null) {
            if (Document.class.isAssignableFrom(target)) {
                if (source instanceof DomRepresentation) {
                    result = ((DomRepresentation) source).getDocument();
                } else {
                    result = new DomRepresentation(source).getDocument();
                }
            } else if (DomRepresentation.class.isAssignableFrom(target)) {
                if (source instanceof DomRepresentation) {
                    result = (DomRepresentation) source;
                } else {
                    result = new DomRepresentation(source);
                }
            } else if (SaxRepresentation.class.isAssignableFrom(target)) {
                if (source instanceof SaxRepresentation) {
                    result = (SaxRepresentation) source;
                } else {
                    result = new SaxRepresentation(source);
                }
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) throws IOException {
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
