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

package org.restlet.ext.wadl;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

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
            Resource resource) {
        float result = -1.0F;

        if ((source != null)
                && (ApplicationInfo.class.isAssignableFrom(target))) {
            result = 1.0F;
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        if (source instanceof ApplicationInfo) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
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
            Resource resource) throws IOException {
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
