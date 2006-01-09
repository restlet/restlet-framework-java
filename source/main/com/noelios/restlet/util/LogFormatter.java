/*
 * Copyright 2005-2006 Jérôme LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Log record formatter which only outputs the message on a new line.
 * Useful for Web-style logs.
 */
public class LogFormatter extends Formatter
{
   /**
    * Format the given log record and return the formatted string.
    * @param logRecord The log record to be formatted.
    * @return The formatted log record.
    */
   public String format(LogRecord logRecord)
   {
      return logRecord.getMessage() + '\n';
   }

}
