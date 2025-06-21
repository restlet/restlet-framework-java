/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Log record formatter which simply outputs the message on a new line. Useful
 * for Web-style logs.
 * 
 * @author Jerome Louvel
 */
public class AccessLogFormatter extends Formatter {

	@Override
	public String format(LogRecord logRecord) {
		return logRecord.getMessage() + '\n';
	}

}
