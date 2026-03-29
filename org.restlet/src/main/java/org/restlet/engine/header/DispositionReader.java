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
import org.restlet.data.Disposition;
import org.restlet.data.Parameter;

/**
 * Disposition header reader.
 *
 * @author Thierry Boileau
 */
public class DispositionReader extends HeaderReader<Disposition> {

    /**
     * Constructor.
     *
     * @param header The header to read.
     */
    public DispositionReader(String header) {
        super(header);
    }

    @Override
    public Disposition readValue() throws IOException {
        Disposition result = null;
        String type = readToken();

        if (!type.isEmpty()) {
            result = new Disposition();
            result.setType(type);

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
        }

        return result;
    }
}
