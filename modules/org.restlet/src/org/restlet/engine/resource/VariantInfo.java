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

    /** Affinity between this variant and an incoming representation. */
    private float inputScore;

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
        inputScore = 1.0f;
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
        this(variant.getMediaType(), annotationInfo);
        setCharacterSet(variant.getCharacterSet());
        setEncodings(variant.getEncodings());
        setLanguages(variant.getLanguages());
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
     * Returns the optional annotation descriptor.
     * 
     * @return The optional annotation descriptor.
     */
    public AnnotationInfo getAnnotationInfo() {
        return annotationInfo;
    }
    

    /**
     * Returns the affinity between this variant and an incoming representation.
     * 
     * @return The affinity between this variant and an incoming representation.
     */
    public float getInputScore() {
        return inputScore;
    }

    /**
     * Sets the affinity between this variant and an incoming representation.
     * 
     * @param inputScore
     *            The affinity between this variant and an incoming
     *            representation.
     */
    public void setInputScore(float inputScore) {
        this.inputScore = inputScore;
    }
}
