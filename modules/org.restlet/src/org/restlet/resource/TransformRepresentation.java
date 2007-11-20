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
 * Representation able to apply an XSLT transformation. Note that the JAXP
 * Transformer is created when the getTransformer() method is first called, then
 * reused. So, if you need to specify a custom URI resolver, you need to do it
 * before actually using the representation for a transformation.
 * 
 * @author Jerome Louvel (contact@noelios.com) <a
 *         href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class TransformRepresentation extends OutputRepresentation {
    /** The source representation to transform. */
    private Representation source;

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
     *                The XSLT transform sheet.
     */
    public TransformRepresentation(Context context, Representation source,
            Representation transformSheet) {
        super(null);
        this.source = source;
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
        return this.source;
    }

    /**
     * Returns the transformer to be used and reused.
     * 
     * @return The transformer to be used and reused.
     */
    public Transformer getTransformer() throws IOException {
        if (this.transformer == null) {
            try {
                // Prepare the XSLT transformer documents
                StreamSource transformSheet = new StreamSource(
                        getTransformSheet().getStream());

                if (getTransformSheet().getIdentifier() != null) {
                    transformSheet.setSystemId(getTransformSheet()
                            .getIdentifier().getTargetRef().toString());
                }

                // Create the transformer factory
                TransformerFactory transformerFactory = TransformerFactory
                        .newInstance();

                // Set the URI resolver
                if (getURIResolver() != null) {
                    transformerFactory.setURIResolver(getURIResolver());
                }

                // Create a new transformer
                this.transformer = transformerFactory
                        .newTransformer(transformSheet);
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
     * Returns the XSLT transform sheet to apply to message entities.
     * 
     * @return The XSLT transform sheet to apply to message entities.
     */
    public Representation getTransformSheet() {
        return this.transformSheet;
    }

    /**
     * Returns the URI resolver.
     * 
     * @return The URI resolver.
     */
    public URIResolver getURIResolver() {
        return this.uriResolver;
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

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            // Prepare the source and result documents
            StreamSource sourceDocument = new StreamSource(
                    getSourceRepresentation().getStream());
            StreamResult resultDocument = new StreamResult(outputStream);

            if (getSourceRepresentation().getIdentifier() != null) {
                sourceDocument.setSystemId(getSourceRepresentation()
                        .getIdentifier().getTargetRef().toString());
            }

            // Generates the result of the transformation
            getTransformer().transform(sourceDocument, resultDocument);
        } catch (TransformerException te) {
            throw new IOException("Transformer exception. " + te.getMessage());
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

                Response response = this.context.getDispatcher().get(
                        targetRef.getTargetRef().toString());
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
}
