/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet.ext.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.restlet.data.MediaType;

/**
 * Representation based on a serializable Java object.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ObjectRepresentation extends OutputRepresentation
{
	/** The serializable object. */
	private Serializable object;

	/**
	 * Constructor;
	 * @param object The serializable object.
	 */
	public ObjectRepresentation(Serializable object)
	{
		super(MediaType.APPLICATION_JAVA_OBJECT);
		this.object = object;
	}

	/**
	 * Writes the datum as a stream of bytes.
	 * @param outputStream The stream to use when writing.
	 */
	public void write(OutputStream outputStream) throws IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream(outputStream);
		oos.writeObject(getObject());
		oos.close();
	}

	/**
	 * Returns the represented object.
	 * @return The represented object.
	 * @throws IOException
	 */
	public Object getObject() throws IOException
	{
		return this.object;
	}

}
