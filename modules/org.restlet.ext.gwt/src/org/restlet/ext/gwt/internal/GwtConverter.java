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

package org.restlet.ext.gwt.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.ext.gwt.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between the XML/JSON and Representation classes based on XStream.
 * 
 * @author Jerome Louvel
 */
public class GwtConverter extends ConverterHelper {

    /** JSON variant. */
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

        if (Serializable.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_GWT);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if (source instanceof Serializable) {
            if (MediaType.APPLICATION_JAVA_OBJECT_GWT.isCompatible(target
                    .getMediaType())) {
                result = 0.7F;
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

        if (Serializable.class.isAssignableFrom(target)) {
            if (MediaType.APPLICATION_JAVA_OBJECT_GWT.isCompatible(source
                    .getMediaType())) {
                result = 0.7F;
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

        if (Serializable.class.isAssignableFrom(target)) {
            result = new ObjectRepresentation(source.getText(), target)
                    .getObject();
        }

        return (T) result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) {
        Representation result = null;

        if (source instanceof Serializable) {
            result = new ObjectRepresentation((Serializable) source);
        } else if (source instanceof Representation) {
            result = (Representation) source;
        }

        return result;
    }
}
