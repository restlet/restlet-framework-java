/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.ext.rdf;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Couple;

/**
 * Linked client resource. In addition to regular client resources, this class
 * offers additional method aware of links exposed by RDF, making it natural to
 * navigate the Web of data.
 * 
 * @author Jerome Louvel
 */
public class RdfClientResource extends ClientResource {

    /** The links cache. */
    private Graph links;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public RdfClientResource(Context context, Method method, Reference reference) {
        super(context, method, reference);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public RdfClientResource(Context context, Method method, String uri) {
        super(context, method, uri);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public RdfClientResource(Context context, Method method, URI uri) {
        super(context, method, uri);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param reference
     *            The target reference.
     */
    public RdfClientResource(Context context, Reference reference) {
        super(context, reference);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The current context.
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public RdfClientResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public RdfClientResource(Context context, String uri) {
        super(context, uri);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public RdfClientResource(Context context, URI uri) {
        super(context, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public RdfClientResource(Method method, Reference reference) {
        super(method, reference);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public RdfClientResource(Method method, String uri) {
        super(method, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public RdfClientResource(Method method, URI uri) {
        super(method, uri);
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The target reference.
     */
    public RdfClientResource(Reference reference) {
        super(reference);
    }

    /**
     * Constructor.
     * 
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public RdfClientResource(Request request, Response response) {
        super(request, response);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public RdfClientResource(String uri) {
        super(uri);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public RdfClientResource(URI uri) {
        super(uri);
    }

    /**
     * Returns all the linked resources, based on the RDF representation
     * exposed.
     * 
     * @return All the linked resources.
     * @see #getLinks()
     */
    public Set<RdfClientResource> getLinked() {
        return getLinked((Collection<Reference>) null);
    }

    /**
     * Returns the linked resources, based on the RDF representation exposed.
     * The type of links to follow can be restricted.
     * 
     * @param typeRefs
     *            The set of types references of the links to select or null.
     * @return All the linked resources.
     * @see #getLinks()
     */
    public Set<RdfClientResource> getLinked(Collection<Reference> typeRefs) {
        Set<RdfClientResource> result = null;

        Graph links = getLinks();

        if (links != null) {
            result = new HashSet<RdfClientResource>();

            for (Link link : links) {
                if (link.hasReferenceTarget()) {
                    if ((typeRefs == null)
                            || typeRefs.contains(link.getTypeRef())) {
                        result.add(new RdfClientResource(getContext(), link
                                .getTargetAsReference()));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the linked resources, based on the RDF representation exposed.
     * The type of links to follow can be restricted.
     * 
     * @param typeRef
     *            The type reference of the links to select or null.
     * @return All the linked resources.
     * @see #getLinks()
     */
    public Set<RdfClientResource> getLinked(Reference typeRef) {
        return getLinked(Collections.singleton(typeRef));
    }

    /**
     * Returns the links exposed by this resource.
     * 
     * @return The links exposed by this resource.
     */
    public Graph getLinks() {
        Graph result = this.links;

        if (result == null) {
            ClientInfo currentInfo = getClientInfo();

            // Customize the preferences to maximize the chance of getting RDF
            ClientInfo newInfo = new ClientInfo();
            newInfo.getAcceptedMediaTypes().add(
                    new Preference<MediaType>(MediaType.APPLICATION_RDF_XML));
            newInfo.getAcceptedMediaTypes().add(
                    new Preference<MediaType>(MediaType.TEXT_RDF_N3));
            newInfo.getAcceptedMediaTypes().add(
                    new Preference<MediaType>(MediaType.TEXT_RDF_NTRIPLES));
            newInfo.getAcceptedMediaTypes()
                    .add(
                            new Preference<MediaType>(
                                    MediaType.APPLICATION_RDF_TURTLE));
            newInfo.getAcceptedMediaTypes().add(
                    new Preference<MediaType>(MediaType.TEXT_XML, 0.5F));
            newInfo.getAcceptedMediaTypes().add(
                    new Preference<MediaType>(MediaType.TEXT_PLAIN, 0.4F));
            newInfo.getAcceptedMediaTypes().add(
                    new Preference<MediaType>(MediaType.APPLICATION_ALL_XML,
                            0.3F));

            // Attempt to retrieve the RDF representation
            try {
                Representation rep = get();

                if (rep != null) {
                    RdfRepresentation rdfRep = new RdfRepresentation(rep);
                    this.links = rdfRep.getGraph();
                    result = this.links;
                } else {
                    getLogger().log(
                            Level.WARNING,
                            "Unable to retrieve an RDF representation of this resource: "
                                    + getReference());
                }
            } catch (Throwable e) {
                getLogger().log(
                        Level.WARNING,
                        "Unable to retrieve an RDF representation of this resource: "
                                + getReference(), e);
            }

            // Restore previous preferences
            setClientInfo(currentInfo);
        }

        return result;
    }

    /**
     * Returns all the linked literals, based on the RDF representation exposed.
     * 
     * @return All the linked literals.
     * @see #getLinks()
     */
    public Set<Couple<Reference, Literal>> getLiterals() {
        Set<Couple<Reference, Literal>> result = null;

        Graph links = getLinks();

        if (links != null) {
            for (Link link : links) {
                if (link.hasLiteralTarget()) {
                    if (result == null) {
                        result = new HashSet<Couple<Reference, Literal>>();
                    }

                    result.add(new Couple<Reference, Literal>(
                            link.getTypeRef(), link.getTargetAsLiteral()));
                }
            }
        }

        return result;
    }

    /**
     * Returns the linked literals, based on the RDF representation exposed. The
     * type of links to follow can be restricted.
     * 
     * @param typeRef
     *            The type reference of the links to select or null.
     * @return All the linked literals.
     * @see #getLiterals()
     */
    public Set<Literal> getLiterals(Reference typeRef) {
        Set<Literal> result = null;

        Graph links = getLinks();

        if (links != null) {
            result = new HashSet<Literal>();

            for (Link link : links) {
                if (link.hasLiteralTarget()) {
                    if ((typeRef == null) || typeRef.equals(link.getTypeRef())) {
                        result.add(link.getTargetAsLiteral());
                    }
                }
            }
        }

        return result;
    }

    /**
     * Refreshes the links cache.
     */
    public void refresh() {
        this.links = null;
        getLinks();
    }
}
