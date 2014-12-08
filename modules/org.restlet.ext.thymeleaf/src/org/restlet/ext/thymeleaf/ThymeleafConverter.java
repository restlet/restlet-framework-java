/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
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

package org.restlet.ext.thymeleaf;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.thymeleaf.Template;

/**
 * Converter between the Thymeleaf Template objects and Representations. The
 * adjoined data model is based on the request and response objects.
 * 
 * @author Grzegorz Godlewski
 */
public class ThymeleafConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_ALL = new VariantInfo(
            MediaType.ALL);

    private Locale getLocale(Resource resource) {
        return Locale.getDefault();
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        return null;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (Template.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_ALL);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        if (source instanceof Template) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        return -1.0f;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        return null;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) throws IOException {

        if (source instanceof Template) {
            Locale locale = getLocale(resource);

            TemplateRepresentation tr = new TemplateRepresentation(
                    ((Template) source).getTemplateName(), locale,
                    target.getMediaType());
            tr.setDataModel(resource.getRequest(), resource.getResponse());
            return tr;
        }

        return null;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (Template.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.ALL, 1.0F);
        }
    }
}
