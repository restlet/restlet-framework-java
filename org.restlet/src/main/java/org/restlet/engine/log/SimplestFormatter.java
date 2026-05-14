/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Special log formatter that displays the actual message only. It also displays the stack trace if
 * available.
 *
 * <p>This is particularly useful for debugging.
 *
 * @author Jerome Louvel
 */
public class SimplestFormatter extends Formatter {

    /**
     * Format the given LogRecord.
     *
     * @param logRecord the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord logRecord) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatMessage(logRecord));
        sb.append('\n');

        if (logRecord.getThrown() != null) {
            try {
                sb.append(System.lineSeparator());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                logRecord.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw);
            } catch (Exception ignored) {
                // Ignored
            }
        }

        return sb.toString();
    }
}
