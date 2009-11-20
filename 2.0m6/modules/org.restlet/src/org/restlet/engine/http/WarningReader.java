/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.http;

import java.io.IOException;

import org.restlet.data.Status;
import org.restlet.data.Warning;
import org.restlet.engine.util.DateUtils;

/**
 * Warning header reader.
 * 
 * @author Thierry Boileau
 */
public class WarningReader extends HeaderReader {

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
    public void readQuotedString(Appendable buffer) throws IOException {
        int nextChar = read();
        while (nextChar != '"' && (nextChar != -1)) {
            nextChar = read();
        }
        if (nextChar == '"') {
            super.readQuotedString(buffer);
        }
    }

    /**
     * Indicates if the given character is a value separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a value separator.
     */
    @Override
    public final boolean isValueSeparator(int character) {
        return (character == ' ');
    }

    /**
     * Read the warning header.
     * 
     * @return The next warning.
     * @throws IOException
     */
    public Warning readWarning() throws IOException {
        Warning result = new Warning();

        String code = readValue();
        String agent = readValue();
        String text = readQuotedString();
        String date = readQuotedString();

        if ((code == null) || (agent == null) || (text == null)) {
            throw new IOException("Warning header malformed.");
        }
        result.setStatus(Status.valueOf(Integer.parseInt(code)));
        result.setAgent(agent);
        result.setText(text);
        result.setDate(DateUtils.parse(date));

        return result;
    }

}
