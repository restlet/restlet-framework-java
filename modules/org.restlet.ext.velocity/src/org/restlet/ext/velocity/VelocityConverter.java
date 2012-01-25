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

package org.restlet.ext.velocity;

import java.io.IOException;
import java.util.List;

import org.apache.velocity.Template;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Converter between the Velocity Template objects and Representations. The
 * adjoined data model is based on the request and response objects.
 * 
 * @author Thierry Boileau.
 */
public class VelocityConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_ALL = new VariantInfo(
            MediaType.ALL);

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
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        return -1.0f;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        if (source instanceof Template) {
            return 1.0f;
        }

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
            TemplateRepresentation tr = new TemplateRepresentation(
                    (Template) source, target.getMediaType());
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
