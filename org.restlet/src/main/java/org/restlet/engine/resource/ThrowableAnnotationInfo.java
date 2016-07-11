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

import org.restlet.data.Status;
import org.restlet.engine.util.SystemUtils;

// [excludes gwt]
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
     * @param throwableClass
     *            The class or interface that hosts the annotated Java method.
     * @param annotationValue
     *            The annotation value containing the HTTP error code.
     * @param serializable
     *            Indicates if the {@link Throwable} should be serialized.
     */
    public ThrowableAnnotationInfo(Class<?> throwableClass,
            int annotationValue, boolean serializable) {
        super(throwableClass, Integer.toString(annotationValue));

        // Parse the main components of the annotation value
        this.status = Status.valueOf(annotationValue);
        this.serializable = serializable;
    }

    /**
     * Indicates if the current object is equal to the given object.
     * 
     * @param other
     *            The other object.
     * @return True if the current object includes the other.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ThrowableAnnotationInfo)) {
            return false;
        }

        ThrowableAnnotationInfo that = (ThrowableAnnotationInfo) other;

        return super.equals(that) 
                && Objects.equals(getStatus(), that.getStatus());
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
     * Returns the serialize indicator parsed from the annotation value.
     * 
     * @return the serialize indicator parsed from the annotation value.
     */
    public boolean isSerializable() {
        return serializable;
    }

    @Override
    public String toString() {
        return "ExceptionAnnotationInfo [status=" + status + ", serializable="
                + serializable + "]";
    }

}
