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

import org.restlet.data.Warning;
import org.restlet.engine.util.DateUtils;

/**
 * Warning manipulation utilities.
 * 
 * @author Thierry Boileau
 */
public class WarningUtils {

    /**
     * Formats a warning.
     * 
     * @param warning
     *            The warning to format.
     * @return The formatted warning.
     * @throws IllegalArgumentException
     *             If the Cookie contains illegal values.
     */
    public static String format(Warning warning)
            throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();

        try {
            format(warning, sb);
        } catch (IOException e) {
            // IOExceptions are not possible on StringBuilders
        }

        return sb.toString();
    }

    /**
     * Formats a warning.
     * 
     * @param warning
     *            The warning to format.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     * @throws IllegalArgumentException
     *             If the warning contains illegal values.
     */
    public static void format(Warning warning, Appendable destination)
            throws IllegalArgumentException, IOException {
        final String agent = warning.getAgent();
        final String text = warning.getText();

        if (warning.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Can't write warning. Invalid status code detected");
        }

        if ((agent == null) || (agent.length() == 0)) {
            throw new IllegalArgumentException(
                    "Can't write warning. Invalid agent detected");
        }

        if ((text == null) || (text.length() == 0)) {
            throw new IllegalArgumentException(
                    "Can't write warning. Invalid text detected");
        }
        destination.append(Integer.toString(warning.getStatus().getCode()));
        destination.append(" ");
        destination.append(agent);
        destination.append(" ");
        HeaderUtils.appendQuotedString(text, destination);

        if (warning.getDate() != null) {
            HeaderUtils.appendQuotedString(DateUtils.format(warning.getDate()),
                    destination);
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private WarningUtils() {
    }

}
