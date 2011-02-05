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

package org.restlet.ext.wadl;

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
 * A converter helper to convert between {@link ApplicationInfo} objects and
 * {@link WadlRepresentation} ones.
 * 
 * @author Thierry Boileau
 */
public class WadlConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_APPLICATION_WADL = new VariantInfo(
            MediaType.APPLICATION_WADL);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_APPLICATION_WADL.includes(source)) {
            result = addObjectClass(result, ApplicationInfo.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (ApplicationInfo.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_APPLICATION_WADL);
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            UniformResource resource) {
        float result = -1.0F;

        if ((source != null)
                && (ApplicationInfo.class.isAssignableFrom(target))) {
            result = 1.0F;
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        if (source instanceof ApplicationInfo) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        Object result = null;

        if (ApplicationInfo.class.isAssignableFrom(target)) {
            if (source instanceof WadlRepresentation) {
                result = ((WadlRepresentation) source).getApplication();
            } else {
                result = new WadlRepresentation(source).getApplication();
            }
        }

        return target.cast(result);
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) throws IOException {
        if (source instanceof ApplicationInfo) {
            return new WadlRepresentation((ApplicationInfo) source);
        }

        return null;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (ApplicationInfo.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_WADL, 1.0F);
        }
    }
}
