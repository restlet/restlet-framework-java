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
package org.restlet.test.jaxrs.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.restlet.data.Form;
import org.restlet.ext.jaxrs.core.MultivaluedMapImpl;
import org.restlet.ext.jaxrs.util.Converter;

/**
 * @author Stephan
 * 
 */
@Path("/providertest")
public class ProviderTestService {
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @GET
    @Path("byteArray")
    public byte[] getByteArray() {
        return ALPHABET.getBytes();
    }

    @POST
    @Path("byteArray")
    public String putByteArray(byte[] byteArray) {
        return new String(byteArray);
    }

    @GET
    @Path("file")
    public File getFile() {
        return new File(this.getClass().getResource("alphabet.txt").getPath());
    }

    @POST
    @Path("file")
    public String putFile(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        return putInputStream(inputStream);
    }

    @GET
    @Path("InputStream")
    public InputStream getInputStream() {
        return new ByteArrayInputStream(ALPHABET.getBytes());
    }

    @POST
    @Path("InputStream")
    public String putInputStream(InputStream inputStream) throws IOException {
        StringBuilder stb = new StringBuilder();
        int b;
        while ((b = inputStream.read()) >= 0)
            stb.append((char) b);
        return stb.toString();
    }

    @GET
    @Path("jaxb")
    public Person getJaxb() {
        return new Person("Angela", "Merkel");
    }

    @POST
    @Path("jaxb")
    public String putJaxb(Person person) {
        return person.toString();
    }

    @GET
    @Path("jaxbElement")
    public JAXBElement<Person> getJaxbElement() {
        return new JAXBElement<Person>(new QName(""), Person.class, getJaxb());
    }

    @POST
    @Path("jaxbElement")
    public String putJaxb(JAXBElement<Person> person) {
        return person.getValue().toString();
    }

    @GET
    @Path("form")
    public Form getForm() {
        Form form = new Form();
        form.add("firstname", "Angela");
        form.add("lastname", "Merkel");
        return form;
    }

    @POST
    @Path("form")
    public String putForm(Form form) {
        return form.toString();
    }

    @GET
    @Path("MultivaluedMap")
    public MultivaluedMap<String, String> getMMap() {
        MultivaluedMap<String, String> mmap = new MultivaluedMapImpl<String, String>();
        mmap.add("firstname", "Angela");
        mmap.add("lastname", "Merkel");
        return mmap;
    }

    @POST
    @Path("MultivaluedMap")
    public String putMMap(MultivaluedMap<String, String> mmap) {
        return Converter.toForm(mmap).toString();
    }
}