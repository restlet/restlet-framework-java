/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream decorator to trap close() calls so that the decorated stream
 * does not get closed.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 */
public class UnclosableOutputStream extends FilterOutputStream {

	/**
	 * Constructor.
	 * 
	 * @param source The decorated source stream.
	 */
	public UnclosableOutputStream(OutputStream source) {
		super(source);
	}

	@Override
	public void close() throws IOException {
	}
}
