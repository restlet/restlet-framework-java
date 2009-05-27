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
    private static final Variant VARIANT_GWT = new Variant(
            MediaType.APPLICATION_JAVA_OBJECT_GWT);

    @Override
    public List<Class<?>> getObjectClasses(Variant variant) {
        List<Class<?>> result = null;

        if (variant != null) {
            if (VARIANT_GWT.isCompatible(variant)) {
                result = addObjectClass(result, Object.class);
            }
        }

        return result;
    }

    @Override
    public List<Variant> getVariants(Class<?> objectClass) {
        return addVariant(null, VARIANT_GWT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation representation, Class<T> targetClass,
            UniformResource resource) throws IOException {
        Object result = null;
        ObjectRepresentation<?> objectRepresentation;

        if (VARIANT_GWT.isCompatible(representation)) {
            objectRepresentation = new ObjectRepresentation(representation
                    .getText(), targetClass);
            result = objectRepresentation.getObject();
        }

        return (T) result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Representation toRepresentation(Object object,
            Variant targetVariant, UniformResource resource) {
        if (targetVariant == null) {
            targetVariant = new Variant(MediaType.APPLICATION_JAVA_OBJECT_GWT);
        }

        ObjectRepresentation objectRepresentation = new ObjectRepresentation(
                (Serializable) object);
        return objectRepresentation;
    }
}
