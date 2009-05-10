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

package org.restlet.ext.atom.internal;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Service;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between the Atom API and Representation classes.
 * 
 * @author Jerome Louvel
 */
public class AtomConverter extends ConverterHelper {

    private static final Variant VARIANT_ATOM = new Variant(
            MediaType.APPLICATION_ATOM);

    private static final Variant VARIANT_ATOMPUB_SERVICE = new Variant(
            MediaType.APPLICATION_ATOMPUB_SERVICE);

    // private static final Variant VARIANT_ATOMPUB_CATEGORY = new Variant(
    // MediaType.APPLICATION_ATOMPUB_CATEGORY);

    @Override
    public List<Class<?>> getObjectClasses(Variant variant) {
        List<Class<?>> result = null;

        if (variant != null) {
            if (VARIANT_ATOM.isCompatible(variant)) {
                result = addObjectClass(result, Feed.class);
            } else if (VARIANT_ATOMPUB_SERVICE.isCompatible(variant)) {
                result = addObjectClass(result, Service.class);
            }
        }

        return result;
    }

    @Override
    public List<Variant> getVariants(Class<?> objectClass, Variant targetVariant) {
        List<Variant> result = null;

        if (Feed.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, targetVariant, VARIANT_ATOM);
        } else if (Service.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, targetVariant, VARIANT_ATOMPUB_SERVICE);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation representation, Class<T> targetClass,
            UniformResource resource) throws IOException {
        T result = null;

        if (representation != null) {
            if (VARIANT_ATOM.isCompatible(representation)) {
                result = (T) new Feed(representation);
            }

            // if (VARIANT_ATOMPUB_SERVICE.isCompatible(representation)) {
            // result = (T) new Service(representation);
            // }
        }

        return result;
    }

    @Override
    public Representation toRepresentation(Object object,
            Variant targetVariant, UniformResource resource) {
        Representation result = null;

        if ((object instanceof Feed)
                && ((targetVariant == null) || targetVariant
                        .isCompatible(VARIANT_ATOM))) {
            Feed feed = (Feed) object;
            result = feed;
        }

        return result;
    }
}
