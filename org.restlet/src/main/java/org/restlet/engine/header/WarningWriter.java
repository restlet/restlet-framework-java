/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
     * @param warnings The list of warnings to format.
     * @return The formatted warning.
     */
    public static String write(List<Warning> warnings) {
        return new WarningWriter().append(warnings).toString();
    }

    @Override
    public WarningWriter append(Warning warning) {

        if (warning.getStatus() == null) {
            throw new IllegalArgumentException("Can't write warning. Invalid status code detected");
        }

        String agent = warning.getAgent();
        String text = warning.getText();

        if ((agent == null) || (agent.isEmpty())) {
            throw new IllegalArgumentException("Can't write warning. Invalid agent detected");
        }

        if ((text == null) || (text.isEmpty())) {
            throw new IllegalArgumentException("Can't write warning. Invalid text detected");
        }

        append(Integer.toString(warning.getStatus().getCode()));
        append(" ");
        append(agent);
        append(" ");
        appendQuotedString(text);

        if (warning.getDate() != null) {
            append(" ");
            appendQuotedString(DateUtils.format(warning.getDate()));
        }

        return this;
    }
}
