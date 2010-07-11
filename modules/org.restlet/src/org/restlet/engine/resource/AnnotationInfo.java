/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

// [excludes gwt]
/**
 * Descriptor for method annotations.
 * 
 * @author Jerome Louvel
 */
public class AnnotationInfo {

    /** The annotated Java method. */
    private final java.lang.reflect.Method javaMethod;

    private final Class<?> resourceInterface;

    /** The matching Restlet method. */
    private final Method restletMethod;

    /** The annotation value. */
    private final String value;

    /**
     * Indicates if the current variant is equal to the given variant.
     * 
     * @param other
     *            The other variant.
     * @return True if the current variant includes the other.
     */
    @Override
    public boolean equals(Object other) {
        boolean result = (other instanceof AnnotationInfo);

        if (result && (other != this)) {
            AnnotationInfo otherAnnotation = (AnnotationInfo) other;

            // Compare the method
            if (result) {
                result = ((getJavaMethod() == null)
                        && (otherAnnotation.getJavaMethod() == null) || (getJavaMethod() != null)
                        && getJavaMethod().equals(
                                otherAnnotation.getJavaMethod()));
            }

            // Compare the resource interface
            if (result) {
                result = ((getResourceInterface() == null)
                        && (otherAnnotation.getResourceInterface() == null) || (getResourceInterface() != null)
                        && getResourceInterface().equals(
                                otherAnnotation.getResourceInterface()));
            }

            // Compare the Restlet method
            if (result) {
                result = ((getRestletMethod() == null)
                        && (otherAnnotation.getRestletMethod() == null) || (getRestletMethod() != null)
                        && getRestletMethod().equals(
                                otherAnnotation.getRestletMethod()));
            }

            // Compare the value
            if (result) {
                result = ((getValue() == null)
                        && (otherAnnotation.getValue() == null) || (getValue() != null)
                        && getValue().equals(otherAnnotation.getValue()));
            }
        }

        return result;
    }

    /**
     * Constructor.
     * 
     * @param resourceInterface
     *            The interface that hosts the annotated Java method.
     * @param restletMethod
     *            The matching Restlet method.
     * @param javaMethod
     *            The annotated Java method.
     * @param value
     *            The annotation value.
     */
    public AnnotationInfo(Class<?> resourceInterface, Method restletMethod,
            java.lang.reflect.Method javaMethod, String value) {
        super();
        this.resourceInterface = resourceInterface;
        this.restletMethod = restletMethod;
        this.javaMethod = javaMethod;
        this.value = value;
    }

    /**
     * Returns the input types of the Java method.
     * 
     * @return The input types of the Java method.
     */
    public Class<?>[] getJavaInputTypes() {
        int count = getJavaMethod().getParameterTypes().length;
        Class<?>[] classes = new Class[count];
        for (int i = 0; i < count; i++) {
            classes[i] = GenericTypeResolver.resolveParameterType(
                    getJavaMethod(), i, resourceInterface);
        }
        return classes;
    }

    /**
     * Returns the annotated Java method.
     * 
     * @return The annotated Java method.
     */
    public java.lang.reflect.Method getJavaMethod() {
        return javaMethod;
    }

    /**
     * Returns the output type of the Java method.
     * 
     * @return The output type of the Java method.
     */
    public Class<?> getJavaOutputType() {
        return GenericTypeResolver.resolveReturnType(getJavaMethod(),
                resourceInterface);
    }

    // [ifndef gwt] method
    /**
     * Returns a list of request variants based on the annotation value.
     * 
     * @param metadataService
     *            The metadata service to use.
     * @return A list of request variants.
     */
    @SuppressWarnings("unchecked")
    public List<Variant> getRequestVariants(MetadataService metadataService,
            org.restlet.service.ConverterService converterService) {
        List<Variant> result = null;
        Class<?>[] classes = getJavaInputTypes();

        if (classes != null && classes.length >= 1) {
            String value = getInputValue();

            if (value != null) {
                String[] extensions = value.split("\\|");

                if (extensions != null) {
                    for (String extension : extensions) {
                        List<MediaType> mediaTypes = metadataService
                                .getAllMediaTypes(extension);

                        if (mediaTypes != null) {
                            if (result == null) {
                                result = new ArrayList<Variant>();
                            }

                            for (MediaType mediaType : mediaTypes) {
                                result.add(new Variant(mediaType));
                            }
                        }
                    }
                }
            }

            if (result == null) {
                Class<?> inputClass = classes[0];
                result = (List<Variant>) converterService.getVariants(
                        inputClass, null);
            }
        }

        return result;
    }

    /**
     * Returns the resource interface value.
     * 
     * @return The resource interface value.
     */
    public Class<?> getResourceInterface() {
        return resourceInterface;
    }

    // [ifndef gwt] method
    /**
     * Returns a list of response variants based on the annotation value.
     * 
     * @param requestEntity
     *            Optional request entity.
     * @param metadataService
     *            The metadata service to use.
     * @param converterService
     *            The converter service to use.
     * @return A list of response variants.
     */
    @SuppressWarnings("unchecked")
    public List<Variant> getResponseVariants(Representation requestEntity,
            MetadataService metadataService,
            org.restlet.service.ConverterService converterService) {
        List<Variant> result = null;

        if ((getJavaOutputType() != null)
                && (getJavaOutputType() != void.class)
                && (getJavaOutputType() != Void.class)) {
            String value = getOutputValue();
            boolean compatibleRequestEntity = true;

            if (value != null) {
                if ((requestEntity != null) && requestEntity.isAvailable()) {
                    List<Variant> requestVariants = getRequestVariants(
                            metadataService, converterService);

                    if ((requestVariants != null) && !requestVariants.isEmpty()) {
                        // Check that the compatibility
                        compatibleRequestEntity = false;

                        for (int i = 0; (!compatibleRequestEntity)
                                && (i < requestVariants.size()); i++) {
                            compatibleRequestEntity = (requestVariants.get(i)
                                    .isCompatible(requestEntity));
                        }
                    } else {
                        compatibleRequestEntity = false;
                    }
                }

                if (compatibleRequestEntity) {
                    String[] extensions = value.split("\\|");
                    for (String extension : extensions) {
                        List<MediaType> mediaTypes = metadataService
                                .getAllMediaTypes(extension);

                        if (mediaTypes != null) {
                            for (MediaType mediaType : mediaTypes) {
                                if ((result == null)
                                        || (!result.contains(mediaType))) {
                                    if (result == null) {
                                        result = new ArrayList<Variant>();
                                    }

                                    result.add(new Variant(mediaType));
                                }
                            }
                        }
                    }
                }
            }

            if (compatibleRequestEntity && (result == null)) {
                result = (List<Variant>) converterService.getVariants(
                        getJavaOutputType(), null);
            }
        }

        return result;
    }

    /**
     * Returns the matching Restlet method.
     * 
     * @return The matching Restlet method.
     */
    public Method getRestletMethod() {
        return restletMethod;
    }

    /**
     * Returns the input part of the annotation value.
     * 
     * @return The input part of the annotation value.
     */
    public String getInputValue() {
        String result = getValue();

        if (result != null) {
            int colonIndex = result.indexOf(':');

            if (colonIndex != -1) {
                result = result.substring(0, colonIndex);
            }
        }

        return result;
    }

    /**
     * Returns the output part of the annotation value.
     * 
     * @return The output part of the annotation value.
     */
    public String getOutputValue() {
        String result = getValue();

        if (result != null) {
            int colonIndex = result.indexOf(':');

            if (colonIndex != -1) {
                result = result.substring(colonIndex + 1);
            }
        }

        return result;
    }

    /**
     * Returns the annotation value.
     * 
     * @return The annotation value.
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "AnnotationInfo [javaMethod=" + javaMethod
                + ", resourceInterface=" + resourceInterface
                + ", restletMethod=" + restletMethod + ", value=" + value + "]";
    }

}