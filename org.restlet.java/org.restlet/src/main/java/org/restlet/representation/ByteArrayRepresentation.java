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

import java.io.ByteArrayInputStream;

/**
 * Representation wrapping a byte array.
 * 
 * @author Jerome Louvel
 */
public class ByteArrayRepresentation extends InputRepresentation {

	/**
	 * Constructor.
	 * 
	 * @param byteArray The byte array to wrap.
	 */
	public ByteArrayRepresentation(byte[] byteArray) {
		super(new ByteArrayInputStream(byteArray));
	}

	/**
	 * 
	 * @param byteArray The byte array to wrap.
	 * @param offSet    The offset inside the byte array.
	 * @param length    The length to expose inside the byte array.
	 */
	public ByteArrayRepresentation(byte[] byteArray, int offSet, int length) {
		super(new ByteArrayInputStream(byteArray, offSet, length));
	}

	/**
	 * 
	 * @param byteArray The byte array to wrap.
	 * @param offSet    The offset inside the byte array.
	 * @param length    The length to expose inside the byte array.
	 * @param mediaType
	 */
	public ByteArrayRepresentation(byte[] byteArray, int offSet, int length, MediaType mediaType) {
		super(new ByteArrayInputStream(byteArray, offSet, length), mediaType);
	}

	/**
	 * 
	 * @param byteArray    The byte array to wrap.
	 * @param offSet       The offset inside the byte array.
	 * @param length       The length to expose inside the byte array.
	 * @param mediaType    The media type.
	 * @param expectedSize
	 */
	public ByteArrayRepresentation(byte[] byteArray, int offSet, int length, MediaType mediaType, long expectedSize) {
		super(new ByteArrayInputStream(byteArray, offSet, length), mediaType, expectedSize);
	}

	/**
	 * Constructor.
	 * 
	 * @param byteArray The byte array to wrap.
	 * @param mediaType The media type.
	 */
	public ByteArrayRepresentation(byte[] byteArray, MediaType mediaType) {
		super(new ByteArrayInputStream(byteArray), mediaType);
	}

	/**
	 * 
	 * @param byteArray    The byte array to wrap.
	 * @param mediaType    The media type.
	 * @param expectedSize
	 */
	public ByteArrayRepresentation(byte[] byteArray, MediaType mediaType, long expectedSize) {
		super(new ByteArrayInputStream(byteArray), mediaType, expectedSize);
	}

}
