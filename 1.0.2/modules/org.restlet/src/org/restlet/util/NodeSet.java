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

import java.util.AbstractList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM nodes set that implements the standard List interface for easier
 * iteration.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class NodeSet extends AbstractList<Node> implements List<Node>, NodeList {

    /** The wrapped node list. */
    private NodeList nodes;

    public NodeSet(NodeList nodes) {
        this.nodes = nodes;
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
    public Node get(int index) {
        return this.nodes.item(index);
    }

    @Override
    public int size() {
        return this.nodes.getLength();
    }

}
