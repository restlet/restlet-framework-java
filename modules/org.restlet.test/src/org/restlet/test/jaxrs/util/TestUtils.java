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
package org.restlet.test.jaxrs.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;

import org.restlet.data.Language;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Utility methods for the tests.
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("all")
public class TestUtils {
    /**
     * Reads the full inputStream and returns an byte-array of it
     * 
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] getByteArray(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(4096);
        Util.copyStream(inputStream, byteStream);
        return byteStream.toByteArray();
    }

    public static MediaType createMediaType(String type, String subtype,
            String... keysAndValues) {
        return Util.createMediaType(type, subtype, keysAndValues);
    }

    /**
     * @param rootResourceClass
     * @return
     */
    public static <A> Set<A> createSet(A... objects) {
        return Util.createSet(objects);
    }

    /**
     * @param objetcs
     * @return
     */
    public static <A> List<A> createList(A... objects) {
        return Util.createList(objects);
    }

    /**
     * @param languages
     * @return
     */
    public static <A> A getOnlyElement(List<A> languages) {
        return Util.getOnlyElement(languages);
    }

    /**
     * @param list
     * @return
     */
    public static <A> A getLastElement(List<A> list) {
        return Util.getLastElement(list);
    }
}
