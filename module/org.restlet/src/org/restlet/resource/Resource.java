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

package org.restlet.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.data.Reference;
import org.restlet.data.Status;

/**
 * Intended conceptual target of a hypertext reference. "Any information that
 * can be named can be a resource: a document or image, a temporal service (e.g.
 * "today's weather in Los Angeles"), a collection of other resources, a
 * non-virtual object (e.g. a person), and so on. In other words, any concept
 * that might be the target of an author's hypertext reference must fit within
 * the definition of a resource."<br/><br/> "The only thing that is required
 * to be static for a resource is the semantics of the mapping, since the
 * semantics is what distinguishes one resource from another." Roy T. Fielding<br/>
 * <br/> Another definition adapted from the URI standard (RFC 3986): a resource
 * is the conceptual mapping to a representation (also known as entity) or set
 * of representations, not necessarily the representation which corresponds to
 * that mapping at any particular instance in time. Thus, a resource can remain
 * constant even when its content (the representations to which it currently
 * corresponds) changes over time, provided that the conceptual mapping is not
 * changed in the process. In addition, a resource is always identified by a
 * URI.
 * 
 * @see <a
 *      href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_1">Source
 *      dissertation</a>
 * @see org.restlet.resource.Representation
 * @see org.restlet.data.Reference
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Resource {
    /** The logger to use. */
    private Logger logger;

    /** The identifier. */
    private Reference identifier;

    /** The modifiable list of variants. */
    private List<Variant> variants;

    /**
     * Constructor.
     */
    public Resource() {
        this((Logger) null);
    }

    /**
     * Constructor.
     * 
     * @param logger
     *            The logger to use.
     */
    public Resource(Logger logger) {
        this.logger = logger;
        this.identifier = null;
        this.variants = null;
    }

    /**
     * Indicates if it is allowed to delete the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowDelete() {
        return false;
    }

    /**
     * Indicates if it is allowed to get the variants. The default value is
     * true.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowGet() {
        return true;
    }

    /**
     * Indicates if it is allowed to post to the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPost() {
        return false;
    }

    /**
     * Indicates if it is allowed to put to the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPut() {
        return false;
    }

    /**
     * Asks the resource to delete itself and all its representations.
     * 
     * @return The result information.
     */
    public Result delete() {
        return new Result(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Returns the official identifier.
     * 
     * @return The official identifier.
     */
    public Reference getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the logger to use.
     * 
     * @return The logger to use.
     */
    public Logger getLogger() {
        if (this.logger == null)
            this.logger = Logger.getLogger(Resource.class.getCanonicalName());
        return this.logger;
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
        Representation result = null;

        if (variant instanceof Representation) {
            result = (Representation) variant;
        }

        return result;
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
        if (this.variants == null)
            this.variants = new ArrayList<Variant>();
        return this.variants;
    }

    /**
     * Posts a representation to the resource.
     * 
     * @param entity
     *            The posted entity.
     * @return The result information.
     */
    public Result post(Representation entity) {
        return new Result(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Puts a representation in the resource.
     * 
     * @param entity
     *            A new or updated representation.
     * @return The result information.
     */
    public Result put(Representation entity) {
        return new Result(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Sets the official identifier.
     * 
     * @param identifier
     *            The official identifier.
     */
    public void setIdentifier(Reference identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the official identifier from a URI string.
     * 
     * @param identifierUri
     *            The official identifier to parse.
     */
    public void setIdentifier(String identifierUri) {
        setIdentifier(new Reference(identifierUri));
    }

    /**
     * Sets the logger to use.
     * 
     * @param logger
     *            The logger to use.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets a new list of variants.
     * 
     * @param variants
     *            The new list of variants.
     */
    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

}
