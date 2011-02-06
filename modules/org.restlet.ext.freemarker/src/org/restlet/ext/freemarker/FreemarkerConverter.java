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

package org.restlet.ext.freemarker;

import java.io.IOException;
import java.util.List;

import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.ext.freemarker.internal.ResolverHashModel;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;
import org.restlet.util.Resolver;

import freemarker.template.Template;

/**
 * Converter between the Freemarker Template objects and Representations. The
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
    public <T> float score(Representation source, Class<T> target,
            UniformResource resource) {
        return -1.0f;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        if (source instanceof Template) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        return null;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) throws IOException {

        if (source instanceof Template) {
            return new TemplateRepresentation((Template) source,
                    new ResolverHashModel(Resolver.createResolver(
                            resource.getRequest(), resource.getResponse())),
                    target.getMediaType());

        }

        return null;
    }

}
