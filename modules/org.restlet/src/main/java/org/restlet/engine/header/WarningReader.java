/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.header;

import java.io.IOException;
import java.util.Collection;

import org.restlet.data.Header;
import org.restlet.data.Status;
import org.restlet.data.Warning;
import org.restlet.engine.util.DateUtils;

/**
 * Warning header reader.
 * 
 * @author Thierry Boileau
 */
public class WarningReader extends HeaderReader<Warning> {

    /**
     * Adds values to the given collection.
     * 
     * @param header
     *            The header to read.
     * @param collection
     *            The collection to update.
     */
    public static void addValues(Header header, Collection<Warning> collection) {
        new WarningReader(header.getValue()).addValues(collection);
    }

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public WarningReader(String header) {
        super(header);
    }

    @Override
    public Warning readValue() throws IOException {
        Warning result = new Warning();

        String code = readToken();
        skipSpaces();
        String agent = readRawText();
        skipSpaces();
        String text = readQuotedString();
        // The date is not mandatory
        skipSpaces();
        String date = null;
        if (peek() != -1) {
            date = readQuotedString();
        }

        if ((code == null) || (agent == null) || (text == null)) {
            throw new IOException("Warning header malformed.");
        }

        result.setStatus(Status.valueOf(Integer.parseInt(code)));
        result.setAgent(agent);
        result.setText(text);
        if (date != null) {
            result.setDate(DateUtils.parse(date));
        }

        return result;
    }

}
