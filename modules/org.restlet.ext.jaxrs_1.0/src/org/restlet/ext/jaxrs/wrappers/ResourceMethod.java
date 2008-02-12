/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.jaxrs.wrappers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.util.SortedMetadata;
import org.restlet.ext.jaxrs.util.Util;

/**
 * A method of a resource class annotated with a request method designator that
 * is used to handle requests on the corresponding resource, see section 2.2.
 * 
 * @author Stephan Koops
 * 
 */
public class ResourceMethod extends AbstractMethodWrapper {

    /**
     * Converts the given mimes to a List of MediaTypes. Will never returns
     * null.
     * 
     * @param mimes
     * @return Returns an unmodifiable List of MediaTypes
     */
    static List<MediaType> convertToMediaTypes(String[] mimes) {
        List<MediaType> mediaTypes = new ArrayList<MediaType>(mimes.length);
        for (String mime : mimes) {
            if (mime == null)
                mediaTypes.add(MediaType.ALL);
            else
                mediaTypes.add(MediaType.valueOf(mime));
        }
        return Collections.unmodifiableList(mediaTypes);
    }

    static org.restlet.data.Method getHttpMethod(Method javaMethod) {
        for (Annotation annotation : javaMethod.getAnnotations()) {
            Class<? extends Annotation> annoType = annotation.annotationType();
            HttpMethod httpMethodAnnot = annoType
                    .getAnnotation(HttpMethod.class);
            if (httpMethodAnnot != null) { // Annotation der Annotation der
                // Methode ist HTTP-Methode
                String httpMethodName = httpMethodAnnot.value();
                return org.restlet.data.Method.valueOf(httpMethodName);
            }
        }
        return null;
    }

    private List<MediaType> consumedMimes;

    private org.restlet.data.Method httpMethod;

    private List<MediaType> producedMimes;

    /**
     * Creates a wrapper for a resource method.
     * 
     * @param javaMethod
     *                the Java method to wrap.
     * @param path
     *                the path of the method.
     * @param resourceClass
     *                the wrapped class of the method.
     * @param httpMethod
     *                the HTTP method of the Java method. It will be checked be
     *                the {@link JaxRsRouter}, so avoiding double work. It will
     *                be requested from the javaMethod.
     */
    public ResourceMethod(Method javaMethod, Path path,
            ResourceClass resourceClass, org.restlet.data.Method httpMethod) {
        super(javaMethod, path, resourceClass);
        this.httpMethod = httpMethod;
    }

    /**
     * @return Returns an unmodifiable List with the MediaTypes the given
     *         resourceMethod consumes. If no consumeMime is given, this method
     *         returns a List with MediaType.ALL. Will never return null.
     */
    public List<MediaType> getConsumedMimes() {
        if (this.consumedMimes == null) {
            ConsumeMime consumeMime = this.javaMethod
                    .getAnnotation(ConsumeMime.class);
            if (consumeMime == null)
                consumeMime = this.javaMethod.getAnnotation(ConsumeMime.class);
            if (consumeMime == null)
                this.consumedMimes = Collections.singletonList(MediaType.ALL);
            else
                this.consumedMimes = convertToMediaTypes(consumeMime.value());
        }
        return consumedMimes;
    }

    /**
     * @return Returns the HTTP method supported by the wrapped java method.
     */
    public org.restlet.data.Method getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * @return Returns an unmodifiable List of MediaTypes the given Resource
     *         Method. if the method is not annotated with {@link ProduceMime},
     *         than the {@link ProduceMime} of the Resource class is returned.
     *         If no {@link ProduceMime} can be found, an empty (also
     *         unmodifiable) List will returned.<br/>This method never returns null.
     */
    public List<MediaType> getProducedMimes() {
        if (producedMimes == null) {
            ProduceMime produceMime = this.javaMethod
                    .getAnnotation(ProduceMime.class);
            if (produceMime == null)
                produceMime = this.javaMethod.getAnnotation(ProduceMime.class);
            if (produceMime == null)
                this.producedMimes = Collections.emptyList();
            else
                this.producedMimes = convertToMediaTypes(produceMime.value());
        }
        return producedMimes;
    }

    /**
     * Check if this method supports the media type to produce for a request.
     * 
     * @param accMediaTypess
     *                The Media Types the client would accept, ordered by
     *                quality. See {@link Util#sortMetadataList(Collection)}
     * @return Returns true, if the give MediaType is supported by the method,
     *         or no MediaType is given for the method, otherweise false.
     */
    public boolean isAcceptedMediaTypeSupported(
            SortedMetadata<MediaType> accMediaTypess) {
        if (accMediaTypess == null || accMediaTypess.isEmpty())
            return true;
        List<MediaType> prodMimes = getProducedMimes();
        if (prodMimes.isEmpty())
            return true;
        for (MediaType producedMediaType : prodMimes) {
            //for (Iterable<MediaType> accMediaTypes : accMediaTypess)
                for (MediaType accMediaType : accMediaTypess)
                    if (accMediaType.includes(producedMediaType))
                        return true;
        }
        return false;
    }

    /**
     * @param resourceMethod
     *                the resource method to check
     * @param givenMediaType
     *                the MediaType of the request entity
     * @return Returns true, if the given MediaType is supported by the method,
     *         or no MediaType is given for the method, otherweise false;
     */
    public boolean isGivenMediaTypeSupported(MediaType givenMediaType) {
        if (givenMediaType == null)
            return true;
        for (MediaType consumedMime : this.getConsumedMimes()) {
            if (consumedMime.includes(givenMediaType))
                return true;
        }
        return false;
    }

    /**
     * 
     * @param resourceMethod
     * @param requestedMethod
     * @return true, if the gien java method is annotated with a runtime
     *         designator for the given requested Method. If the requested
     *         method is null, than the method returns true, when the method is
     *         annotated with any runtime desginator.
     * @see #annotatedWithMethodDesignator(Method)
     */
    public boolean isHttpMethodSupported(org.restlet.data.Method requestedMethod) {
        return isHttpMethodSupported(requestedMethod, false);
    }

    /**
     * Checks, if this method suppors the given HTTP method.
     * 
     * @param requestedMethod
     *                the requested Method
     * @param alsoGet
     *                if true, than this method returns also true, if this
     *                method is GET. This functionality is needed for HEAD.
     * @return true, if this method supports the given HTTP method. Returns also
     *         true, if alsoGet is true and this method is true.
     * @throws IllegalArgumentException
     */
    public boolean isHttpMethodSupported(
            org.restlet.data.Method requestedMethod, boolean alsoGet)
            throws IllegalArgumentException {
        if (requestedMethod == null)
            throw new IllegalArgumentException(
                    "null is not a valid HTTP method");
        if (this.httpMethod == null)
            this.httpMethod = getHttpMethod(this.javaMethod);
        if (alsoGet && this.httpMethod.equals(org.restlet.data.Method.GET))
            return true;
        return this.httpMethod.equals(requestedMethod);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + javaMethod.toString()
                + ", " + this.httpMethod + "]";
    }
}