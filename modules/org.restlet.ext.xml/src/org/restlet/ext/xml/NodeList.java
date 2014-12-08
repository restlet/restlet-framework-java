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

package org.restlet.ext.xml;

import java.util.AbstractList;

import org.w3c.dom.Node;

/**
 * DOM nodes set that implements the standard List interface for easier
 * iteration.
 * 
 * @author Jerome Louvel
 */
public class NodeList extends AbstractList<Node> implements
        org.w3c.dom.NodeList {

    /** The wrapped node list. */
    private volatile org.w3c.dom.NodeList nodes;

    /**
     * Constructor.
     * 
     * @param nodes
     *            The node list to wrap.
     */
    public NodeList(org.w3c.dom.NodeList nodes) {
        this.nodes = nodes;
    }

    @Override
    public Node get(int index) {
        return this.nodes.item(index);
    }

    /**
     * {@inheritDoc org.w3c.dom.NodeList#getLength()}
     */
    public int getLength() {
        return this.nodes.getLength();
    }

    /**
     * {@inheritDoc org.w3c.dom.NodeList#item(int)}
     */
    public Node item(int index) {
        return this.nodes.item(index);
    }

    @Override
    public int size() {
        return this.nodes.getLength();
    }

}
