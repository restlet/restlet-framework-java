/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.emf;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Converter between the XML/XMI/ECore and representation classes based on EMF.
 * 
 * @author Jerome Louvel
 */
public class EmfConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_APPLICATION_ALL_XML = new VariantInfo(
            MediaType.APPLICATION_ALL_XML);

    private static final VariantInfo VARIANT_APPLICATION_ECORE = new VariantInfo(
            MediaType.APPLICATION_ECORE);

    private static final VariantInfo VARIANT_APPLICATION_XMI = new VariantInfo(
            MediaType.APPLICATION_XMI);

    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(
            MediaType.APPLICATION_XML);

    private static final VariantInfo VARIANT_TEXT_HTML = new VariantInfo(
            MediaType.TEXT_HTML);

    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(
            MediaType.TEXT_XML);

    /**
     * Creates the marshaling {@link EmfRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link EmfRepresentation}.
     */
    protected <T extends EObject> EmfRepresentation<T> create(
            MediaType mediaType, T source) {
        return new EmfRepresentation<T>(mediaType, source);
    }

    /**
     * Creates the unmarshaling {@link EmfRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @return The unmarshaling {@link EmfRepresentation}.
     */
    protected <T extends EObject> EmfRepresentation<T> create(
            Representation source) {
        return new EmfRepresentation<T>(source);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (isCompatible(source)) {
            result = addObjectClass(result, EObject.class);
            result = addObjectClass(result, EmfRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if ((source != null) && EObject.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_APPLICATION_ALL_XML);
            result = addVariant(result, VARIANT_APPLICATION_XML);
            result = addVariant(result, VARIANT_APPLICATION_XMI);
            result = addVariant(result, VARIANT_APPLICATION_ECORE);
            result = addVariant(result, VARIANT_TEXT_XML);
            result = addVariant(result, VARIANT_TEXT_HTML);
        }

        return result;
    }

    protected boolean isCompatible(Variant variant) {
        return (variant != null)
                && (VARIANT_APPLICATION_ALL_XML.isCompatible(variant)
                        || VARIANT_APPLICATION_XML.isCompatible(variant)
                        || VARIANT_APPLICATION_XMI.isCompatible(variant)
                        || VARIANT_APPLICATION_ECORE.isCompatible(variant)
                        || VARIANT_TEXT_HTML.isCompatible(variant) || VARIANT_TEXT_XML
                        .isCompatible(variant));
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof EmfRepresentation<?>) {
            result = 1.0F;
        } else {
            if (target == null) {
                result = 0.5F;
            } else if (isCompatible(target)) {
                result = 0.8F;
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

        if (target != null) {
            if (isCompatible(source)) {
                result = 0.8F;
            }
        } else {
            result = 0.5F;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        Object result = null;

        if (source instanceof EmfRepresentation) {
            result = ((EmfRepresentation<?>) source).getObject();
        } else if (isCompatible(source)) {
            result = create(source).getObject();
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) {
        Representation result = null;

        if (source instanceof EmfRepresentation) {
            result = (EmfRepresentation<?>) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.TEXT_XML);
            }

            if (isCompatible(target)) {
                result = create(target.getMediaType(), (EObject) source);
            }
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (EObject.class.isAssignableFrom(entity)
                || EmfRepresentation.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_ALL_XML, 1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_XML, 1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_XMI, 1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_ECORE, 1.0F);
            updatePreferences(preferences, MediaType.TEXT_HTML, 1.0F);
            updatePreferences(preferences, MediaType.TEXT_XML, 1.0F);
        }
    }
}
