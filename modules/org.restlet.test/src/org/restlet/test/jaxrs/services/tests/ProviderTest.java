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

import java.io.IOException;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.test.jaxrs.services.resources.ProviderTestService;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Stephan Koops
 * @see ProviderTestService
 * @see MessageBodyReader
 * @see MessageBodyWriter
 * @see JsonTest
 */
public class ProviderTest extends JaxRsTestCase {

    private static Form createForm() {
        Form form = new Form();
        form.add("firstname", "Angela");
        form.add("lastname", "Merkel");
        return form;
    }

    /**
     * @param subPath
     * @throws IOException
     * @throws DOMException
     */
    private void getAndCheckJaxb(String subPath) throws Exception {
        Response response = get(subPath);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        DomRepresentation entity = response.getEntityAsDom();
        Node xml = entity.getDocument().getFirstChild();
        System.out.println(subPath + ": " + entity.getText());
        assertEquals("person", xml.getNodeName());
        NodeList nodeList = xml.getChildNodes();
        Node node = nodeList.item(0);
        assertEquals("firstname", node.getNodeName());
        assertEquals("Angela", node.getFirstChild().getNodeValue());
        node = nodeList.item(1);
        assertEquals("lastname", node.getNodeName());
        assertEquals("Merkel", node.getFirstChild().getNodeValue());
        assertEquals(2, nodeList.getLength());
    }

    /**
     * @param subPath
     * @throws IOException
     */
    private Response getAndExpectAlphabet(String subPath) throws IOException {
        Response response = get(subPath);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(ProviderTestService.ALPHABET, entity.getText());
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<?> getRootResourceClass() {
        return ProviderTestService.class;
    }

    /**
     * @param subPath
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private void postAndCheckXml(String subPath) throws Exception {
        Representation send = new DomRepresentation(
                new StringRepresentation(
                        "<person><firstname>Helmut</firstname><lastname>Kohl</lastname></person>\n",
                        MediaType.TEXT_XML));
        Response response = post(subPath, send);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation respEntity = response.getEntity();
        assertEquals("Helmut Kohl", respEntity.getText());
    }

    /**
     * @param subPath
     * @param postEntity
     * @param postMediaType
     * @param responseMediaType
     *                if null, it will not be testet
     * @throws IOException
     */
    private void postAndExceptGiven(String subPath, String postEntity,
            MediaType postMediaType, MediaType responseMediaType)
            throws IOException {
        Representation entity = new StringRepresentation(postEntity,
                postMediaType);
        Response response = post(subPath, entity);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        entity = response.getEntity();
        assertEquals(postEntity, entity.getText());
        if (responseMediaType != null)
            assertEquals(responseMediaType, entity.getMediaType());
    }

    public void testBufferedReaderGet() throws Exception {
        getAndExpectAlphabet("BufferedReader");
    }

    public void testBufferedReaderPost() throws Exception {
        Representation entity = new StringRepresentation("big test",
                MediaType.APPLICATION_OCTET_STREAM);
        Response response = post("BufferedReader", entity);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        entity = response.getEntity();
        assertEquals("big test", entity.getText());
    }

    public void testByteArrayGet() throws Exception {
        getAndExpectAlphabet("byteArray");
    }

    public void testByteArrayPost() throws Exception {
        Representation entity = new StringRepresentation("big test",
                MediaType.APPLICATION_OCTET_STREAM);
        Response response = post("byteArray", entity);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("big test", response.getEntity().getText());
    }

    public void testCharSequenceGet() throws Exception {
        Response response = get("CharSequence");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(ProviderTestService.createCS(), entity.getText());
    }

    public void testCharSequencePost() throws Exception {
        postAndExceptGiven("CharSequence", "a character sequence",
                MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN);
    }

    public void testFileGet() throws Exception {
        getAndExpectAlphabet("file");
    }

    public void testFilePost() throws Exception {
        Response response = post("file", new StringRepresentation("big test",
                MediaType.APPLICATION_OCTET_STREAM));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("big test", response.getEntity().getText());
    }

    public void testFormGet() throws Exception {
        Response response = get("form");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals("firstname=Angela&lastname=Merkel", entity.getText());
    }

    public void testFormPost() throws Exception {
        Response response = post("form", createForm().getWebRepresentation());
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String respEntity = response.getEntity().getText();
        assertEquals("[firstname: Angela, lastname: Merkel]", respEntity);
    }

    public void testInputStreamGet() throws Exception {
        getAndExpectAlphabet("InputStream");
    }

    public void testInputStreamPost() throws Exception {
        Representation entity = new StringRepresentation("big test",
                MediaType.APPLICATION_OCTET_STREAM);
        Response response = post("InputStream", entity);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        entity = response.getEntity();
        assertEquals("big test", entity.getText());
    }

    public void testJaxbElementGet() throws Exception {
        getAndCheckJaxb("jaxbElement");
    }

    public void testJaxbElementPost() throws Exception {
        postAndCheckXml("jaxbElement");
    }

    public void testJaxbGet() throws Exception {
        getAndCheckJaxb("jaxb");
    }

    public void testJaxbPost() throws Exception {
        postAndCheckXml("jaxb");
    }

    public void testMultivaluedMapGet() throws Exception {
        Response response = get("MultivaluedMap");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals("lastname=Merkel&firstname=Angela", entity.getText());
    }

    public void testMultivaluedMapPost() throws Exception {
        Response response = post("MultivaluedMap", createForm()
                .getWebRepresentation());
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        MediaType respMediaType = response.getEntity().getMediaType();
        assertEqualMediaType(MediaType.TEXT_PLAIN, respMediaType);
        String respEntity = response.getEntity().getText();
        assertEquals("[lastname: Merkel, firstname: Angela]", respEntity);
    }

    public void testReaderGet() throws Exception {
        getAndExpectAlphabet("Reader");
    }

    public void testReaderPost() throws Exception {
        postAndExceptGiven("Reader", "big test",
                MediaType.APPLICATION_OCTET_STREAM, null);
    }

    public void testStringBuilderGet() throws Exception {
        getAndExpectAlphabet("StringBuilder");
    }

    public void testStringGet() throws Exception {
        getAndExpectAlphabet("String");
    }

    public void testStringPost() throws Exception {
        postAndExceptGiven("String", "another String", MediaType.TEXT_PLAIN,
                MediaType.TEXT_PLAIN);
    }

    public void testSubStringGet() throws Exception {
        Response response = get("String/substring;start=5;end=9");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("FGHI", response.getEntity().getText());
    }

    public void testXmlTransformGet() throws Exception {
        Response response = get("source");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String entity = response.getEntity().getText();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><abc/>", entity);
    }

    public void testXmlTransformPost() throws Exception {
        Response response = post("source", new StringRepresentation("abcdefg",
                MediaType.TEXT_XML));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abcdefg", response.getEntity().getText());
    }
}