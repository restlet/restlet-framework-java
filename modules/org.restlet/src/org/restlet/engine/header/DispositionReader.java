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
     * @param header
     *            The header to read.
     */
    public DispositionReader(String header) {
        super(header);
    }

    @Override
    public Disposition readValue() throws IOException {
        Disposition result = null;
        String type = readToken();

        if (type.length() > 0) {
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
