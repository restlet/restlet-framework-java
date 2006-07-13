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

package org.restlet.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

/**
 * Current or intended state of a resource. For performance purpose, it is essential that a minimal overhead 
 * occurs upon initialization. The main overhead must only occur during invocation of content processing 
 * methods (write, getStream, getChannel and toString).Current or intended state of a resource.<br/><br/> 
 * "REST components perform actions on a resource by using a representation to capture the current or intended 
 * state of that resource and transferring that representation between components. A representation is a 
 * sequence of bytes, plus representation metadata to describe those bytes. Other commonly used but less 
 * precise names for a representation include: document, file, and HTTP message entity, instance, or variant." 
 * Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2">Source dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Representation extends Resource
{
	/**
	 * Inidicates that the size of the representation can't be known in advance. 
	 */
	public static final long UNKNOWN_SIZE = -1L;

   /**
    * Returns the character set or null if not applicable.
    * @return The character set or null if not applicable.
    */
   public CharacterSet getCharacterSet();
   
   /**
    * Sets the character set or null if not applicable.
    * @param characterSet The character set or null if not applicable.
    */
   public void setCharacterSet(CharacterSet characterSet);

   /**
    * Indicates if some fresh content is available, without having to actually call one of the content
    * manipulation method like getStream() that would actually consume it. This is especially useful for
    * transient representation whose content can only be accessed once. 
    * @return True if some fresh content is available.
    */
   public boolean isContentAvailable();
   
   /**
    * Indicates if the representation's content is transient, which means that it can 
    * be obtained only once. This is often the case with representations transmitted
    * via network sockets for example. In such case, if you need to read the content 
    * several times, you need to cache it first, for example into memory or into a file.   
    * @return True if the representation's content is transient.
    */
	public boolean isContentTransient();
   
   /**
    * Returns the encoding or null if identity encoding applies.
    * @return The encoding or null if identity encoding applies.
    */
   public Encoding getEncoding();
   
   /**
    * Sets the encoding or null if identity encoding applies.
    * @param encoding The encoding or null if identity encoding applies.
    */
   public void setEncoding(Encoding encoding);
   
   /**
    * Returns the future date when this representation expire. If this information is not known, returns null.
    * @return The expiration date.
    */
   public Date getExpirationDate();
   
   /**
    * Sets the future date when this representation expire. If this information is not known, pass null.
    * @param expirationDate The expiration date.
    */
   public void setExpirationDate(Date expirationDate);
   
   /**
    * Returns the language or null if not applicable.
    * @return The language or null if not applicable.
    */
   public Language getLanguage();
   
   /**
    * Sets the language or null if not applicable.
    * @param language The language or null if not applicable.
    */
   public void setLanguage(Language language);
   
   /**
    * Returns the media type.
    * @return The media type.
    */
   public MediaType getMediaType();
   
   /**
    * Sets the media type.
    * @param mediaType The media type.
    */
   public void setMediaType(MediaType mediaType);
   
   /**
    * Returns the last date when this representation was modified. If this information is not known, returns
    * null.
    * @return The modification date.
    */
   public Date getModificationDate();
   
   /**
    * Sets the last date when this representation was modified. If this information is not known, pass null.
    * @param modificationDate The modification date.
    */
   public void setModificationDate(Date modificationDate);

	/**
    * Returns the represented resource if available.
    * @return The represented resource if available.
    */
   public Resource getResource();

   /**
    * Sets the represented resource.
    * @param resource The represented resource.
    */
   public void setResource(Resource resource);
	
   /**
    * Returns the size in bytes if known, -1 otherwise.
    * @return The size in bytes if known, -1 otherwise.
    */
   public long getSize();
	
   /**
    * Sets the expected size in bytes if known, -1 otherwise.
    * @param expectedSize The expected size in bytes if known, -1 otherwise.
    */
   public void setSize(long expectedSize);

   /**
    * Returns the tag.
    * @return The tag.
    */
   public Tag getTag();
   
   /**
    * Sets the tag.
    * @param tag The tag.
    */
   public void setTag(Tag tag);
   
   /**
    * Returns a channel with the representation's content.<br/>
    * If it is supported by a file, a read-only instance of FileChannel is returned.<br/>
    * This method is ensured to return a fresh channel for each invocation unless it 
    * is a transient representation, in which case null is returned.
    * @return A channel with the representation's content.
    * @throws IOException
    */
   public ReadableByteChannel getChannel() throws IOException;

   /**
    * Returns a stream with the representation's content.
    * This method is ensured to return a fresh stream for each invocation unless it 
    * is a transient representation, in which case null is returned.
    * @return A stream with the representation's content.
    * @throws IOException
    */
   public InputStream getStream() throws IOException;

   /**
    * Writes the representation to a byte channel.
    * This method is ensured to write the full content for each invocation unless it 
    * is a transient representation, in which case an exception is thrown.
    * @param writableChannel A writable byte channel.
    * @throws IOException
    */
   public void write(WritableByteChannel writableChannel) throws IOException;

   /**
    * Writes the representation to a byte stream.
    * This method is ensured to write the full content for each invocation unless it 
    * is a transient representation, in which case an exception is thrown.
    * @param outputStream The output stream.
    * @throws IOException
    */
   public void write(OutputStream outputStream) throws IOException;

   /**
    * Converts the representation to a string.
    * This method is ensured to return a fresh string for each invocation unless it 
    * is a transient representation, in which case null is returned.
    * @return The representation as a string.
    */
   public String toString();
}
