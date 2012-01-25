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

package org.restlet.ext.emf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLOptions;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EMOFResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLOptionsImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.emf.internal.EmfHtmlWriter;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

/**
 * Representation based on the EMF library. By default, it can serialize and
 * deserialize automatically in either XML, XMI or ECore.
 * 
 * @see <a href="http://www.eclipse.org/modeling/emf/">EMF project</a>
 * @author Jerome Louvel
 * @param <T>
 *            The type to wrap.
 */
public class EmfRepresentation<T extends EObject> extends OutputRepresentation {

    /** The maximum number of characters per line. */
    private volatile int lineWidth;

    /** The (parsed) object to format. */
    private volatile T object;

    /** The representation to parse. */
    private volatile Representation representation;

    /** Indicates if EMF references should be written as URI anchors. */
    private volatile boolean usingEncodedAttributeStyle;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The target media type. Supported values are
     *            {@link MediaType#APPLICATION_XMI},
     *            {@link MediaType#APPLICATION_ECORE} and XML media types.
     * @param object
     *            The object to format.
     */
    public EmfRepresentation(MediaType mediaType, T object) {
        super(mediaType);
        this.object = object;
        this.representation = null;
        this.usingEncodedAttributeStyle = true;
        this.lineWidth = 80;
    }

    /**
     * Constructor.
     * 
     * @param representation
     *            The representation to parse.
     */
    public EmfRepresentation(Representation representation) {
        super(representation.getMediaType());
        this.object = null;
        this.representation = representation;
    }

    /**
     * Creates and configure an EMF resource. Not to be confused with a Restlet
     * resource. By default, it calls {@link #createEmfXmlResource(MediaType)}.
     * 
     * @param mediaType
     *            The associated media type.
     * @return A new configured EMF resource.
     */
    protected Resource createEmfResource(MediaType mediaType) {
        return createEmfXmlResource(mediaType);
    }

    /**
     * Creates and configure an EMF resource. Not to be confused with a Restlet
     * resource.
     * 
     * @param mediaType
     *            The associated media type (ECore, XMI or XML).
     * @return A new configured EMF resource.
     */
    protected XMLResource createEmfXmlResource(MediaType mediaType) {
        XMLResource result = null;

        if (MediaType.APPLICATION_ECORE.isCompatible(getMediaType())) {
            result = new EMOFResourceImpl();
        } else if (MediaType.APPLICATION_XMI.isCompatible(getMediaType())) {
            result = new XMIResourceImpl();
        } else {
            result = new XMLResourceImpl();
        }

        if (getCharacterSet() != null) {
            result.setEncoding(getCharacterSet().getName());
        } else {
            result.setEncoding(CharacterSet.UTF_8.getName());
        }

        // Set XML load options
        result.getDefaultLoadOptions().put(
                XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
        result.getDefaultLoadOptions().put(
                XMLResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);
        result.getDefaultLoadOptions().put(
                XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);

        // Set XML save options
        result.getDefaultSaveOptions().put(
                XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
        result.getDefaultSaveOptions().put(XMLResource.OPTION_LINE_WIDTH,
                getLineWidth());
        result.getDefaultSaveOptions().put(
                XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE,
                isUsingEncodedAttributeStyle());
        result.getDefaultSaveOptions().put(XMLResource.OPTION_SCHEMA_LOCATION,
                Boolean.TRUE);

        // Set other XML options
        XMLOptions xmlOptions = new XMLOptionsImpl();
        xmlOptions.setProcessAnyXML(true);
        xmlOptions.setProcessSchemaLocations(true);
        result.getDefaultLoadOptions().put(XMLResource.OPTION_XML_OPTIONS,
                xmlOptions);

        return result;
    }

    /**
     * Returns the maximum number of characters per line. Defaults to 80.
     * 
     * @return The maximum number of characters per line.
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * Returns the loading options. Null by default.
     * 
     * @return The loading options.
     */
    protected Map<?, ?> getLoadOptions() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public T getObject() {
        T result = null;

        if (this.object != null) {
            result = this.object;
        } else if (this.representation != null) {
            try {
                Resource emfResource = createEmfResource(this.representation
                        .getMediaType());
                emfResource.load(this.representation.getStream(),
                        getLoadOptions());
                result = (T) emfResource.getContents().get(0);
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to parse the object with an EMF resource.", e);
            }
        }

        return result;
    }

    /**
     * Returns the saving options. Null by default.
     * 
     * @return The saving options.
     */
    protected Map<?, ?> getSaveOptions() {
        return null;
    }

    /**
     * Indicates if EMF references should be written as URI anchors.
     * 
     * @return True if EMF references should be written as URI anchors.
     */
    public boolean isUsingEncodedAttributeStyle() {
        return usingEncodedAttributeStyle;
    }

    /**
     * Sets the maximum number of characters per line.
     * 
     * @param lineWidth
     *            The maximum number of characters per line.
     */
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Indicates if EMF references should be written as URI anchors.
     * 
     * @param usingEncodedAttributeStyle
     *            True if EMF references should be written as URI anchors.
     */
    public void setUsingEncodedAttributeStyle(boolean usingEncodedAttributeStyle) {
        this.usingEncodedAttributeStyle = usingEncodedAttributeStyle;
    }

    /**
     * Writes the representation based on a given EMF object.
     * 
     * @param object
     *            The EMF object to serialize.
     * @param outputStream
     *            The target output stream.
     * @throws IOException
     */
    public void write(EObject object, OutputStream outputStream)
            throws IOException {
        if (MediaType.TEXT_HTML.isCompatible(getMediaType())) {
            EmfHtmlWriter htmlWriter = new EmfHtmlWriter(object);
            htmlWriter.write(new OutputStreamWriter(outputStream,
                    ((getCharacterSet() == null) ? CharacterSet.ISO_8859_1
                            .getName() : getCharacterSet().getName())));
        } else {
            Resource emfResource = createEmfResource(getMediaType());
            emfResource.getContents().add(object);
            emfResource.save(outputStream, getSaveOptions());
        }
    }

    /**
     * If this representation wraps an {@link EObject}, then it tries to write
     * it as either XML, XMI or ECore/EMOF depending on the media type set.
     * 
     * Note that in order to write this {@link EObject}, an EMF resource is
     * created, configured for proper serialization and the {@link EObject} is
     * then added to the content of this resource. This could has a side effect
     * of removing it from a previous resource/container.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (this.representation != null) {
            this.representation.write(outputStream);
        } else if (object != null) {
            write(this.object, outputStream);
        }
    }
}
