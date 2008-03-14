package org.restlet.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Response;

/**
 * Representation able to apply an XSLT transformation. The internal JAXP
 * transformer is created when the getTransformer() method is first called. So,
 * if you need to specify a custom URI resolver, you need to do it before
 * actually using the representation for a transformation.<br>
 * <br>
 * This representation should be viewed as a wrapper representation that applies
 * a transform sheet on a source representation when it is read or written out.
 * Therefore, it isn't intended to be reused on different sources. For this use
 * case, you should instead use the {@link org.restlet.Transformer} filter.
 * 
 * @author Jerome Louvel (contact@noelios.com) <a
 *         href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class TransformRepresentation extends OutputRepresentation {
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
         *                The Restlet context.
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

                if ((base != null) && !base.equals("")) {
                    // Potentially a relative reference
                    Reference baseRef = new Reference(base);
                    targetRef = new Reference(baseRef, href);
                } else {
                    // No base, assume "href" is an absolute URI
                    targetRef = new Reference(href);
                }

                final String targetUri = targetRef.getTargetRef().toString();
                Response response = this.context.getClientDispatcher().get(
                        targetUri);
                if (response.getStatus().isSuccess()
                        && response.isEntityAvailable()) {
                    try {
                        result = new StreamSource(response.getEntity()
                                .getStream());
                        result.setSystemId(targetUri);

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

    /** The source representation to transform. */
    private Representation sourceRepresentation;

    /** The transformer to be used and reused. */
    private Transformer transformer;

    /** The XSLT transform sheet to apply to message entities. */
    private Representation transformSheet;

    /** The URI resolver. */
    private URIResolver uriResolver;

    /**
     * Constructor.
     * 
     * @param context
     *                The parent context.
     * @param source
     *                The source representation to transform.
     * @param transformSheet
     *                The XSLT transform sheet to apply.
     */
    public TransformRepresentation(Context context, Representation source,
            Representation transformSheet) {
        super(null);
        this.sourceRepresentation = source;
        this.transformSheet = transformSheet;
        this.uriResolver = (context == null) ? null : new ContextResolver(
                context);
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
     * Returns the transformer to be used and reused. Creates a new one based on
     * the transformSheet representation and on the URI resolver if no one
     * exists.
     * 
     * @return The transformer to be used and reused.
     */
    public Transformer getTransformer() throws IOException {
        if (this.transformer == null) {
            try {
                // Prepare the XSLT transformer documents
                StreamSource transformSource = new StreamSource(
                        getTransformSheet().getStream());

                if (getTransformSheet().getIdentifier() != null) {
                    transformSource.setSystemId(getTransformSheet()
                            .getIdentifier().getTargetRef().toString());
                }

                // Create the transformer factory
                TransformerFactory transformerFactory = TransformerFactory
                        .newInstance();

                // Set the URI resolver
                if (getUriResolver() != null) {
                    transformerFactory.setURIResolver(getUriResolver());
                }

                // Create a new transformer
                this.transformer = transformerFactory
                        .newTransformer(transformSource);
            } catch (TransformerConfigurationException tce) {
                throw new IOException("Transformer configuration exception. "
                        + tce.getMessage());
            } catch (TransformerFactoryConfigurationError tfce) {
                throw new IOException(
                        "Transformer factory configuration exception. "
                                + tfce.getMessage());
            }
        }

        return this.transformer;
    }

    /**
     * Returns the XSLT transform sheet to apply to the source representation.
     * 
     * @return The XSLT transform sheet to apply.
     */
    public Representation getTransformSheet() {
        return this.transformSheet;
    }

    /**
     * Returns the URI resolver.
     * 
     * @return The URI resolver.
     */
    public URIResolver getUriResolver() {
        return this.uriResolver;
    }

    /**
     * Returns the URI resolver.
     * 
     * @return The URI resolver.
     * @deprecated Use the getUriResolver method instead.
     */
    @Deprecated
    public URIResolver getURIResolver() {
        return this.uriResolver;
    }

    /**
     * Releases the source and transform sheet representations, the transformer
     * and the URI resolver.
     */
    @Override
    public void release() {
        if (this.sourceRepresentation != null) {
            this.sourceRepresentation.release();
            this.sourceRepresentation = null;
        }

        if (this.transformer != null) {
            this.transformer = null;
        }

        if (this.transformSheet != null) {
            this.transformSheet.release();
            this.transformSheet = null;
        }

        if (this.uriResolver != null) {
            this.uriResolver = null;
        }

        super.release();
    }

    /**
     * Sets the source representation to transform.
     * 
     * @param source
     *                The source representation to transform.
     */
    public void setSourceRepresentation(Representation source) {
        this.sourceRepresentation = source;
    }

    /**
     * Sets the transformer to be used and reused.
     * 
     * @param transformer
     *                The transformer to be used and reused.
     */
    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Sets the XSLT transform sheet to apply to message entities.
     * 
     * @param transformSheet
     *                The XSLT transform sheet to apply to message entities.
     */
    public void setTransformSheet(Representation transformSheet) {
        this.transformSheet = transformSheet;
    }

    /**
     * Sets the URI resolver.
     * 
     * @param uriResolver
     *                The URI resolver.
     */
    public void setUriResolver(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            // Prepare the source and result documents
            StreamSource sourceDocument = new StreamSource(
                    getSourceRepresentation().getStream());

            if (getSourceRepresentation().getIdentifier() != null) {
                sourceDocument.setSystemId(getSourceRepresentation()
                        .getIdentifier().getTargetRef().toString());
            }

            StreamResult resultDocument = new StreamResult(outputStream);

            // Generates the result of the transformation
            getTransformer().transform(sourceDocument, resultDocument);
        } catch (TransformerException te) {
            throw new IOException("Transformer exception. " + te.getMessage());
        }
    }
}
