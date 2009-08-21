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

package org.restlet.engine.resource;

import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.representation.Variant;

/**
 * Variant that is declared by an annotated Java method.
 * 
 * @author Jerome Louvel
 */
public class VariantInfo extends Variant {

    /** The optional annotation descriptor. */
    private volatile AnnotationInfo annotationInfo;

    /** The optional converter helper. */
    private volatile ConverterHelper converterHelper;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     */
    public VariantInfo(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     * @param annotationInfo
     *            The optional annotation descriptor.
     */
    public VariantInfo(MediaType mediaType, AnnotationInfo annotationInfo) {
        super(mediaType);
        this.annotationInfo = annotationInfo;
    }

    /**
     * Constructor.
     * 
     * @param variant
     *            The variant to enrich.
     * @param annotationInfo
     *            The optional annotation descriptor.
     */
    public VariantInfo(Variant variant, AnnotationInfo annotationInfo) {
        super(variant.getMediaType());
        setCharacterSet(variant.getCharacterSet());
        setEncodings(variant.getEncodings());
        setLanguages(variant.getLanguages());
        this.annotationInfo = annotationInfo;
    }

    /**
     * Returns the optional annotation descriptor.
     * 
     * @return The optional annotation descriptor.
     */
    public AnnotationInfo getAnnotationInfo() {
        return annotationInfo;
    }

    /**
     * Returns the optional converter helper.
     * 
     * @return The optional converter helper.
     */
    public ConverterHelper getConverterHelper() {
        return converterHelper;
    }

    /**
     * Sets the annotation descriptor.
     * 
     * @param annotationInfo
     *            The annotation descriptor.
     */
    public void setAnnotationInfo(AnnotationInfo annotationInfo) {
        this.annotationInfo = annotationInfo;
    }

    /**
     * Sets the optional converter helper.
     * 
     * @param converterHelper
     *            The optional converter helper.
     */
    public void setConverterHelper(ConverterHelper converterHelper) {
        this.converterHelper = converterHelper;
    }

}
