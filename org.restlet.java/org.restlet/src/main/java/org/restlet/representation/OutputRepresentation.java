/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import java.io.IOException;
import java.io.InputStream;

import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/**
 * Representation based on a BIO output stream. This class is a good basis to
 * write your own representations, especially for the dynamic and large
 * ones.<br>
 * <br>
 * For this you just need to create a subclass and override the abstract
 * Representation.write(OutputStream) method. This method will later be called
 * back by the connectors when the actual representation's content is needed.
 * 
 * @author Jerome Louvel
 */
public abstract class OutputRepresentation extends StreamRepresentation {
	/**
	 * Constructor.
	 * 
	 * @param mediaType The representation's mediaType.
	 */
	public OutputRepresentation(MediaType mediaType) {
		super(mediaType);
	}

	/**
	 * Constructor.
	 * 
	 * @param mediaType    The representation's mediaType.
	 * @param expectedSize The expected input stream size.
	 */
	public OutputRepresentation(MediaType mediaType, long expectedSize) {
		super(mediaType);
		setSize(expectedSize);
	}

	/**
	 * Returns a stream with the representation's content. Internally, it uses a
	 * writer thread and a pipe stream.
	 * 
	 * @return A stream with the representation's content.
	 */
	@Override
	public InputStream getStream() throws IOException {
		return IoUtils.getStream(this);
	}

}
