/**
 * Copyright 2005-2020 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.rebind;

import java.util.ArrayList;
import java.util.List;

import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.representation.Variant;
import org.restlet.client.service.MetadataService;

/**
 * Descriptor for method annotations.
 * 
 * @author Jerome Louvel
 */
public class AnnotationInfo {
    /** The annotated Java method. */
    private final java.lang.reflect.Method javaMethod;

    /** The matching Restlet method. */
    private final Method restletMethod;

    /** The annotation value. */
    private final String value;

    /**
     * Constructor.
     * 
     * @param restletMethod
     *            The matching Restlet method.
     * @param javaMethod
     *            The annotated Java method.
     * @param value
     *            The annotation value.
     */
    public AnnotationInfo(Method restletMethod,
            java.lang.reflect.Method javaMethod, String value) {
        super();
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
        return getJavaMethod().getParameterTypes();
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
        return getJavaMethod().getReturnType();
    }

    /**
     * Returns a list of request variants based on the annotation value.
     * 
     * @param metadataService
     *            The metadata service to use.
     * @return A list of response variants.
     */
    public List<Variant> getRequestVariants(MetadataService metadataService) {
        List<Variant> result = null;
        String value = getValue();

        if (value != null) {
            int colonIndex = value.indexOf(':');

            if (colonIndex != -1) {
                value = getValue().substring(0, colonIndex);
            }

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
     * Returns the annotation value.
     * 
     * @return The annotation value.
     */
    private String getValue() {
        return value;
    }
}
