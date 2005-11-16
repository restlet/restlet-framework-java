package org.restlet.data;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.RestletException;

/**
 * Current or intended state of a resource.
 * For performance purpose, it is essential that a minimal overhead occurs upon initialization.
 * Most overhead should occurs during invocation of content processing methods (write, getStream, getChannel and toString)<br/><br/>
 * "REST components perform actions on a resource by using a representation to capture the current or intended state of that
 * resource and transferring that representation between components. A representation is a sequence of bytes, plus
 * representation metadata to describe those bytes. Other commonly used but less precise names for a representation include:
 * document, file, and HTTP message entity, instance, or variant." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2">Source dissertation</a>
 */
public interface Representation extends Data
{
   /**
    * Returns the metadata.
    * @return The metadata.
    */
   public RepresentationMetadata getMetadata();
   
   /**
    * Returns a readable byte channel.
    * If it is supported by a file a read-only instance of FileChannel is returned.
    * @return A readable byte channel.
    */
   public ReadableByteChannel getChannel() throws RestletException;

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    */
   public InputStream getStream() throws RestletException;

   /**
    * Writes the representation to a byte channel.
    * @param writableChannel A writable byte channel.
    */
   public void write(WritableByteChannel writableChannel) throws RestletException;

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
   public void write(OutputStream outputStream) throws RestletException;

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString();
}
