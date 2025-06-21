/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream decorator to trap {@code close()} calls so that the underlying
 * stream is not closed.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 * 
 */
public class UnclosableInputStream extends FilterInputStream {

	/**
	 * Constructor.
	 * 
	 * @param source The source input stream.
	 */
	public UnclosableInputStream(InputStream source) {
		super(source);
	}

	@Override
	public void close() throws IOException {
	}
}
