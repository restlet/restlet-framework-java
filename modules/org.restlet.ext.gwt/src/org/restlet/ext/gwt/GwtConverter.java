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
import org.restlet.resource.Resource;

import com.google.gwt.user.client.rpc.IsSerializable;

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
                || IsSerializable.class.isAssignableFrom(source)
                || ObjectRepresentation.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_GWT);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof Serializable || source instanceof IsSerializable) {
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
            Resource resource) {
        float result = -1.0F;

        if (source instanceof ObjectRepresentation<?>) {
            result = 1.0F;
        } else if ((target != null)
                && ObjectRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if ((target != null)
                && (Serializable.class.isAssignableFrom(target) || IsSerializable.class
                        .isAssignableFrom(target))) {
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
            Resource resource) throws IOException {
        T result = null;

        if (target != null) {
            if (ObjectRepresentation.class.isAssignableFrom(target)) {
                if (source instanceof ObjectRepresentation<?>) {
                    result = (T) source;
                } else {
                    result = (T) new ObjectRepresentation<T>(source.getText(),
                            target);
                }
            } else if (Serializable.class.isAssignableFrom(target)
                    || IsSerializable.class.isAssignableFrom(target)) {
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
            Resource resource) {
        Representation result = null;

        if (source instanceof Serializable) {
            result = new ObjectRepresentation<Serializable>(
                    (Serializable) source);
        } else if (source instanceof IsSerializable) {
            result = new ObjectRepresentation<IsSerializable>(
                    (IsSerializable) source);
        } else if (source instanceof Representation) {
            result = (Representation) source;
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (Serializable.class.isAssignableFrom(entity)
                || IsSerializable.class.isAssignableFrom(entity)
                || ObjectRepresentation.class.isAssignableFrom(entity)) {
            updatePreferences(preferences,
                    MediaType.APPLICATION_JAVA_OBJECT_GWT, 1.0F);
        }
    }

}
