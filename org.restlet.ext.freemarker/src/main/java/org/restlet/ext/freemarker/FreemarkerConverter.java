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

package org.restlet.ext.freemarker;

import java.io.IOException;
import java.util.List;

import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.ext.freemarker.internal.ResolverHashModel;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.util.Resolver;

import freemarker.template.Template;

/**
 * Converter between the FreeMarker Template objects and Representations. The
 * adjoined data model is based on the request and response objects.
 * 
 * @author Thierry Boileau.
 */
public class FreemarkerConverter extends ConverterHelper {

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        return null;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        return null;
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
            return new TemplateRepresentation((Template) source,
                    new ResolverHashModel(Resolver.createResolver(
                            resource.getRequest(), resource.getResponse())),
                    target.getMediaType());

        }

        return null;
    }

}
