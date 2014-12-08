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
