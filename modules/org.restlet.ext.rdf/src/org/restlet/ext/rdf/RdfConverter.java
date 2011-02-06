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

package org.restlet.ext.rdf;

import java.io.IOException;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between the Graph and RDF Representation classes.
 * 
 * @author Thierry Boileau
 */
public class RdfConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_RDF_N3 = new VariantInfo(
            MediaType.TEXT_RDF_N3);

    private static final VariantInfo VARIANT_RDF_NTRIPLES = new VariantInfo(
            MediaType.TEXT_RDF_NTRIPLES);

    private static final VariantInfo VARIANT_RDF_TURTLE = new VariantInfo(
            MediaType.APPLICATION_RDF_TURTLE);

    private static final VariantInfo VARIANT_RDF_XML = new VariantInfo(
            MediaType.APPLICATION_ALL_XML);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_RDF_N3.isCompatible(source)
                || VARIANT_RDF_NTRIPLES.isCompatible(source)
                || VARIANT_RDF_TURTLE.isCompatible(source)
                || VARIANT_RDF_XML.isCompatible(source)) {
            result = addObjectClass(result, Graph.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null && Graph.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_RDF_N3);
            result = addVariant(result, VARIANT_RDF_NTRIPLES);
            result = addVariant(result, VARIANT_RDF_NTRIPLES);
            result = addVariant(result, VARIANT_RDF_XML);
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            UniformResource resource) {
        float result = -1.0f;

        if (target != null) {
            if (Graph.class.isAssignableFrom(target)) {
                result = 1.0f;
            }
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if (source instanceof Graph) {
            if (target == null) {
                result = 0.5F;
            } else if (VARIANT_RDF_N3.isCompatible(target)
                    || VARIANT_RDF_NTRIPLES.isCompatible(target)
                    || VARIANT_RDF_TURTLE.isCompatible(target)
                    || VARIANT_RDF_XML.isCompatible(target)) {
                result = 1.0F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        Object result = null;

        try {
            if (source instanceof RdfRepresentation) {
                result = ((RdfRepresentation) source).getGraph();
            } else {
                result = (new RdfRepresentation(source)).getGraph();
            }
        } catch (Exception e) {
            Context.getCurrentLogger()
                    .warning(
                            "Cannot convert a source representation into a Graph object.");
        }

        return target.cast(result);
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) throws IOException {
        if (source instanceof Graph) {
            return new RdfRepresentation((Graph) source, target.getMediaType());
        }

        return null;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (Graph.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.TEXT_RDF_N3, 1.0F);
            updatePreferences(preferences, MediaType.TEXT_RDF_NTRIPLES, 1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_RDF_TURTLE,
                    1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_RDF_XML, 1.0F);
        }
    }

}
