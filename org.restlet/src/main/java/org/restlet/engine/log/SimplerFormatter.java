/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Special log formatter that displays the level, the logger name and the actual
 * message. It also displays the stack trace if available.
 * 
 * This is particularly useful for debugging.
 * 
 * @author Jerome Louvel
 */
public class SimplerFormatter extends Formatter {

	/**
	 * Format the given LogRecord.
	 * 
	 * @param record the log record to be formatted.
	 * @return a formatted log record
	 */
	public synchronized String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();

		sb.append(record.getLevel().getLocalizedName());
		sb.append(" [");
		sb.append(record.getLoggerName());
		sb.append("] - ");
		sb.append(formatMessage(record));
		sb.append('\n');

		if (record.getThrown() != null) {
			try {
				sb.append(System.getProperty("line.separator"));
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
			}
		}

		return sb.toString();
	}

}
