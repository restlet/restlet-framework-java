/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.resource;

import java.util.Objects;
import org.restlet.data.Status;
import org.restlet.engine.util.SystemUtils;

/**
 * Descriptor for status annotations.
 *
 * @author Jerome Louvel
 */
public class ThrowableAnnotationInfo extends AnnotationInfo {

    /** Indicates if the {@link Status#getThrowable()} should be serialized. */
    private final boolean serializable;

    /** The status parsed from the annotation value. */
    private final Status status;

    /**
     * Constructor.
     *
     * @param throwableClass The class or interface that hosts the annotated Java method.
     * @param annotationValue The annotation value containing the HTTP error code.
     * @param serializable Indicates if the {@link Throwable} should be serialized.
     */
    public ThrowableAnnotationInfo(
            Class<?> throwableClass, int annotationValue, boolean serializable) {
        super(throwableClass, Integer.toString(annotationValue));

        // Parse the main parts of the annotation value
        this.status = Status.valueOf(annotationValue);
        this.serializable = serializable;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof final ThrowableAnnotationInfo that)) {
            return false;
        }

        return super.equals(that) && Objects.equals(getStatus(), that.getStatus());
    }

    /**
     * Returns the status parsed from the annotation value.
     *
     * @return The status parsed from the annotation value.
     */
    public Status getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        return SystemUtils.hashCode(super.hashCode(), status);
    }

    /**
     * Returns the serialized indicator parsed from the annotation value.
     *
     * @return the serialized indicator parsed from the annotation value.
     */
    public boolean isSerializable() {
        return serializable;
    }

    @Override
    public String toString() {
        return "ExceptionAnnotationInfo [status=" + status + ", serializable=" + serializable + "]";
    }
}
