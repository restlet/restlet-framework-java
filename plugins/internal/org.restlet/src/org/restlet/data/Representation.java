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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import org.restlet.Resource;

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
public abstract class Representation extends Resource
{
	/**
	 * Inidicates that the size of the representation can't be known in advance. 
	 */
	public static final long UNKNOWN_SIZE = -1L;

   /** The character set or null if not applicable. */
	private CharacterSet characterSet;
   
   /** Indicates if the representation's content is available. */
	private boolean contentAvailable;
	
   /** Indicates if the representation's content is transient. */
	private boolean contentTransient;

   /** The encoding or null if not identity encoding applies. */
	private Encoding encoding;

	/** 
	 * The expected size. Dynamic representations can have any size, but sometimes we can know in 
	 * advance the expected size. If this expected size is specified by the user, it has a higher priority
	 * than any size that can be guessed by the representation (like a file size).
	 */
	private long size;
   
   /** The expiration date. */
	private Date expirationDate;
   
   /** The language or null if not applicable. */
	private Language language;

   /** The media type. */
	private MediaType mediaType;

   /** The modification date. */
	private Date modificationDate;

	/** The represented resource, if available. */
	private Resource resource;

   /** The tag. */
	private Tag tag;
   
   /**
    * Constructor.
    * @param mediaType The media type.
    */
   public Representation(MediaType mediaType)
   {
      this.characterSet = null;
      this.contentAvailable = true;
      this.contentTransient = false;
      this.encoding = null;
      this.size = UNKNOWN_SIZE;
      this.expirationDate = null;
      this.language = null;
      this.mediaType = mediaType;
      this.modificationDate = null;
      this.resource = null;
      this.tag = null;
      
      // A representation is also a resource whose only 
      // variant is the representation itself
      getVariants().add(this);
   }

   /**
    * Returns the character set or null if not applicable.
    * @return The character set or null if not applicable.
    */
   public CharacterSet getCharacterSet()
   {
      return characterSet;
   }

   /**
    * Sets the character set or null if not applicable.
    * @param characterSet The character set or null if not applicable.
    */
   public void setCharacterSet(CharacterSet characterSet)
   {
      this.characterSet = characterSet;
   }

   /**
    * Indicates if some fresh content is available, without having to actually call one of the content
    * manipulation method like getStream() that would actually consume it. This is especially useful for
    * transient representation whose content can only be accessed once. 
    * @return True if some fresh content is available.
    */
   public boolean isAvailable()
   {
   	return this.contentAvailable;
   }

   /**
    * Indicates if the representation's content is transient, which means that it can 
    * be obtained only once. This is often the case with representations transmitted
    * via network sockets for example. In such case, if you need to read the content 
    * several times, you need to cache it first, for example into memory or into a file.   
    * @return True if the representation's content is transient.
    */
	public boolean isTransient()
	{
		return this.contentTransient;
	}

   /**
    * Returns the encoding or null if identity encoding applies.
    * @return The encoding or null if identity encoding applies.
    */
   public Encoding getEncoding()
   {
      return this.encoding;
   }

   /**
    * Sets the encoding or null if identity encoding applies.
    * @param encoding The encoding or null if identity encoding applies.
    */
   public void setEncoding(Encoding encoding)
   {
      this.encoding = encoding;
   }

   /**
    * Returns the future date when this representation expire. If this information is not known, returns null.
    * @return The expiration date.
    */
   public Date getExpirationDate()
   {
      return expirationDate;
   }

   /**
    * Sets the future date when this representation expire. If this information is not known, pass null.
    * @param expirationDate The expiration date.
    */
   public void setExpirationDate(Date expirationDate)
   {
      this.expirationDate = expirationDate;
   }

   /**
    * Returns the language or null if not applicable.
    * @return The language or null if not applicable.
    */
   public Language getLanguage()
   {
      return language;
   }

   /**
    * Sets the language or null if not applicable.
    * @param language The language or null if not applicable.
    */
   public void setLanguage(Language language)
   {
      this.language = language;
   }

   /**
    * Returns the media type.
    * @return The media type.
    */
   public MediaType getMediaType()
   {
      return mediaType;
   }

   /**
    * Sets the media type.
    * @param mediaType The media type.
    */
   public void setMediaType(MediaType mediaType)
   {
      this.mediaType = mediaType;
   }

   /**
    * Returns the last date when this representation was modified. If this information is not known, returns
    * null.
    * @return The modification date.
    */
   public Date getModificationDate()
   {
      return modificationDate;
   }

   /**
    * Sets the last date when this representation was modified. If this information is not known, pass null.
    * @param modificationDate The modification date.
    */
   public void setModificationDate(Date modificationDate)
   {
      this.modificationDate = modificationDate;
   }

   /**
    * Returns the represented resource if available.
    * @return The represented resource if available.
    */
   public Resource getResource()
   {
      return this.resource;
   }

   /**
    * Sets the represented resource.
    * @param resource The represented resource.
    */
   public void setResource(Resource resource)
   {
      this.resource = resource;
   }
   
   /**
    * Returns the size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
    * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
    */
   public long getSize()
   {
      return this.size;
   }

   /**
    * Sets the expected size in bytes if known, -1 otherwise.
    * @param expectedSize The expected size in bytes if known, -1 otherwise.
    */
   public void setSize(long expectedSize)
   {
      this.size = expectedSize;
   }

   /**
    * Returns the tag.
    * @return The tag.
    */
   public Tag getTag()
   {
      return tag;
   }

   /**
    * Sets the tag.
    * @param tag The tag.
    */
   public void setTag(Tag tag)
   {
      this.tag = tag;
   }

	/**
	 * Indicates if some fresh content is available.
	 * @param contentAvailable True if some fresh content is available.
	 */
	protected void setAvailable(boolean contentAvailable)
	{
		this.contentAvailable = contentAvailable;
	}

	/**
	 * Indicates if the representation's content is transient.
	 * @param contentTransient True if the representation's content is transient.
	 */
	protected void setTransient(boolean contentTransient)
	{
		this.contentTransient = contentTransient;
	}

   /**
    * Returns a channel with the representation's content.<br/>
    * If it is supported by a file, a read-only instance of FileChannel is returned.<br/>
    * This method is ensured to return a fresh channel for each invocation unless it 
    * is a transient representation, in which case null is returned.
    * @return A channel with the representation's content.
    * @throws IOException
    */
   public abstract ReadableByteChannel getChannel() throws IOException;

   /**
    * Returns a stream with the representation's content.
    * This method is ensured to return a fresh stream for each invocation unless it 
    * is a transient representation, in which case null is returned.
    * @return A stream with the representation's content.
    * @throws IOException
    */
   public abstract InputStream getStream() throws IOException;

   /**
    * Writes the representation to a byte channel.
    * This method is ensured to write the full content for each invocation unless it 
    * is a transient representation, in which case an exception is thrown.
    * @param writableChannel A writable byte channel.
    * @throws IOException
    */
   public abstract void write(WritableByteChannel writableChannel) throws IOException;

   /**
    * Writes the representation to a byte stream.
    * This method is ensured to write the full content for each invocation unless it 
    * is a transient representation, in which case an exception is thrown.
    * @param outputStream The output stream.
    * @throws IOException
    */
   public abstract void write(OutputStream outputStream) throws IOException;

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString()
   {
      String result = null;

      if(isAvailable())
      {
	      try
	      {
	         ByteArrayOutputStream baos = new ByteArrayOutputStream();
	         write(baos);
	         
	         if(getCharacterSet() != null)
	         {
	         	result = baos.toString(getCharacterSet().getName());
	         }
	         else
	         {
	         	result = baos.toString();
	         }
	      }
	      catch(Exception ioe)
	      {
	      }
      }
      
      return result;
   }
}
