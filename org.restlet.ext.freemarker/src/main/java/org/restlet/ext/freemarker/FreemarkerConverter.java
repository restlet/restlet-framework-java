/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.freemarker;

import freemarker.template.Template;
import java.util.List;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.ext.freemarker.internal.ResolverHashModel;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.util.Resolver;

/**
 * Converter between the FreeMarker Template objects and Representations. The adjoined data model is
 * based on the request and response objects.
 *
 * @author Thierry Boileau.
 */
public class FreemarkerConverter extends ConverterHelper {

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        return List.of();
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
    public <T> float score(Representation source, Class<T> target, Resource resource) {
        return -1.0f;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target, Resource resource) {
        return null;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target, Resource resource) {

        if (source instanceof Template template) {
            return new TemplateRepresentation(
                    template,
                    new ResolverHashModel(
                            Resolver.createResolver(resource.getRequest(), resource.getResponse())),
                    target.getMediaType());
        }

        return null;
    }
}
