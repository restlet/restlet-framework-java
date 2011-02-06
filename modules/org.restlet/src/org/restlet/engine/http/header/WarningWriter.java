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

import java.util.List;

import org.restlet.data.Warning;
import org.restlet.engine.util.DateUtils;

/**
 * Warning header writer.
 * 
 * @author Thierry Boileau
 */
public class WarningWriter extends HeaderWriter<Warning> {

    /**
     * Writes a warning.
     * 
     * @param warnings
     *            The list of warnings to format.
     * @return The formatted warning.
     */
    public static String write(List<Warning> warnings) {
        return new WarningWriter().append(warnings).toString();
    }

    @Override
    public WarningWriter append(Warning warning) {
        String agent = warning.getAgent();
        String text = warning.getText();

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

        append(Integer.toString(warning.getStatus().getCode()));
        append(" ");
        append(agent);
        append(" ");
        appendQuotedString(text);

        if (warning.getDate() != null) {
            appendQuotedString(DateUtils.format(warning.getDate()));
        }

        return this;
    }

}
