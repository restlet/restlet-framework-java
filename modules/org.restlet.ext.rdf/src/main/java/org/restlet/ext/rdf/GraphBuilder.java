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

package org.restlet.ext.rdf;

import org.restlet.data.Reference;

/**
 * Graph handler used when parsing an RDF representation. It completes the inner
 * set of links with all detected ones.
 */
public class GraphBuilder extends GraphHandler {

    /** The inner graph of links. */
    private Graph linkSet;

    /**
     * 
     * @param linkSet
     *            The graph of links.
     */
    public GraphBuilder(Graph linkSet) {
        super();
        this.linkSet = linkSet;
    }

    @Override
    public void link(Graph source, Reference typeRef, Literal target) {
        if (source != null && typeRef != null && target != null) {
            this.linkSet.add(source, typeRef, target);
        }
    }

    @Override
    public void link(Graph source, Reference typeRef, Reference target) {
        if (source != null && typeRef != null && target != null) {
            this.linkSet.add(source, typeRef, target);
        }
    }

    @Override
    public void link(Reference source, Reference typeRef, Literal target) {
        if (source != null && typeRef != null && target != null) {
            this.linkSet.add(source, typeRef, target);
        }
    }

    @Override
    public void link(Reference source, Reference typeRef, Reference target) {
        if (source != null && typeRef != null && target != null) {
            this.linkSet.add(source, typeRef, target);
        }
    }

}
