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

package org.restlet.engine.resource;

import java.util.Objects;

import org.restlet.data.MediaType;
import org.restlet.engine.util.SystemUtils;
import org.restlet.representation.Variant;

// [excludes gwt]
/**
 * Variant that is declared by an annotated Java method.
 * 
 * @author Jerome Louvel
 */
public class VariantInfo extends Variant {

    /** The optional annotation descriptor. */
    private final MethodAnnotationInfo annotationInfo;

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
    public VariantInfo(MediaType mediaType, MethodAnnotationInfo annotationInfo) {
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
    public VariantInfo(Variant variant, MethodAnnotationInfo annotationInfo) {
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
        if (other == this) {
            return true;
        }
        if (!(other instanceof VariantInfo)) {
            return false;
        }

        VariantInfo that = (VariantInfo) other;

        return super.equals(that)
                && Objects.equals(getAnnotationInfo(), that.getAnnotationInfo());
    }

    /**
     * Returns the optional annotation descriptor.
     * 
     * @return The optional annotation descriptor.
     */
    public MethodAnnotationInfo getAnnotationInfo() {
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
    
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(super.hashCode(), annotationInfo);
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
