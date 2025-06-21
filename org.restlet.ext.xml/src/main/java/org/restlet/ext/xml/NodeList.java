/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
