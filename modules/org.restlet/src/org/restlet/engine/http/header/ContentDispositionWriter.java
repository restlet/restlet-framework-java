/**
 * Copyright 2005-2010 Noelios Technologies.
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

import org.restlet.data.Disposition;
import org.restlet.data.Parameter;

/**
 * Disposition manipulation utilities.
 * 
 * @author Thierry Boileau
 */
public class ContentDispositionWriter {

    /**
     * Formats a disposition.
     * 
     * @param disposition
     *            The disposition to format.
     * @return The formatted disposition.
     */
    public static String write(Disposition disposition) {
        if (Disposition.TYPE_NONE.equals(disposition.getType())
                || disposition.getType() == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();

        sb.append(disposition.getType());
        for (Parameter parameter : disposition.getParameters()) {
            sb.append("; ");
            sb.append(parameter.getName());
            sb.append("=");
            if (HeaderUtils.isToken(parameter.getValue())) {
                sb.append(parameter.getValue());
            } else {
                try {
                    HeaderUtils.appendQuotedString(parameter.getValue(), sb);
                } catch (IOException e) {
                    // IOExceptions are not possible on StringBuilders
                }
            }
        }
        return sb.toString();
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private ContentDispositionWriter() {
    }

}
