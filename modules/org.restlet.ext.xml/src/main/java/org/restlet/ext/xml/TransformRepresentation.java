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

package org.restlet.ext.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.restlet.Context;
import org.restlet.ext.xml.internal.AbstractXmlReader;
import org.restlet.ext.xml.internal.ContextResolver;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

/**
 * Representation able to apply an XSLT transformation. The internal JAXP
 * transformer is created when the getTransformer() method is first called. So,
 * if you need to specify a custom URI resolver, do it before actually using the
 * representation for a transformation.<br>
 * <br>
 * This representation should be viewed as a wrapper representation that applies
 * a transform sheet on a source representation when it is read or written out.
 * Therefore, it isn't intended to be reused on different sources. For this use
 * case, you should instead use the {@link org.restlet.routing.Transformer}
 * filter.
 * 
 * @author Jerome Louvel
 */
public class TransformRepresentation extends WriterRepresentation {
    /**
     * Wraps a source representation into a {@link SAXSource}. This method can
     * detect other {@link XmlRepresentation} instances to use their
     * {@link XmlRepresentation#getSaxSource()} method as well as other
     * {@link TransformRepresentation} instances to support transformation
     * chaining.
     * 
     * @param representation
     *            The source representation.
     * @return The SAX source.
     * @throws IOException
     */
    public static SAXSource toSaxSource(Representation representation)
            throws IOException {
        SAXSource result = null;

        if (representation instanceof XmlRepresentation) {
            result = ((XmlRepresentation) representation).getSaxSource();
        } else if (representation instanceof TransformRepresentation) {
            final TransformRepresentation source = (TransformRepresentation) representation;
            XMLReader reader = new AbstractXmlReader() {

                /**
                 * Parses the input source by sending the result event to the
                 * XML reader's content handler.
                 * 
                 * @param input
                 *            The input source.
                 */
                public void parse(InputSource input) throws IOException,
                        SAXException {
                    try {
                        source.getTransformer().transform(
                                source.getSaxSource(),
                                new SAXResult(getContentHandler()));
                    } catch (TransformerException te) {
                        throw new IOException("Transformer exception. "
                                + te.getMessage());
                    }
                }

                public void parse(String systemId) throws IOException,
                        SAXException {
                    throw new IllegalStateException("Not implemented");
                }
            };

            result = new SAXSource(reader, new InputSource(
                    representation.getReader()));
        } else {
            // Prepare the source and result documents
            result = new SAXSource(new InputSource(representation.getReader()));
        }

        // Copy the representation's URI as an XML system ID.
        if (representation.getLocationRef() != null) {
            result.setSystemId(representation.getLocationRef().getTargetRef()
                    .toString());
        }

        return result;
    }

    /** The transformer's error listener. */
    private volatile ErrorListener errorListener;

    /** The JAXP transformer output properties. */
    private volatile Map<String, String> outputProperties;

    /** The JAXP transformer parameters. */
    private volatile Map<String, Object> parameters;

    /** The source representation to transform. */
    private volatile Representation sourceRepresentation;

    /** The template to be used and reused. */
    private volatile Templates templates;

    /** The XSLT transform sheet to apply to message entities. */
    private volatile Representation transformSheet;

    /** The URI resolver. */
    private volatile URIResolver uriResolver;

    /**
     * Constructor. Note that a default URI resolver will be created based on
     * the given context.
     * 
     * @param context
     *            The parent context.
     * @param source
     *            The source representation to transform.
     * @param transformSheet
     *            The XSLT transform sheet to apply.
     */
    public TransformRepresentation(Context context, Representation source,
            Representation transformSheet) {
        this((context == null) ? null : new ContextResolver(context), source,
                transformSheet);
    }

    /**
     * Default constructor.
     * 
     * @param source
     *            The source representation to transform.
     * @param transformSheet
     *            The XSLT transform sheet to apply.
     */
    public TransformRepresentation(Representation source,
            Representation transformSheet) {
        this((URIResolver) null, source, transformSheet);
    }

    /**
     * Constructor. Note that a default URI resolver will be created based on
     * the given context.
     * 
     * @param uriResolver
     *            The JAXP URI resolver.
     * @param source
     *            The source representation to transform.
     * @param transformSheet
     *            The XSLT transform sheet to apply.
     */
    public TransformRepresentation(URIResolver uriResolver,
            Representation source, Representation transformSheet) {
        this(uriResolver, source, transformSheet, null);
    }

    /**
     * Constructor.
     * 
     * @param uriResolver
     *            The optional JAXP URI resolver.
     * @param source
     *            The source representation to transform.
     * @param templates
     *            The precompiled JAXP template.
     */
    private TransformRepresentation(URIResolver uriResolver,
            Representation source, Representation transformSheet,
            Templates templates) {
        super(null);
        this.sourceRepresentation = source;
        this.templates = templates;
        this.transformSheet = transformSheet;
        this.uriResolver = uriResolver;
        this.parameters = new HashMap<String, Object>();
        this.outputProperties = new HashMap<String, String>();
        this.errorListener = null;
    }

    /**
     * Constructor.
     * 
     * @param uriResolver
     *            The optional JAXP URI resolver.
     * @param source
     *            The source representation to transform.
     * @param templates
     *            The precompiled JAXP template.
     */
    public TransformRepresentation(URIResolver uriResolver,
            Representation source, Templates templates) {
        this(uriResolver, source, null, templates);
    }

    /**
     * Returns the transformer's error listener. Default value is null, leaving
     * the original listener intact.
     * 
     * @return The transformer's error listener.
     */
    public ErrorListener getErrorListener() {
        return errorListener;
    }

    /**
     * Returns the modifiable map of JAXP transformer output properties.
     * 
     * @return The JAXP transformer output properties.
     */
    public Map<String, String> getOutputProperties() {
        return this.outputProperties;
    }

    /**
     * Returns the modifiable map of JAXP transformer parameters.
     * 
     * @return The JAXP transformer parameters.
     */
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    /**
     * Returns the SAX source associated to the source representation.
     * 
     * @return The SAX source associated to the source representation.
     * @throws IOException
     */
    public SAXSource getSaxSource() throws IOException {
        return toSaxSource(getSourceRepresentation());
    }

    /**
     * Returns the default SAX transformer factory.
     * 
     * @return The default SAX transformer factory.
     */
    private SAXTransformerFactory getSaxTransformerFactory() {
        SAXTransformerFactory result = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        return result;
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
     * Returns the templates to be used and reused. If no one exists, it creates
     * a new one based on the transformSheet representation and on the URI
     * resolver.
     * 
     * @return The templates to be used and reused.
     */
    public Templates getTemplates() throws IOException {
        if (this.templates == null) {
            if (getTransformSheet() != null) {
                try {
                    // Prepare the XSLT transformer documents
                    final StreamSource transformSource = new StreamSource(
                            getTransformSheet().getStream());

                    if (getTransformSheet().getLocationRef() != null) {
                        transformSource.setSystemId(getTransformSheet()
                                .getLocationRef().getTargetRef().toString());
                    }

                    // Create the transformer factory
                    final TransformerFactory transformerFactory = TransformerFactory
                            .newInstance();

                    // Set the URI resolver
                    if (getUriResolver() != null) {
                        transformerFactory.setURIResolver(getUriResolver());
                    }

                    // Create a new transformer
                    this.templates = transformerFactory
                            .newTemplates(transformSource);
                } catch (TransformerConfigurationException tce) {
                    throw new IOException(
                            "Transformer configuration exception. "
                                    + tce.getMessage());
                }
            }
        }

        return this.templates;
    }

    /**
     * Returns a new transformer to be used. Creation is based on the
     * {@link #getTemplates()}.newTransformer() method.
     * 
     * @return The new transformer to be used.
     */
    public Transformer getTransformer() throws IOException {
        Transformer result = null;

        try {
            Templates templates = getTemplates();

            if (templates != null) {
                result = templates.newTransformer();

                if (getErrorListener() != null) {
                    result.setErrorListener(getErrorListener());
                }

                if (getUriResolver() != null) {
                    result.setURIResolver(getUriResolver());
                }

                for (String name : getParameters().keySet()) {
                    result.setParameter(name, getParameters().get(name));
                }

                for (String name : getOutputProperties().keySet()) {
                    result.setOutputProperty(name,
                            getOutputProperties().get(name));
                }
            }
        } catch (TransformerConfigurationException tce) {
            throw new IOException("Transformer configuration exception. "
                    + tce.getMessage());
        } catch (TransformerFactoryConfigurationError tfce) {
            throw new IOException(
                    "Transformer factory configuration exception. "
                            + tfce.getMessage());
        }

        return result;
    }

    /**
     * Returns the SAX transformer handler associated to the transform sheet.
     * 
     * @return The SAX transformer handler.
     * @throws IOException
     */
    public TransformerHandler getTransformerHandler() throws IOException {
        TransformerHandler result = null;
        Templates templates = getTemplates();

        if (templates != null) {
            try {
                result = getSaxTransformerFactory().newTransformerHandler(
                        templates);
            } catch (TransformerConfigurationException tce) {
                throw new IOException("Transformer configuration exception. "
                        + tce.getMessage());
            }
        }

        return result;
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
     * Returns the SAX XML filter applying the transform sheet to its input.
     * 
     * @return The SAX XML filter.
     * @throws IOException
     */
    public XMLFilter getXmlFilter() throws IOException {
        XMLFilter result = null;
        final Templates templates = getTemplates();

        if (templates != null) {
            try {
                result = getSaxTransformerFactory().newXMLFilter(templates);
            } catch (TransformerConfigurationException tce) {
                throw new IOException("Transformer configuration exception. "
                        + tce.getMessage());
            }
        }

        return result;
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

        if (this.templates != null) {
            this.templates = null;
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
     * Sets the transformer's error listener.
     * 
     * @param errorListener
     *            The transformer's error listener.
     */
    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    /**
     * Sets the modifiable map of JAXP transformer output properties.
     * 
     * @param outputProperties
     *            The JAXP transformer output properties.
     */
    public void setOutputProperties(Map<String, String> outputProperties) {
        this.outputProperties = outputProperties;
    }

    /**
     * Sets the JAXP transformer parameters.
     * 
     * @param parameters
     *            The JAXP transformer parameters.
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the source representation to transform.
     * 
     * @param source
     *            The source representation to transform.
     */
    public void setSourceRepresentation(Representation source) {
        this.sourceRepresentation = source;
    }

    /**
     * Sets the templates to be used and reused.
     * 
     * @param templates
     *            The templates to be used and reused.
     */
    public void setTemplates(Templates templates) {
        this.templates = templates;
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
     * Sets the URI resolver.
     * 
     * @param uriResolver
     *            The URI resolver.
     */
    public void setUriResolver(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    /**
     * Transforms the given JAXP source into the given result.
     * 
     * @param source
     *            The JAXP source object.
     * @param result
     *            The JAXP result object.
     * @throws IOException
     */
    public void transform(Source source, Result result) throws IOException {
        if (getTransformer() == null) {
            Context.getCurrentLogger()
                    .warning(
                            "Unable to apply the transformation. No transformer found!");
        } else {
            try {
                // Generates the result of the transformation
                getTransformer().transform(source, result);
            } catch (TransformerException te) {
                throw new IOException("Transformer exception. "
                        + te.getMessage());
            }
        }
    }

    /**
     * Writes the transformed source into the given JAXP result. The source is
     * retrieved using the {@link #getSaxSource()} method.
     * 
     * @param result
     *            The JAXP result object.
     * @throws IOException
     */
    public void write(Result result) throws IOException {
        transform(getSaxSource(), result);
    }

    /**
     * Writes the transformed source into the given output stream. By default,
     * it leverages the {@link #write(Result)} method using a
     * {@link StreamResult} object.
     */
    @Override
    public void write(Writer writer) throws IOException {
        write(new StreamResult(writer));
    }
}
