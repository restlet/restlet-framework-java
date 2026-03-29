/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.io.IOException;

/**
 * Token header reader.
 *
 * @author Jerome Louvel
 */
public class TokenReader extends HeaderReader<String> {

    /**
     * Constructor.
     *
     * @param header The header to read.
     */
    public TokenReader(String header) {
        super(header);
    }

    @Override
    public String readValue() throws IOException {
        return readToken();
    }
}
