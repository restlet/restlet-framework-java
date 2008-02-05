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

package org.restlet.test.jaxrs.services.tests;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.ProviderTestService;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProviderTest extends JaxRsTestCase {

    private static final Class<?> SERVICE_CLASS = ProviderTestService.class;

    @SuppressWarnings("unchecked")
    @Override
    protected Class<?> getRootResourceClass() {
        return SERVICE_CLASS;
    }

    public void testByteArray() throws Exception {
        Response response = get("byteArray");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation representation = response.getEntity();
        assertEquals(ProviderTestService.ALPHABET, representation.getText());

        // TODO POST byteArray
        // Representation entity = new StringRepresentation("big test");
        // response = post(SERVICE_CLASS, "byteArray", entity);
        // assertEquals(Status.SUCCESS_OK, response.getStatus());
        // representation = response.getEntity();
        // assertEquals("big test", representation.getText());
    }

    public void testFile() throws Exception {
        Response response = get("file");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(ProviderTestService.ALPHABET, entity.getText());

        // TODO POST file
    }

    public void testInputStream() throws Exception {
        Response response = get("InputStream");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(ProviderTestService.ALPHABET, entity.getText());

        // TODO POST InputStream
    }

    public void testJaxb() throws Exception {
        Response response = get("jaxb");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        DomRepresentation entity = response.getEntityAsDom();
        Node xml = entity.getDocument().getFirstChild();
        assertEquals("person", xml.getNodeName());
        NodeList nodeList = xml.getChildNodes();
        Node node = nodeList.item(0);
        assertEquals("firstname", node.getNodeName());
        assertEquals("Angela", node.getFirstChild().getNodeValue());
        node = nodeList.item(1);
        assertEquals("lastname", node.getNodeName());
        assertEquals("Merkel", node.getFirstChild().getNodeValue());
        assertEquals(2, nodeList.getLength());

        // TODO POST jaxb
    }

    public void testXmlTransform() throws Exception {
        // TODO XmlTransform
    }
}