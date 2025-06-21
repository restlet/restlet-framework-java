/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.log;

import java.io.IOException;

/**
 * Log file handler that uses the {@link AccessLogFormatter} by default. Also
 * useful in configuration files to differentiate from the
 * {@link java.util.logging.FileHandler}.
 * 
 * @author Jerome Louvel
 */
public class AccessLogFileHandler extends java.util.logging.FileHandler {
	/**
	 * Constructor.
	 * 
	 * @throws IOException
	 * @throws SecurityException
	 */
	public AccessLogFileHandler() throws IOException, SecurityException {
		super();
		init();
	}

	/**
	 * Constructor.
	 * 
	 * @param pattern The name of the output file.
	 * @throws IOException
	 * @throws SecurityException
	 */
	public AccessLogFileHandler(String pattern) throws IOException, SecurityException {
		super(pattern);
		init();
	}

	/**
	 * Constructor.
	 * 
	 * @param pattern The name of the output file.
	 * @param append  Specifies append mode.
	 * @throws IOException
	 * @throws SecurityException
	 */
	public AccessLogFileHandler(String pattern, boolean append) throws IOException, SecurityException {
		super(pattern, append);
		init();
	}

	/**
	 * Constructor.
	 * 
	 * @param pattern The name of the output file.
	 * @param limit   The maximum number of bytes to write to any one file.
	 * @param count   The number of files to use.
	 * @throws IOException
	 * @throws SecurityException
	 */
	public AccessLogFileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
		super(pattern, limit, count);
		init();
	}

	/**
	 * Constructor.
	 * 
	 * @param pattern The name of the output file.
	 * @param limit   The maximum number of bytes to write to any one file.
	 * @param count   The number of files to use.
	 * @param append  Specifies append mode.
	 * @throws IOException
	 * @throws SecurityException
	 */
	public AccessLogFileHandler(String pattern, int limit, int count, boolean append)
			throws IOException, SecurityException {
		super(pattern, limit, count, append);
		init();
	}

	/**
	 * Initialization code common to all constructors.
	 */
	protected void init() {
		setFormatter(new AccessLogFormatter());
	}

}
