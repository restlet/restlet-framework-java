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

package org.restlet.ext.sip.internal;

import static org.restlet.engine.header.HeaderUtils.isComma;
import static org.restlet.engine.header.HeaderUtils.isLinearWhiteSpace;
import static org.restlet.engine.header.HeaderUtils.isSemiColon;

import java.io.IOException;

import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.header.HeaderReader;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.ext.sip.Address;

/**
 * Address like header reader.
 * 
 * @author Thierry Boileau
 */
public class AddressReader extends HeaderReader<Address> {

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public AddressReader(String header) {
        super(header);
    }

    /**
     * Reads the next reference. The first character must be a '&lt;'.
     * 
     * @return The next reference.
     * @throws IOException
     */
    private String readReference() throws IOException {
        String result = null;
        int next = read();

        // First character must be a "<"
        if (next == '<') {
            StringBuilder buffer = new StringBuilder();

            while (result == null) {
                next = read();

                if (next == '>') {
                    // End of URL
                    result = buffer.toString();
                } else if (next == -1) {
                    throw new IOException(
                            "Unexpected end of reference. Please check your value");
                } else {
                    buffer.append((char) next);
                }
            }
        } else {
            throw new IOException("A reference must start with a '<' character");
        }

        return result;
    }

    @Override
    public Address readValue() throws IOException {
        Address result = null;

        skipSpaces();
        if (peek() != -1) {
            result = new Address();
            if (peek() == '"') {
                result.setDisplayName(readQuotedString());
                skipSpaces();
                result.setReference(new Reference(readReference()));
            } else if (peek() == '<') {
                result.setReference(new Reference(readReference()));
            } else if (HeaderUtils.isTokenChar(peek())) {
                // Read value until end or value or parameter separator
                StringBuilder sb = null;
                int next = read();

                while ((next != -1) && !isComma(next) && !isSemiColon(next)) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }

                    sb.append((char) next);
                    next = read();
                }

                // Remove trailing spaces
                if (sb != null) {
                    for (int i = sb.length() - 1; (i >= 0)
                            && isLinearWhiteSpace(sb.charAt(i)); i--) {
                        sb.deleteCharAt(i);
                    }
                }

                // Unread the separator
                if (isComma(next) || isSemiColon(next)) {
                    unread();
                }

                // The last token is the reference
                int index = sb.lastIndexOf(" ");
                if (index != -1) {
                    if (sb.charAt(index + 1) == '<') {
                        if (sb.charAt(sb.length() - 1) == '>') {
                            result.setReference(new Reference(sb.substring(
                                    index + 2, sb.length() - 1)));
                        } else {
                            throw new IOException(
                                    "Unexpected end of reference. Please check your value");
                        }
                    }
                    result.setDisplayName(sb.substring(0, index).trim());
                } else {
                    result.setReference(new Reference(sb.toString()));
                }
            }
        }

        // Read address parameters.
        if (skipParameterSeparator()) {
            Parameter param = readParameter();

            while (param != null) {
                result.getParameters().add(param);

                if (skipParameterSeparator()) {
                    param = readParameter();
                } else {
                    param = null;
                }
            }
        }

        return result;
    }
}
