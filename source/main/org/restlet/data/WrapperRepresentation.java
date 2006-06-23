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
 * Representation wrapper. Useful for application developer who need to enrich the representation 
 * with application related properties and behavior.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperRepresentation extends WrapperResource implements Representation
{
   /**
    * Constructor.
    * @param wrappedRepresentation The wrapped representation.
    */
   public WrapperRepresentation(Representation wrappedRepresentation)
   {
      super(wrappedRepresentation);
   }

   /**
    * Returns the wrapped Resource.
    * @return The wrapped Resource.
    */
   public Representation getWrappedRepresentation()
   {
   	return (Representation)getWrappedResource();
   }

   /**
    * Returns the character set or null if not applicable.
    * @return The character set or null if not applicable.
    */
   public CharacterSet getCharacterSet()
   {
   	return getWrappedRepresentation().getCharacterSet();
   }
   
   /**
    * Sets the character set or null if not applicable.
    * @param characterSet The character set or null if not applicable.
    */
   public void setCharacterSet(CharacterSet characterSet)
   {
   	getWrappedRepresentation().setCharacterSet(characterSet);
   }

   /**
    * Indicates if some fresh content is available, without having to actually call one of the content
    * manipulation method like getStream() that would actually consume it. This is especially useful for
    * transient representation whose content can only be accessed once. 
    * @return True if some fresh content is available.
    */
   public boolean isContentAvailable()
   {
   	return getWrappedRepresentation().isContentAvailable();
   }
   
   /**
    * Indicates if the representation's content is transient, which means that it can 
    * be obtained only once. This is often the case with representations transmitted
    * via network sockets for example. In such case, if you need to read the content 
    * several times, you need to cache it first, for example into memory or into a file.   
    * @return True if the representation's content is transient.
    */
	public boolean isContentTransient()
	{
		return getWrappedRepresentation().isContentTransient();
	}
   
   /**
    * Returns the encoding or null if identity encoding applies.
    * @return The encoding or null if identity encoding applies.
    */
   public Encoding getEncoding()
   {
   	return getWrappedRepresentation().getEncoding();
   }
   
   /**
    * Sets the encoding or null if identity encoding applies.
    * @param encoding The encoding or null if identity encoding applies.
    */
   public void setEncoding(Encoding encoding)
   {
   	getWrappedRepresentation().setEncoding(encoding);
   }
   
   /**
    * Returns the future date when this representation expire. If this information is not known, returns null.
    * @return The expiration date.
    */
   public Date getExpirationDate()
   {
   	return getWrappedRepresentation().getExpirationDate();
   }
   
   /**
    * Sets the future date when this representation expire. If this information is not known, pass null.
    * @param expirationDate The expiration date.
    */
   public void setExpirationDate(Date expirationDate)
   {
   	getWrappedRepresentation().setExpirationDate(expirationDate);
   }
   
   /**
    * Returns the language or null if not applicable.
    * @return The language or null if not applicable.
    */
   public Language getLanguage()
   {
   	return getWrappedRepresentation().getLanguage();
   }
   
   /**
    * Sets the language or null if not applicable.
    * @param language The language or null if not applicable.
    */
   public void setLanguage(Language language)
   {
   	getWrappedRepresentation().setLanguage(language);
   }
   
   /**
    * Returns the media type.
    * @return The media type.
    */
   public MediaType getMediaType()
   {
   	return getWrappedRepresentation().getMediaType();
   }
   
   /**
    * Sets the media type.
    * @param mediaType The media type.
    */
   public void setMediaType(MediaType mediaType)
   {
   	getWrappedRepresentation().setMediaType(mediaType);
   }
   
   /**
    * Returns the last date when this representation was modified. If this information is not known, returns
    * null.
    * @return The modification date.
    */
   public Date getModificationDate()
   {
   	return getWrappedRepresentation().getModificationDate();
   }
   
   /**
    * Sets the last date when this representation was modified. If this information is not known, pass null.
    * @param modificationDate The modification date.
    */
   public void setModificationDate(Date modificationDate)
   {
   	getWrappedRepresentation().setModificationDate(modificationDate);
   }

	/**
    * Returns the represented resource if available.
    * @return The represented resource if available.
    */
   public Resource getResource()
   {
   	return getWrappedRepresentation().getResource();
   }

   /**
    * Sets the represented resource.
    * @param resource The represented resource.
    */
   public void setResource(Resource resource)
   {
   	getWrappedRepresentation().setResource(resource);
   }
	
   /**
    * Returns the size in bytes if known, -1 otherwise.
    * @return The size in bytes if known, -1 otherwise.
    */
   public long getSize()
   {
   	return getWrappedRepresentation().getSize();
   }
	
   /**
    * Sets the expected size in bytes if known, -1 otherwise.
    * @param expectedSize The expected size in bytes if known, -1 otherwise.
    */
   public void setSize(long expectedSize)
   {
   	getWrappedRepresentation().setSize(expectedSize);
   }

   /**
    * Returns the tag.
    * @return The tag.
    */
   public Tag getTag()
   {
   	return getWrappedRepresentation().getTag();
   }
   
   /**
    * Sets the tag.
    * @param tag The tag.
    */
   public void setTag(Tag tag)
   {
   	getWrappedRepresentation().setTag(tag);
   }

   /**
    * Returns a channel with the representation's content.<br/>
    * If it is supported by a file, a read-only instance of FileChannel is returned.
    * @return A channel with the representation's content.
    * @throws IOException
    */
   public ReadableByteChannel getChannel() throws IOException
   {
   	return getWrappedRepresentation().getChannel();
   }

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    * @throws IOException
    */
   public InputStream getStream() throws IOException
   {
   	return getWrappedRepresentation().getStream();
   }

   /**
    * Writes the representation to a byte channel.
    * @param writableChannel A writable byte channel.
    * @throws IOException
    */
   public void write(WritableByteChannel writableChannel) throws IOException
   {
   	getWrappedRepresentation().write(writableChannel);
   }

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    * @throws IOException
    */
   public void write(OutputStream outputStream) throws IOException
   {
   	getWrappedRepresentation().write(outputStream);
   }

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString()
   {
   	return getWrappedRepresentation().toString();
   }

}
