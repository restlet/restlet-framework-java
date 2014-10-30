/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.resource;

import org.restlet.data.Status;

// [excludes gwt]
/**
 * Descriptor for status annotations.
 * 
 * @author Jerome Louvel
 */
public class StatusAnnotationInfo extends AnnotationInfo {

    /** The status parsed from the annotation value. */
    private final Status status;

    /** The serializeProperties indicator parsed from the annotation value. */
    private final boolean serializeProperties;

    /**
     * Constructor.
     *  @param javaClass
     *            The class or interface that hosts the annotated Java method.
     * @param code
     *            The status code
     * @param serializeProperties
     *            The indicator for serialize properties attribute.
     */
    public StatusAnnotationInfo(Class<?> javaClass, int code, boolean serializeProperties) {
        super(javaClass, null, Integer.toString(code));

        // Parse the main components of the annotation value
        this.status = Status.valueOf(code);
        this.serializeProperties = serializeProperties;
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
        boolean result = (other instanceof StatusAnnotationInfo);

        if (result && (other != this)) {
            StatusAnnotationInfo otherAnnotation = (StatusAnnotationInfo) other;
            result = super.equals(otherAnnotation);

            // Compare the Restlet method
            if (result) {
                result = ((getStatus() == null)
                        && (otherAnnotation.getStatus() == null) || (getStatus() != null)
                        && getStatus().equals(otherAnnotation.getStatus()));
            }
        }

        return result;
    }

    /**
     * Returns the status parsed from the annotation value.
     * 
     * @return The status parsed from the annotation value.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the serializeProperties indicator parsed from the annotation value.
     *
     * @return the serializeProperties indicator parsed from the annotation value.
     */
    public boolean isSerializeProperties() {
        return serializeProperties;
    }

    @Override
    public String toString() {
        return "StatusAnnotationInfo [javaMethod: " + javaMethod
                + ", javaClass: " + getJavaClass() + ", status: " + status
                + ", serializeProperties: " + serializeProperties
                + "]";
    }

}
