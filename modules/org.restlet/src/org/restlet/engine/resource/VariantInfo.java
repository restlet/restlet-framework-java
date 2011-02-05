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

package org.restlet.engine.resource;

import org.restlet.data.MediaType;
import org.restlet.representation.Variant;

// [excludes gwt]
/**
 * Variant that is declared by an annotated Java method.
 * 
 * @author Jerome Louvel
 */
public class VariantInfo extends Variant {

    /** The optional annotation descriptor. */
    private final AnnotationInfo annotationInfo;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     */
    public VariantInfo(MediaType mediaType) {
        this(mediaType, null);
    }

    /**
     * Indicates if the current variant is equal to the given variant.
     * 
     * @param other
     *            The other variant.
     * @return True if the current variant includes the other.
     */
    @Override
    public boolean equals(Object other) {
        boolean result = super.equals(other) && (other instanceof VariantInfo);

        if (result && (other != this)) {
            VariantInfo otherVariant = (VariantInfo) other;

            // Compare the annotation info
            if (result) {
                result = ((getAnnotationInfo() == null)
                        && (otherVariant.getAnnotationInfo() == null) || (getAnnotationInfo() != null)
                        && getAnnotationInfo().equals(
                                otherVariant.getAnnotationInfo()));
            }
        }

        return result;
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
}
