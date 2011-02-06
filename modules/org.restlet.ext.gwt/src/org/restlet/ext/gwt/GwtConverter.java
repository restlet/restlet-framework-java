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

package org.restlet.ext.gwt;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between Object instances and Representations based on GWT
 * serialization format.
 * 
 * @author Jerome Louvel
 */
public class GwtConverter extends ConverterHelper {

    /** GWT variant. */
    private static final VariantInfo VARIANT_GWT = new VariantInfo(
            MediaType.APPLICATION_JAVA_OBJECT_GWT);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_GWT.isCompatible(source)) {
            result = addObjectClass(result, Serializable.class);
            result = addObjectClass(result, ObjectRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (Serializable.class.isAssignableFrom(source)
                || ObjectRepresentation.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_GWT);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if (source instanceof Serializable) {
            if (target == null) {
                result = 0.5F;
            } else if (MediaType.APPLICATION_JAVA_OBJECT_GWT.equals(target
                    .getMediaType())) {
                result = 1.0F;
            } else if (MediaType.APPLICATION_JAVA_OBJECT_GWT
                    .isCompatible(target.getMediaType())) {
                result = 0.6F;
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

        if (source instanceof ObjectRepresentation<?>) {
            result = 1.0F;
        } else if ((target != null)
                && ObjectRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if ((target != null)
                && Serializable.class.isAssignableFrom(target)) {
            if (MediaType.APPLICATION_JAVA_OBJECT_GWT.equals(source
                    .getMediaType())) {
                result = 1.0F;
            } else if (MediaType.APPLICATION_JAVA_OBJECT_GWT
                    .isCompatible(source.getMediaType())) {
                result = 0.6F;
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        T result = null;

        if (target != null) {
            if (ObjectRepresentation.class.isAssignableFrom(target)) {
                if (source instanceof ObjectRepresentation<?>) {
                    result = (T) source;
                } else {
                    result = (T) new ObjectRepresentation<T>(source.getText(),
                            target);
                }
            } else if (Serializable.class.isAssignableFrom(target)) {
                result = new ObjectRepresentation<T>(source.getText(), target)
                        .getObject();
            }
        } else if (source instanceof ObjectRepresentation<?>) {
            result = ((ObjectRepresentation<T>) source).getObject();
        }

        return result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) {
        Representation result = null;

        if (source instanceof Serializable) {
            result = new ObjectRepresentation<Serializable>(
                    (Serializable) source);
        } else if (source instanceof Representation) {
            result = (Representation) source;
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (Serializable.class.isAssignableFrom(entity)
                || ObjectRepresentation.class.isAssignableFrom(entity)) {
            updatePreferences(preferences,
                    MediaType.APPLICATION_JAVA_OBJECT_GWT, 1.0F);
        }
    }

}
