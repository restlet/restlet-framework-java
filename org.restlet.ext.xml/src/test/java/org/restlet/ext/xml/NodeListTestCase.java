/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** Unit tests for {@link NodeList}. */
class NodeListTestCase {

    private static final String XML = "<?xml version=\"1.0\"?><root><a/><b/><c/></root>";

    private static org.w3c.dom.NodeList domNodeList() throws Exception {
        Document document =
                DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(new org.xml.sax.InputSource(new java.io.StringReader(XML)));
        return document.getDocumentElement().getChildNodes();
    }

    @Test
    void get_returnsWrappedNodeAtIndex() throws Exception {
        NodeList nodeList = new NodeList(domNodeList());
        assertEquals("a", nodeList.get(0).getNodeName());
        assertEquals("b", nodeList.get(1).getNodeName());
        assertEquals("c", nodeList.get(2).getNodeName());
    }

    @Test
    void item_returnsSameNodeAsGet() throws Exception {
        NodeList nodeList = new NodeList(domNodeList());
        Node viaGet = nodeList.get(1);
        Node viaItem = nodeList.item(1);
        assertEquals(viaGet, viaItem);
    }

    @Test
    void getLength_matchesUnderlyingNodeListLength() throws Exception {
        org.w3c.dom.NodeList wrapped = domNodeList();
        NodeList nodeList = new NodeList(wrapped);
        assertEquals(wrapped.getLength(), nodeList.getLength());
    }

    @Test
    void size_matchesGetLength() throws Exception {
        NodeList nodeList = new NodeList(domNodeList());
        assertEquals(nodeList.getLength(), nodeList.size());
    }

    @Test
    void isEmpty_falseWhenNodesPresent() throws Exception {
        NodeList nodeList = new NodeList(domNodeList());
        assertFalse(nodeList.isEmpty());
    }

    @Test
    void iterator_iteratesOverAllWrappedNodes() throws Exception {
        NodeList nodeList = new NodeList(domNodeList());
        int count = 0;
        for (Node node : nodeList) {
            assertNotNull(node);
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    void asList_supportsStandardListOperations() throws Exception {
        NodeList nodeList = new NodeList(domNodeList());
        assertTrue(nodeList.contains(nodeList.get(0)));
        assertEquals(0, nodeList.indexOf(nodeList.get(0)));
    }
}
