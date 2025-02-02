/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.resource;

import org.restlet.data.MediaType;
import org.restlet.engine.util.SystemUtils;
import org.restlet.representation.Variant;

import java.util.Objects;

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
	 * @param mediaType The media type.
	 */
	public VariantInfo(MediaType mediaType) {
		this(mediaType, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param mediaType      The media type.
	 * @param annotationInfo The optional annotation descriptor.
	 */
	public VariantInfo(MediaType mediaType, MethodAnnotationInfo annotationInfo) {
		super(mediaType);
		this.annotationInfo = annotationInfo;
		inputScore = 1.0f;
	}

	/**
	 * Constructor.
	 * 
	 * @param variant        The variant to enrich.
	 * @param annotationInfo The optional annotation descriptor.
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
	 * @param other The other variant.
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

		return super.equals(that) && Objects.equals(getAnnotationInfo(), that.getAnnotationInfo());
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
	 * @param inputScore The affinity between this variant and an incoming
	 *                   representation.
	 */
	public void setInputScore(float inputScore) {
		this.inputScore = inputScore;
	}
}
