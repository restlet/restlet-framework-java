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
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.CacheDirective;

/**
 * Cache control header reader.
 * 
 * @author Thierry Boileau
 */
public class CacheControlReader extends HeaderReader {
    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public CacheControlReader(String header) {
        super(header);
    }

    /**
     * Reads the list of cache directives.
     * 
     * @return The list of cache directives.
     * @throws IOException
     */
    public List<CacheDirective> readDirectives() throws IOException {
        List<CacheDirective> result = null;
        String directive = readValue();
        if (directive != null) {
            result = new ArrayList<CacheDirective>();
        }
        while (directive != null) {
            int index = directive.indexOf("=");
            if (index != -1) {
                String name = directive.substring(0, index);
                String value = directive.substring(index + 1);
                HeaderReader hr = new HeaderReader(value) {
                    @Override
                    public void readQuotedString(Appendable buffer)
                            throws IOException {
                        int nextChar = read();
                        while (nextChar != '"' && (nextChar != -1)) {
                            nextChar = read();
                        }
                        if (nextChar == '"') {
                            super.readQuotedString(buffer);
                        }
                    }
                };
                result.add(new CacheDirective(name, hr.readQuotedString()));
            } else {
                result.add(new CacheDirective(directive));
            }

            directive = readValue();
        }

        return result;
    }

}
