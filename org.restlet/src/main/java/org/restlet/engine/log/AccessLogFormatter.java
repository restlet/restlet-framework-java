/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Log record formatter which simply outputs the message on a new line. Useful for Web-style logs.
 *
 * @author Jerome Louvel
 */
public class AccessLogFormatter extends Formatter {

    @Override
    public String format(LogRecord logRecord) {
        return logRecord.getMessage() + '\n';
    }
}
