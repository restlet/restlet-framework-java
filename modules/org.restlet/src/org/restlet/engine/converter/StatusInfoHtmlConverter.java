/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.converter;

import org.restlet.data.MediaType;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StatusInfo;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import java.io.IOException;
import java.util.List;

/**
 * Converter for the {@link StatusInfo} class.
 * 
 * @author Manuel Boillod
 */
public class StatusInfoHtmlConverter extends ConverterHelper {

    /** Variant with media type application/xhtml+xml. */
    private static final VariantInfo VARIANT_APPLICATION_XHTML = new VariantInfo(
            MediaType.APPLICATION_XHTML);

    /** Variant with media type text/html. */
    private static final VariantInfo VARIANT_TEXT_HTML = new VariantInfo(
            MediaType.TEXT_HTML);


    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (isCompatible(source)) {
            result = addObjectClass(result, StatusInfo.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) throws IOException {
        List<VariantInfo> result = null;

        if (source != null && StatusInfo.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_TEXT_HTML);
            result = addVariant(result, VARIANT_APPLICATION_XHTML);
        }

        return result;
    }

    /**
     * Indicates if the given variant is compatible with the media types
     * supported by this converter.
     *
     * @param variant
     *            The variant.
     * @return True if the given variant is compatible with the media types
     *         supported by this converter.
     */
    protected boolean isCompatible(Variant variant) {
        return (variant != null)
                && (VARIANT_TEXT_HTML.isCompatible(variant)
                || VARIANT_APPLICATION_XHTML.isCompatible(variant));
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof StatusInfo && isCompatible(target)) {
            result = 1.0F;
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target, Resource resource) {
        return -1.0F;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target, Resource resource) throws IOException {
        return null;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target, Resource resource) throws IOException {
        return null;
    }

}
