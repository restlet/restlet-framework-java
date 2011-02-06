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

package org.restlet.ext.atom;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between the Atom API and Representation classes.
 * 
 * @author Jerome Louvel
 */
public class AtomConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_ATOM = new VariantInfo(
            MediaType.APPLICATION_ATOM);

    private static final VariantInfo VARIANT_ATOMPUB_SERVICE = new VariantInfo(
            MediaType.APPLICATION_ATOMPUB_SERVICE);

    // private static final Variant VARIANT_ATOMPUB_CATEGORY = new Variant(
    // MediaType.APPLICATION_ATOMPUB_CATEGORY);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_ATOM.isCompatible(source)) {
            result = addObjectClass(result, Feed.class);
        } else if (VARIANT_ATOMPUB_SERVICE.isCompatible(source)) {
            result = addObjectClass(result, Service.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (Feed.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_ATOM);
        } else if (Service.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_ATOMPUB_SERVICE);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if (source instanceof Feed) {
            if (target == null) {
                result = 0.5F;
            } else if (MediaType.APPLICATION_ATOM.isCompatible(target
                    .getMediaType())) {
                result = 1.0F;
            } else {
                result = 0.5F;
            }
        } else if (source instanceof Service) {
            if ((target != null)
                    && MediaType.APPLICATION_ATOMPUB_SERVICE
                            .isCompatible(target.getMediaType())) {
                result = 1.0F;
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

        if (target != null) {
            if (Feed.class.isAssignableFrom(target)) {
                if (MediaType.APPLICATION_ATOM.isCompatible(source
                        .getMediaType())) {
                    result = 1.0F;
                } else {
                    result = 0.5F;
                }
            } else if (Service.class.isAssignableFrom(target)) {
                if (MediaType.APPLICATION_ATOMPUB_SERVICE.isCompatible(source
                        .getMediaType())) {
                    result = 1.0F;
                } else {
                    result = 0.5F;
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        Object result = null;

        if (Feed.class.isAssignableFrom(target)) {
            result = new Feed(source);
        } else if (Service.class.isAssignableFrom(target)) {
            result = new Service(source);
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) {
        Representation result = null;

        if (source instanceof Feed) {
            result = (Feed) source;
        } else if (source instanceof Service) {
            result = (Service) source;
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (Feed.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_ATOM, 1.0F);
        } else if (Service.class.isAssignableFrom(entity)) {
            updatePreferences(preferences,
                    MediaType.APPLICATION_ATOMPUB_SERVICE, 1.0F);
        }
    }

}
