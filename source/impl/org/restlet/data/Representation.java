/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.restlet.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Current or intended state of a resource. For performance purpose, it is essential that a minimal overhead
 * occurs upon initialization. Most overhead should occurs during invocation of content processing methods
 * (write, getStream, getChannel and toString)<br/><br/> "REST components perform actions on a resource by
 * using a representation to capture the current or intended state of that resource and transferring that
 * representation between components. A representation is a sequence of bytes, plus representation metadata to
 * describe those bytes. Other commonly used but less precise names for a representation include: document,
 * file, and HTTP message entity, instance, or variant." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2">Source
 * dissertation</a>
 */
public interface Representation extends Data
{
   /**
    * Returns the metadata.
    * @return The metadata.
    */
   public RepresentationMetadata getMetadata();

   /**
    * Returns a readable byte channel. If it is supported by a file a read-only instance of FileChannel is
    * returned.
    * @return A readable byte channel.
    * @throws IOException
    */
   public ReadableByteChannel getChannel() throws IOException;

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    * @throws IOException
    */
   public InputStream getStream() throws IOException;

   /**
    * Writes the representation to a byte channel.
    * @param writableChannel A writable byte channel.
    * @throws IOException
    */
   public void write(WritableByteChannel writableChannel) throws IOException;

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    * @throws IOException
    */
   public void write(OutputStream outputStream) throws IOException;

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString();
}
