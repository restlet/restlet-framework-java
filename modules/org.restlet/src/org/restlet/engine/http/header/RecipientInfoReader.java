/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http.header;

import java.io.IOException;
import java.util.Collection;

import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.RecipientInfo;

/**
 * Recipient info header reader.
 * 
 * @author Jerome Louvel
 */
public class RecipientInfoReader extends HeaderReader<RecipientInfo> {
    /**
     * Adds values to the given collection.
     * 
     * @param header
     *            The header to read.
     * @param collection
     *            The collection to update.
     */
    public static void addValues(Parameter header,
            Collection<RecipientInfo> collection) {
        new RecipientInfoReader(header.getValue()).addValues(collection);
    }

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public RecipientInfoReader(String header) {
        super(header);
    }

    @Override
    public RecipientInfo readValue() throws IOException {
        RecipientInfo result = new RecipientInfo();
        String protocolToken = readToken();

        if (peek() == '/') {
            read();
            result.setProtocol(new Protocol(protocolToken, protocolToken, null,
                    -1, readToken()));
        } else {
            result.setProtocol(new Protocol("HTTP", "HTTP", null, -1,
                    protocolToken));
        }

        // Move to the next text
        if (skipSpaces()) {
            result.setName(readRawText());

            // Move to the next text
            if (skipSpaces()) {
                result.setComment(readComment());
            }
        }

        return result;
    }
}
