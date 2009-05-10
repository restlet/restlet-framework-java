/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.xml.internal;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.ext.xml.XmlRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;
import org.w3c.dom.Document;

/**
 * Converter between the XML APIs and XML Representation classes.
 * 
 * @author Jerome Louvel
 */
public class XmlConverter extends ConverterHelper {

    /** XML application variant. */
    private static final Variant VARIANT_XML_APP = new Variant(
            MediaType.APPLICATION_XML);

    /** XML text variant. */
    private static final Variant VARIANT_XML_TEXT = new Variant(
            MediaType.TEXT_XML);

    @Override
    public List<Class<?>> getObjectClasses(Variant variant) {
        List<Class<?>> result = null;

        if (variant.getMediaType() != null) {
            MediaType mediaType = variant.getMediaType();

            if (MediaType.APPLICATION_ALL_XML.equals(mediaType)
                    || MediaType.TEXT_XML.equals(mediaType)
                    || MediaType.APPLICATION_ATOMPUB_SERVICE.equals(mediaType)
                    || MediaType.APPLICATION_ATOM.equals(mediaType)
                    || MediaType.APPLICATION_RDF_XML.equals(mediaType)
                    || MediaType.APPLICATION_WADL.equals(mediaType)
                    || MediaType.APPLICATION_XHTML.equals(mediaType)) {
                result = addObjectClass(result, Document.class);
                result = addObjectClass(result, XmlRepresentation.class);
            }
        }

        return result;
    }

    @Override
    public List<Variant> getVariants(Class<?> objectClass, Variant targetVariant) {
        List<Variant> result = null;

        if (Document.class.isAssignableFrom(objectClass)
                || DomRepresentation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, targetVariant, VARIANT_XML_APP);
            result = addVariant(result, targetVariant, VARIANT_XML_TEXT);
        } else if (SaxRepresentation.class.isAssignableFrom(objectClass)) {
            result = addVariant(result, targetVariant, VARIANT_XML_APP);
            result = addVariant(result, targetVariant, VARIANT_XML_TEXT);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation representation, Class<T> targetClass,
            UniformResource resource) throws IOException {
        Object result = null;

        if (representation != null) {
            if (targetClass != null) {
                if (targetClass.isAssignableFrom(representation.getClass())) {
                    result = (T) representation;
                } else {
                    if (Document.class.isAssignableFrom(targetClass)) {
                        result = new DomRepresentation(representation)
                                .getDocument();
                    } else if (DomRepresentation.class
                            .isAssignableFrom(targetClass)) {
                        result = new DomRepresentation(representation);
                    } else if (SaxRepresentation.class
                            .isAssignableFrom(targetClass)) {
                        result = new SaxRepresentation(representation);
                    }
                }
            }

            if (result instanceof Representation) {
                Representation resultRepresentation = (Representation) result;

                // Copy the variant metadata
                resultRepresentation.setCharacterSet(representation
                        .getCharacterSet());
                resultRepresentation
                        .setMediaType(representation.getMediaType());
                resultRepresentation
                        .setEncodings(representation.getEncodings());
                resultRepresentation
                        .setLanguages(representation.getLanguages());
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object object,
            Variant targetVariant, UniformResource resource) throws IOException {
        Representation result = null;

        if (object instanceof Document) {
            result = new DomRepresentation(targetVariant == null ? null
                    : targetVariant.getMediaType(), (Document) object);
        }

        if ((result != null) && (targetVariant != null)) {
            // Copy the variant metadata
            result.setCharacterSet(targetVariant.getCharacterSet());
            result.setMediaType(targetVariant.getMediaType());
            result.setEncodings(targetVariant.getEncodings());
            result.setLanguages(targetVariant.getLanguages());
        }

        return result;
    }
}
