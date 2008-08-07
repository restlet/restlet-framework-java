/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
