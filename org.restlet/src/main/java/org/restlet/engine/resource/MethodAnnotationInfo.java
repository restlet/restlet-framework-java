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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.engine.util.StringUtils;
import org.restlet.engine.util.SystemUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

// [excludes gwt]
/**
 * Descriptor for method annotations.
 * 
 * @author Jerome Louvel
 */
public class MethodAnnotationInfo extends AnnotationInfo {

    /** The input part of the annotation value. */
    private final String input;

    /** The output part of the annotation value. */
    private final String output;

    /** The optional query part of the annotation value. */
    private final String query;

    /** The matching Restlet method. */
    private final Method restletMethod;

    /**
     * Constructor.
     * 
     * @param javaClass
     *            The class or interface that hosts the annotated Java method.
     * @param restletMethod
     *            The matching Restlet method.
     * @param javaMethod
     *            The annotated Java method.
     * @param annotationValue
     *            The annotation value.
     */
    public MethodAnnotationInfo(Class<?> javaClass, Method restletMethod,
            java.lang.reflect.Method javaMethod, String annotationValue) {
        super(javaClass, javaMethod, annotationValue);
        this.restletMethod = restletMethod;

        // Parse the main components of the annotation value
        if (!StringUtils.isNullOrEmpty(annotationValue)) {
            int queryIndex = annotationValue.indexOf('?');

            if (queryIndex != -1) {
                this.query = annotationValue.substring(queryIndex + 1);
                annotationValue = annotationValue.substring(0, queryIndex);
            } else {
                this.query = null;
            }

            int ioSeparatorIndex = annotationValue.indexOf(':');

            if (ioSeparatorIndex != -1) {
                this.input = annotationValue.substring(0, ioSeparatorIndex);
                this.output = annotationValue.substring(ioSeparatorIndex + 1);
            } else {
                this.input = annotationValue;
                this.output = annotationValue;
            }

        } else {
            this.query = null;
            this.input = null;
            this.output = null;
        }
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
        if (!(other instanceof MethodAnnotationInfo)) {
            return false;
        }

        MethodAnnotationInfo that = (MethodAnnotationInfo) other;

        return super.equals(that)
                && Objects.equals(getRestletMethod(), that.getRestletMethod());
    }

    /**
     * Returns the input part of the annotation value.
     * 
     * @return The input part of the annotation value.
     */
    public String getInput() {
        return input;
    }

    /**
     * Returns the generic type for the given input parameter.
     * 
     * @param index
     *            The input parameter index.
     * 
     * @return The generic type.
     */
    private Class<?> getJavaInputType(int index) {
        return getJavaActualType(javaMethodImpl.getParameterTypes()[index],
                javaMethodImpl.getGenericParameterTypes()[index]);
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
            classes[i] = getJavaInputType(i);
        }

        return classes;
    }

    /**
     * Returns the output type of the Java method.
     * 
     * @return The output type of the Java method.
     */
    public Class<?> getJavaOutputType() {
        return getJavaActualType(javaMethodImpl.getReturnType(),
                javaMethodImpl.getGenericReturnType());
    }

    /**
     * Returns the output part of the annotation value.
     * 
     * @return The output part of the annotation value.
     */
    public String getOutput() {
        return output;
    }

    /**
     * Returns the optional query part of the annotation value.
     * 
     * @return The optional query part of the annotation value.
     */
    public String getQuery() {
        return query;
    }

    // [ifndef gwt] method
    /**
     * Returns a list of request variants based on the annotation value.
     * 
     * @param metadataService
     *            The metadata service to use.
     * @return A list of request variants.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public List<Variant> getRequestVariants(MetadataService metadataService,
            org.restlet.service.ConverterService converterService)
            throws IOException {
        List<Variant> result = null;
        Class<?>[] classes = getJavaInputTypes();

        if (classes != null && classes.length >= 1) {
            result = getVariants(metadataService, getInput());

            if (result == null) {
                Class<?> inputClass = classes[0];

                if (inputClass != null) {
                    result = (List<Variant>) converterService.getVariants(
                            inputClass, null);
                }
            }
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Returns a list of response variants based on the annotation value.
     * 
     * @param metadataService
     *            The metadata service to use.
     * @param converterService
     *            The converter service to use.
     * @return A list of response variants.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public List<Variant> getResponseVariants(MetadataService metadataService,
            org.restlet.service.ConverterService converterService)
            throws IOException {
        List<Variant> result = null;

        if ((getJavaOutputType() != null)
                && (getJavaOutputType() != void.class)
                && (getJavaOutputType() != Void.class)) {
            result = getVariants(metadataService, getOutput());

            if (result == null) {
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

    // [ifndef gwt] method
    /**
     * Returns the list of representation variants associated to a given
     * annotation value, corresponding to either an input or output entity.
     * 
     * @param metadataService
     *            The metadata service to use.
     * @param annotationValue
     *            The entity annotation value.
     * @return A list of variants.
     */
    private List<Variant> getVariants(MetadataService metadataService,
            String annotationValue) {
        List<Variant> result = null;

        if (annotationValue != null) {

            StringTokenizer stValue = new StringTokenizer(annotationValue,
                    "\\|");
            while (stValue.hasMoreTokens()) {
                String variantValue = stValue.nextToken().trim();

                Variant variant = null;
                List<MediaType> mediaTypes = null;
                List<Language> languages = null;
                CharacterSet characterSet = null;

                StringTokenizer stExtension = new StringTokenizer(variantValue,
                        "\\+");
                while (stExtension.hasMoreTokens()) {
                    String extension = stExtension.nextToken().trim();
                    if (extension == null) {
                        continue;
                    }

                    List<Metadata> metadataList = metadataService
                            .getAllMetadata(extension);

                    if (metadataList != null) {
                        for (Metadata metadata : metadataList) {
                            if (metadata instanceof MediaType) {
                                if (mediaTypes == null) {
                                    mediaTypes = new ArrayList<MediaType>();
                                }

                                mediaTypes.add((MediaType) metadata);
                            } else if (metadata instanceof Language) {
                                if (languages == null) {
                                    languages = new ArrayList<Language>();
                                }

                                languages.add((Language) metadata);
                            } else if (metadata instanceof CharacterSet) {
                                if (characterSet == null) {
                                    characterSet = (CharacterSet) metadata;
                                } else {
                                    Context.getCurrentLogger()
                                            .warning(
                                                    "A representation variant can have only one character set. Please check your annotation value.");
                                }
                            }
                        }
                    }
                }

                // Now build the representation variants
                if (mediaTypes != null) {
                    for (MediaType mediaType : mediaTypes) {
                        if ((result == null) || (!result.contains(mediaType))) {
                            if (result == null) {
                                result = new ArrayList<Variant>();
                            }

                            variant = new Variant(mediaType);

                            if (languages != null) {
                                variant.getLanguages().addAll(languages);
                            }

                            if (characterSet != null) {
                                variant.setCharacterSet(characterSet);
                            }

                            result.add(variant);
                        }
                    }
                }
            }
        }

        return result;
    }
    
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(super.hashCode(), restletMethod);
    }

    /**
     * Indicates if the annotated method described is compatible with the given
     * parameters.
     * 
     * @param restletMethod
     *            The Restlet method to match.
     * @param requestEntity
     *            Optional request entity.
     * @param metadataService
     *            The metadata service to use.
     * @param converterService
     *            The converter service to use.
     * @return True if the annotated method is compatible.
     * @throws IOException
     */
    public boolean isCompatible(Method restletMethod, Form queryParams,
            Representation requestEntity, MetadataService metadataService,
            org.restlet.service.ConverterService converterService)
            throws IOException {
        boolean result = true;

        // Verify query parameters
        if (getQuery() != null) {
            Form requiredParams = new Form(getQuery());

            for (Iterator<Parameter> iter = requiredParams.iterator(); iter
                    .hasNext() && result;) {
                result = queryParams.contains(iter.next());
            }
        }

        // Verify HTTP method
        if (result) {
            result = getRestletMethod().equals(restletMethod);
        }

        // Verify request entity
        if (result) {
            result = isCompatibleRequestEntity(requestEntity, metadataService,
                    converterService);

        }

        return result;
    }

    /**
     * Indicates if the given request entity is compatible with the annotated
     * method described.
     * 
     * @param requestEntity
     *            Optional request entity.
     * @param metadataService
     *            The metadata service to use.
     * @param converterService
     *            The converter service to use.
     * @return True if the given request entity is compatible with the annotated
     *         method described.
     * @throws IOException
     */
    public boolean isCompatibleRequestEntity(Representation requestEntity,
            MetadataService metadataService,
            org.restlet.service.ConverterService converterService)
            throws IOException {
        boolean result = true;

        if ((requestEntity != null) && requestEntity.isAvailable()) {
            List<Variant> requestVariants = getRequestVariants(metadataService,
                    converterService);

            if ((requestVariants != null) && !requestVariants.isEmpty()) {
                // Check that the compatibility
                result = false;

                for (int i = 0; (!result) && (i < requestVariants.size()); i++) {
                    result = (requestVariants.get(i)
                            .isCompatible(requestEntity));
                }
            } else {
                result = false;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "MethodAnnotationInfo [javaMethod: " + javaMethod
                + ", javaClass: " + getJavaClass() + ", restletMethod: "
                + restletMethod + ", input: " + getInput() + ", value: "
                + getAnnotationValue() + ", output: " + getOutput()
                + ", query: " + getQuery() + "]";
    }

}
