package org.restlet.engine.util;

import org.restlet.representation.Variant;

public class VariantInfo {

    private AnnotationInfo annotationInfo;

    private Variant variant;

    public VariantInfo(AnnotationInfo annotationInfo, Variant variant) {
        this.annotationInfo = annotationInfo;
        this.variant = variant;
    }

    public AnnotationInfo getAnnotationInfo() {
        return annotationInfo;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setAnnotationInfo(AnnotationInfo annotationInfo) {
        this.annotationInfo = annotationInfo;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }
}
