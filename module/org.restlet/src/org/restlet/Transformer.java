/*
 * Copyright 2005-2006 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;

/**
 * Filter that can transform XML representations by applying an XSLT transform
 * sheet.
 * 
 * @author Jerome Louvel (contact@noelios.com) <a
 *         href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Transformer extends Filter {
    /**
     * Result representation able to apply an XSLT transformation.
     * 
     * @author Jerome Louvel (contact@noelios.com) <a
     *         href="http://www.noelios.com/">Noelios Consulting</a>
     */
    private final static class ResultRepresentation extends
            OutputRepresentation {
        /** The parent transformer. */
        private Transformer transformer;

        /** The source representation to transform. */
        private Representation sourceRepresentation;

        /** The URI resolver. */
        private URIResolver uriResolver;

        /**
         * Constructor.
         * 
         * @param transformer
         *            The parent transformer.
         * @param sourceRepresentation
         *            The source representation to transform.
         * @param uriResolver
         *            The URI resolver.
         */
        public ResultRepresentation(Transformer transformer,
                Representation sourceRepresentation, URIResolver uriResolver) {
            super(null);
            this.transformer = transformer;
            this.sourceRepresentation = sourceRepresentation;
            this.uriResolver = uriResolver;

            if (transformer != null) {
                setCharacterSet(transformer.getResultCharacterSet());
                setLanguage(transformer.getResultLanguage());
                setMediaType(transformer.getResultMediaType());
            }
        }

        /**
         * Returns the source representation to transform.
         * 
         * @return The source representation to transform.
         */
        public Representation getSourceRepresentation() {
            return this.sourceRepresentation;
        }

        /**
         * Returns the parent transformer.
         * 
         * @return The parent transformer.
         */
        public Transformer getTransformer() {
            return this.transformer;
        }

        /**
         * Returns the URI resolver.
         * 
         * @return The URI resolver.
         */
        public URIResolver getURIResolver() {
            return this.uriResolver;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            // Prepare the XSLT transformer documents
            StreamSource sourceDocument = new StreamSource(
                    getSourceRepresentation().getStream());
            StreamSource transformSheet = new StreamSource(getTransformer()
                    .getTransformSheet().getStream());
            StreamResult resultDocument = new StreamResult(outputStream);

            try {
                // Create a new transformer as they are not thread safe
                javax.xml.transform.Transformer transformer = TransformerFactory
                        .newInstance().newTransformer(transformSheet);

                // Set the URI resolver
                transformer.setURIResolver(getURIResolver());

                // Generates the result of the transformation
                transformer.transform(sourceDocument, resultDocument);
            } catch (TransformerConfigurationException tce) {
                getTransformer().getContext().getLogger().log(Level.WARNING,
                        "Transformer configuration exception", tce);
            } catch (TransformerFactoryConfigurationError tfce) {
                getTransformer().getContext().getLogger().log(Level.WARNING,
                        "Transformer factory configuration exception", tfce);
            } catch (TransformerException te) {
                getTransformer().getContext().getLogger().log(Level.WARNING,
                        "Transformer exception", te);
            }
        }
    }

    /**
     * URI resolver based on a Restlet Context instance.
     * 
     * @author Jerome Louvel (contact@noelios.com)
     */
    private final static class ContextResolver implements URIResolver {
        /** The Restlet context. */
        private Context context;

        /**
         * Constructor.
         * 
         * @param context
         *            The Restlet context.
         */
        public ContextResolver(Context context) {
            this.context = context;
        }

        /**
         * Resolves a target reference into a Source document.
         * 
         * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
         *      java.lang.String)
         */
        public Source resolve(String href, String base)
                throws TransformerException {
            Source result = null;

            if (this.context != null) {
                Reference targetRef = null;

                if (base != null) {
                    // Potentially a relative reference
                    Reference baseRef = new Reference(base);
                    targetRef = new Reference(baseRef, href);
                } else {
                    // No base, assume "href" is an absolute URI
                    targetRef = new Reference(href);
                }

                Response response = this.context.getDispatcher().get(
                        targetRef.toString());
                if (response.getStatus().isSuccess()
                        && response.isEntityAvailable()) {
                    try {
                        result = new StreamSource(response.getEntity()
                                .getStream());
                    } catch (IOException e) {
                        this.context.getLogger().log(Level.WARNING,
                                "I/O error while getting the response stream",
                                e);
                    }
                }
            }

            return result;
        }
    }

    /**
     * Mode where the developer manually applies transformations by calling the
     * transform() method.
     */
    public static final int MODE_MANUAL = 0;

    /**
     * Mode that transforms request entities before their handling by the
     * attached Restlet.
     */
    public static final int MODE_REQUEST = 1;

    /**
     * Mode that transforms response entities after their handling by the
     * attached Restlet.
     */
    public static final int MODE_RESPONSE = 2;

    /** The transformation mode. */
    private int mode;

    /** The XSLT transform sheet to apply to message entities. */
    private Representation transformSheet;

    /**
     * The media type of the result representation. MediaType.APPLICATION_XML by
     * default.
     */
    private MediaType resultMediaType;

    /** The language of the result representation. The default value is null. */
    private Language resultLanguage;

    /**
     * The character set of the result representation. The default value is
     * null.
     */
    private CharacterSet resultCharacterSet;

    /**
     * Constructor.
     * 
     * @param transformSheet
     *            The XSLT transform sheet to apply to message entities.
     */
    public Transformer(Representation transformSheet) {
        this(MODE_MANUAL, transformSheet);
    }

    /**
     * Constructor.
     * 
     * @param mode
     *            The transformation mode.
     * @param transformSheet
     *            The XSLT transform sheet to apply to message entities.
     */
    public Transformer(int mode, Representation transformSheet) {
        this.mode = mode;
        this.transformSheet = transformSheet;
        this.resultMediaType = MediaType.APPLICATION_XML;
        this.resultLanguage = null;
        this.resultCharacterSet = null;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (getMode() == MODE_RESPONSE) {
            response.setEntity(transform(response.getEntity()));
        }
    }

    @Override
    protected void beforeHandle(Request request, Response response) {
        if (getMode() == MODE_REQUEST) {
            request.setEntity(transform(request.getEntity()));
        }
    }

    /**
     * Returns the transformation mode. See MODE_* constants.
     * 
     * @return The transformation mode.
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * Returns the character set of the result representation. The default value
     * is null.
     * 
     * @return The character set of the result representation.
     */
    public CharacterSet getResultCharacterSet() {
        return this.resultCharacterSet;
    }

    /**
     * Returns the language of the result representation. The default value is
     * null.
     * 
     * @return The language of the result representation.
     */
    public Language getResultLanguage() {
        return this.resultLanguage;
    }

    /**
     * Returns the media type of the result representation. The default value is
     * MediaType.APPLICATION_XML.
     * 
     * @return The media type of the result representation.
     */
    public MediaType getResultMediaType() {
        return this.resultMediaType;
    }

    /**
     * Returns the XSLT transform sheet to apply to message entities.
     * 
     * @return The XSLT transform sheet to apply to message entities.
     */
    public Representation getTransformSheet() {
        return this.transformSheet;
    }

    /**
     * Sets the transformation mode. See MODE_* constants.
     * 
     * @param mode
     *            The transformation mode.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Sets the character set of the result representation.
     * 
     * @param resultCharacterSet
     *            The character set of the result representation.
     */
    public void setResultCharacterSet(CharacterSet resultCharacterSet) {
        this.resultCharacterSet = resultCharacterSet;
    }

    /**
     * Sets the language of the result representation.
     * 
     * @param resultLanguage
     *            The language of the result representation.
     */
    public void setResultLanguage(Language resultLanguage) {
        this.resultLanguage = resultLanguage;
    }

    /**
     * Sets the media type of the result representation.
     * 
     * @param resultMediaType
     *            The media type of the result representation.
     */
    public void setResultMediaType(MediaType resultMediaType) {
        this.resultMediaType = resultMediaType;
    }

    /**
     * Sets the XSLT transform sheet to apply to message entities.
     * 
     * @param transformSheet
     *            The XSLT transform sheet to apply to message entities.
     */
    public void setTransformSheet(Representation transformSheet) {
        this.transformSheet = transformSheet;
    }

    /**
     * Transforms a source XML representation by applying an XSLT transform
     * sheet to it.
     * 
     * @param source
     *            The source XML representation.
     * @return The generated result representation.
     */
    public Representation transform(Representation source) {
        return new ResultRepresentation(this, source, new ContextResolver(
                getContext()));
    }

}
