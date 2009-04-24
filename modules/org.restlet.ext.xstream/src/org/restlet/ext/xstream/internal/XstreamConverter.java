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

package org.restlet.ext.xstream.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between the XML/JSON and Representation classes based on XStream.
 * 
 * @author Jerome Louvel
 */
public class XstreamConverter extends ConverterHelper {

    private static final Variant VARIANT_JSON = new Variant(
            MediaType.APPLICATION_JSON);

    private static final Variant VARIANT_XML = new Variant(
            MediaType.APPLICATION_XML);

    @Override
    public List<Class<?>> getObjectClasses(Variant variant) {
        List<Class<?>> result = null;

        if (variant != null) {
            if (VARIANT_JSON.isCompatible(variant)
                    || VARIANT_XML.isCompatible(variant)) {
                if (result == null) {
                    result = new ArrayList<Class<?>>();
                }

                result.add(Object.class);
            }
        }

        return result;
    }

    @Override
    public List<Variant> getVariants(Class<?> objectClass, Variant targetVariant) {
        List<Variant> result = new ArrayList<Variant>();
        result.add(VARIANT_JSON);
        result.add(VARIANT_XML);
        return result;
    }

    @Override
    public <T> T toObject(Representation representation, Class<T> targetClass,
            UniformResource resource) throws IOException {
        T result = null;
        XstreamRepresentation<T> xstreamRepresentation;

        if (VARIANT_JSON.isCompatible(representation)) {
            xstreamRepresentation = new XstreamRepresentation<T>(representation);
            result = xstreamRepresentation.getObject();
        } else if (VARIANT_XML.isCompatible(representation)) {
            xstreamRepresentation = new XstreamRepresentation<T>(representation);
            result = xstreamRepresentation.getObject();
        }

        return result;
    }

    @Override
    public Representation toRepresentation(Object object,
            Variant targetVariant, UniformResource resource) {
        return new XstreamRepresentation<Object>(targetVariant.getMediaType(),
                object);
    }
}
