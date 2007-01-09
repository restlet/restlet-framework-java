/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.util;

import java.util.List;
import java.util.logging.Logger;

import org.restlet.data.Reference;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Result;
import org.restlet.resource.Variant;

/**
 * Resource wrapper. Useful for application developer who need to enrich the
 * resource with application related properties and behavior.
 * 
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class WrapperResource extends Resource {
    /** The wrapped resource. */
    private Resource wrappedResource;

    /**
     * Constructor.
     * 
     * @param wrappedResource
     *            The wrapped resource.
     */
    public WrapperResource(Resource wrappedResource) {
        this.wrappedResource = wrappedResource;
    }

    /**
     * Indicates if it is allowed to delete the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowDelete() {
        return getWrappedResource().allowDelete();
    }

    /**
     * Indicates if it is allowed to get the variants. The default value is
     * true.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowGet() {
        return getWrappedResource().allowGet();
    }

    /**
     * Indicates if it is allowed to post to the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPost() {
        return getWrappedResource().allowPost();
    }

    /**
     * Indicates if it is allowed to put to the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPut() {
        return getWrappedResource().allowPut();
    }

    /**
     * Asks the resource to delete itself and all its representations.
     * 
     * @return The result information.
     */
    public Result delete() {
        return getWrappedResource().delete();
    }

    /**
     * Returns the official identifier.
     * 
     * @return The official identifier.
     */
    public Reference getIdentifier() {
        return getWrappedResource().getIdentifier();
    }

    /**
     * Returns the logger to use.
     * 
     * @return The logger to use.
     */
    public Logger getLogger() {
        return getWrappedResource().getLogger();
    }

    /**
     * Returns a full representation for a given variant previously returned via
     * the getVariants() method. The default implementation directly returns the
     * variant in case the variants are already full representations. In all
     * other cases, you will need to override this method in order to provide
     * your own implementation. <br/><br/>
     * 
     * This method is very useful for content negotiation when it is too costly
     * to initilize all the potential representations. It allows a resource to
     * simply expose the available variants via the getVariants() method and to
     * actually server the one selected via this method.
     * 
     * @param variant
     *            The variant whose full representation must be returned.
     * @return The full representation for the variant.
     * @see #getVariants()
     */
    public Representation getRepresentation(Variant variant) {
        return (getWrappedResource() == null) ? null : getWrappedResource()
                .getRepresentation(variant);
    }

    /**
     * Returns the list of variants. A variant can be a purely descriptive
     * representation, with no actual content that can be served. It can also be
     * a full representation in case a resource has only one variant or if the
     * initialization cost is very low.
     * 
     * @return The list of variants.
     * @see #getRepresentation(Variant)
     */
    public List<Variant> getVariants() {
        return getWrappedResource().getVariants();
    }

    /**
     * Returns the wrapped Resource.
     * 
     * @return The wrapped Resource.
     */
    protected Resource getWrappedResource() {
        return this.wrappedResource;
    }

    /**
     * Posts a variant representation in the resource.
     * 
     * @param entity
     *            The posted entity.
     * @return The result information.
     */
    public Result post(Representation entity) {
        return getWrappedResource().post(entity);
    }

    /**
     * Puts a variant representation in the resource.
     * 
     * @param variant
     *            A new or updated variant representation.
     * @return The result information.
     */
    public Result put(Representation variant) {
        return getWrappedResource().put(variant);
    }

    /**
     * Sets the official identifier.
     * 
     * @param identifier
     *            The official identifier.
     */
    public void setIdentifier(Reference identifier) {
        getWrappedResource().setIdentifier(identifier);
    }

    /**
     * Sets the official identifier from a URI string.
     * 
     * @param identifierUri
     *            The official identifier to parse.
     */
    public void setIdentifier(String identifierUri) {
        getWrappedResource().setIdentifier(identifierUri);
    }

    /**
     * Sets the logger to use.
     * 
     * @param logger
     *            The logger to use.
     */
    public void setLogger(Logger logger) {
        getWrappedResource().setLogger(logger);
    }

    /**
     * Sets a new list of variants.
     * 
     * @param variants
     *            The new list of variants.
     */
    public void setVariants(List<Variant> variants) {
        getWrappedResource().setVariants(variants);
    }

}
