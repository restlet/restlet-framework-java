/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.header;

import java.io.IOException;
import java.util.Collection;

import org.restlet.client.data.CacheDirective;
import org.restlet.client.data.Header;

/**
 * Cache directive header reader.
 * 
 * @author Jerome Louvel
 */
public class CacheDirectiveReader extends HeaderReader<CacheDirective> {

    /**
     * Adds values to the given collection.
     * 
     * @param header
     *            The header to read.
     * @param collection
     *            The collection to update.
     */
    public static void addValues(Header header,
            Collection<CacheDirective> collection) {
        new CacheDirectiveReader(header.getValue()).addValues(collection);
    }

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public CacheDirectiveReader(String header) {
        super(header);
    }

    @Override
    public CacheDirective readValue() throws IOException {
        return readNamedValue(CacheDirective.class);
    }

}
