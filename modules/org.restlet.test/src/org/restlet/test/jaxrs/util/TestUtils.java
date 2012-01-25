/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.jaxrs.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.restlet.engine.io.BioUtils;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Utility methods for the tests.
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("all")
public class TestUtils {
    /**
     * @param objetcs
     * @return
     */
    public static <A> List<A> createList(A... objects) {
        return Util.createList(objects);
    }

    public static MediaType createMediaType(String type, String subtype,
            String... keysAndValues) {
        return new MediaType(type, subtype, Util.createMap(keysAndValues));
    }

    /**
     * @param rootResourceClass
     * @return
     */
    public static <A> Set<A> createSet(A... objects) {
        return Util.createSet(objects);
    }

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
        BioUtils.copy(inputStream, byteStream);
        return byteStream.toByteArray();
    }

    /**
     * @param list
     * @return
     */
    public static <A> A getLastElement(List<A> list) {
        return Util.getLastElement(list);
    }

    /**
     * @param languages
     * @return
     */
    public static <A> A getOnlyElement(List<A> languages) {
        return Util.getOnlyElement(languages);
    }

    /**
     * Sleeps 100 seconds. Will not throw an InterruptedException
     */
    public static void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // shit happens
        }
    }
}
