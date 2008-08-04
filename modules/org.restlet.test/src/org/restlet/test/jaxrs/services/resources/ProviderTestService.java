/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.test.jaxrs.services.resources;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.restlet.data.Form;
import org.restlet.ext.jaxrs.internal.core.MultivaluedMapImpl;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.tests.ProviderTest;
import org.restlet.test.jaxrs.util.TestUtils;
import org.xml.sax.InputSource;

/**
 * @author Stephan Koops
 * @see ProviderTest
 * @see JsonTestService
 */
@SuppressWarnings("all")
@Path("/providerTest")
public class ProviderTestService {
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int CS_LAST_CHAR = 126;

    /**
     * @return
     */
    public static String createCS() {
        final StringBuilder stb = new StringBuilder();
        for (char c = 32; c <= CS_LAST_CHAR; c++) {
            stb.append(c);
        }
        return stb.toString();
    }

    @GET
    @Path("BufferedReader")
    @Produces("application/octet-stream")
    public BufferedReader bufferedReaderGet() {
        return new BufferedReader(readerGet());
    }

    @POST
    @Path("BufferedReader")
    @Produces("text/plain")
    public String bufferedReaderPost(BufferedReader reader) throws IOException {
        final StringBuilder stb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stb.append(line);
            stb.append('\n');
        }
        stb.deleteCharAt(stb.length() - 1);
        return stb.toString();
    }

    @GET
    @Path("byteArray")
    @Produces("application/octet-stream")
    public byte[] byteArrayGet() {
        return ALPHABET.getBytes();
    }

    @POST
    @Path("byteArray")
    @Produces("text/plain")
    public String byteArrayPost(byte[] byteArray) {
        return new String(byteArray);
    }

    /**
     * Returns a {@link CharSequence}, which class is equals to no default.
     * 
     * @return a {@link CharSequence}, which class is equals to no default.
     */
    @GET
    @Path("CharSequence")
    @Produces("text/plain")
    public CharSequence charSequenceGet() {
        return new CharSequence() {

            public char charAt(int index) {
                return (char) (index + 32);
            }

            public int length() {
                return CS_LAST_CHAR - 32 + 1;
            }

            public CharSequence subSequence(int start, int end) {
                return toString().subSequence(start, end);
            }

            @Override
            public String toString() {
                return createCS();
            }
        };
    }

    @POST
    @Path("CharSequence")
    @Produces("text/plain")
    @Consumes("text/plain")
    public String charSequencePost(CharSequence form) {
        return form.toString();
    }

    @GET
    @Path("file")
    @Produces("application/octet-stream")
    public File fileGet() {
        return new File(this.getClass().getResource("alphabet.txt").getPath());
    }

    @POST
    @Path("file")
    @Consumes("application/octet-stream")
    @Produces("text/plain")
    public String filePost(File file) throws IOException {
        final InputStream inputStream = new FileInputStream(file);
        return inputStreamPost(inputStream);
    }

    @GET
    @Path("form")
    @Produces("application/x-www-form-urlencoded")
    public Form formGet() {
        final Form form = new Form();
        form.add("firstname", "Angela");
        form.add("lastname", "Merkel");
        return form;
    }

    @POST
    @Path("form")
    @Produces("text/plain")
    @Consumes("application/x-www-form-urlencoded")
    public String formPost(Form form) {
        return form.toString();
    }

    @GET
    @Path("InputStream")
    @Produces("application/octet-stream")
    public InputStream inputStreamGet() {
        return new ByteArrayInputStream(ALPHABET.getBytes());
    }

    @POST
    @Path("InputStream")
    @Produces("text/plain")
    public String inputStreamPost(InputStream inputStream) throws IOException {
        final StringBuilder stb = new StringBuilder();
        int b;
        while ((b = inputStream.read()) >= 0) {
            stb.append((char) b);
        }
        return stb.toString();
    }

    @GET
    @Path("jaxbElement")
    @Produces("text/xml")
    public JAXBElement<Person> jaxbElementGet() {
        return new JAXBElement<Person>(new QName("qName"), Person.class,
                jaxbGet());
    }

    @GET
    @Path("jaxb")
    @Produces("text/xml")
    public Person jaxbGet() {
        return new Person("Angela", "Merkel");
    }

    @POST
    @Path("jaxbElement")
    @Consumes( { "text/xml", "application/xml" })
    @Produces("text/plain")
    public String jaxbPost(JAXBElement<Person> person) {
        if (person == null) {
            throw new WebApplicationException(Response.serverError().entity(
                    "the JAXBElement is null").build());
        }
        if (person.getValue() == null) {
            return null;
        }
        return person.getValue().toString();
    }

    @POST
    @Path("jaxbElement/rootElement")
    @Consumes( { "text/xml", "application/xml" })
    @Produces("text/plain")
    public String jaxbPostRootElement(JAXBElement<Person> person) {
        if (person == null) {
            throw new WebApplicationException(Response.serverError().entity(
                    "the JAXBElement is null").build());
        }
        if (person.getValue() == null) {
            return null;
        }
        return person.getName().toString();
    }

    @POST
    @Path("jaxb")
    @Consumes( { "text/xml", "application/xml" })
    @Produces("text/plain")
    public String jaxbPost(Person person) {
        return person.toString();
    }

    @GET
    @Path("MultivaluedMap")
    @Produces("application/x-www-form-urlencoded")
    public MultivaluedMap<String, String> mMapGet() {
        final MultivaluedMap<String, String> mmap = new MultivaluedMapImpl<String, String>();
        mmap.add("firstname", "Angela");
        mmap.add("lastname", "Merkel");
        return mmap;
    }

    @POST
    @Path("MultivaluedMap")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/plain")
    public String mMapPost(MultivaluedMap<String, String> mmap) {
        return Converter.toForm(mmap).toString();
    }

    @POST
    @Path("multipart/form-data")
    @Consumes("multipart/form-data")
    public Object multipartPost(@QueryParam("attrNo") int attrNo,
            Multipart multipart) throws MessagingException, IOException {
        final BodyPart bodyPart = multipart.getBodyPart(attrNo);
        return bodyPart.getInputStream();
    }

    @GET
    @Path("Reader")
    @Produces("application/octet-stream")
    public Reader readerGet() {
        return new StringReader(ALPHABET);
    }

    @POST
    @Path("Reader")
    @Produces("text/plain")
    public String readerPost(Reader reader) throws IOException {
        final StringBuilder stb = new StringBuilder();
        int c;
        while ((c = reader.read()) >= 0) {
            stb.append((char) c);
        }
        return stb.toString();
    }

    @GET
    @Path("StringBuilder")
    @Produces("text/plain")
    public StringBuilder stringBuilderGet() {
        return new StringBuilder(ALPHABET);
    }

    @GET
    @Path("String")
    @Produces("text/plain")
    public String stringGet() {
        return ALPHABET;
    }

    @POST
    @Path("String")
    @Produces("text/plain")
    @Consumes("text/plain")
    public String stringPost(String entity) {
        return entity;
    }

    @GET
    @Path("String/substring")
    @Produces("text/plain")
    public String subStringGet(@MatrixParam("start") int start,
            @MatrixParam("end") int end) {
        if (end >= ALPHABET.length()) {
            return ALPHABET.substring(start);
        }
        return ALPHABET.substring(start, end);
    }

    @GET
    @Path("source")
    @Produces("text/xml")
    public Source xsltGet() {
        return new StreamSource(new ByteArrayInputStream("<abc/>".getBytes()));
    }

    @POST
    @Path("source")
    @Consumes("text/xml")
    @Produces("text/plain")
    public byte[] xsltPost(Source source) throws IOException {
        final InputSource inputSource = SAXSource.sourceToInputSource(source);
        return TestUtils.getByteArray(inputSource.getByteStream());
    }
}