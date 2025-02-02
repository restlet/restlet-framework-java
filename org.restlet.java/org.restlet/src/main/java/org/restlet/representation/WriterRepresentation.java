/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

import java.io.IOException;
import java.io.Reader;

/**
 * Representation based on a BIO characters writer. This class is a good basis
 * to write your own representations, especially for the dynamic and large ones.
 * <br>
 * <br>
 * For this you just need to create a subclass and override the abstract
 * Representation.write(Writer) method. This method will later be called back by
 * the connectors when the actual representation's content is needed.
 * 
 * @author Jerome Louvel
 */
public abstract class WriterRepresentation extends CharacterRepresentation {

	/**
	 * Constructor.
	 * 
	 * @param mediaType The representation's mediaType.
	 */
	public WriterRepresentation(MediaType mediaType) {
		super(mediaType);
	}

	/**
	 * Constructor.
	 * 
	 * @param mediaType    The representation's mediaType.
	 * @param expectedSize The expected writer size in bytes.
	 */
	public WriterRepresentation(MediaType mediaType, long expectedSize) {
		super(mediaType);
		setSize(expectedSize);
	}

	@Override
	public Reader getReader() throws IOException {
		return IoUtils.getReader(this);
	}

}
