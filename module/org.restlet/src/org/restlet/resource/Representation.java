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

package org.restlet.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.logging.Logger;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.util.ImmutableDate;

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
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Representation extends Resource
{
	/** Inidicates that the size of the representation can't be known in advance. */
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
	private ImmutableDate expirationDate;

	/** The language or null if not applicable. */
	private Language language;

	/** The media type. */
	private MediaType mediaType;

	/** The modification date. */
	private ImmutableDate modificationDate;

	/** The represented resource, if available. */
	private Resource resource;

	/** The tag. */
	private Tag tag;

	/**
	 * Default constructor.
	 */
	public Representation()
	{
		this(null);
	}

	/**
	 * Constructor.
	 * @param mediaType The media type.
	 */
	public Representation(MediaType mediaType)
	{
		super((Logger) null);
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
		if((getVariants() != null) && !getVariants().contains(this)) 
		{
			getVariants().add(this);
		}
	}

	/**
	 * Returns a channel with the representation's content.<br/>
	 * If it is supported by a file, a read-only instance of FileChannel is returned.<br/>
	 * This method is ensured to return a fresh channel for each invocation unless it 
	 * is a transient representation, in which case null is returned.
	 * @return A channel with the representation's content.
	 * @throws IOException
	 */
	public ReadableByteChannel getChannel() throws IOException
	{
		return null;
	}

	/**
	 * Returns the character set or null if not applicable.
	 * @return The character set or null if not applicable.
	 */
	public CharacterSet getCharacterSet()
	{
		return this.characterSet;
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
	 * Returns the future date when this representation expire. If this information is not known, returns null.
	 * @return The expiration date.
	 */
	public Date getExpirationDate()
	{
		return this.expirationDate;
	}

	/**
	 * Returns the language or null if not applicable.
	 * @return The language or null if not applicable.
	 */
	public Language getLanguage()
	{
		return this.language;
	}

	/**
	 * Returns the media type.
	 * @return The media type.
	 */
	public MediaType getMediaType()
	{
		return this.mediaType;
	}

	/**
	 * Returns the last date when this representation was modified. If this information is not known, returns
	 * null.
	 * @return The modification date.
	 */
	public Date getModificationDate()
	{
		return this.modificationDate;
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
	 * Returns the size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
	 * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
	 */
	public long getSize()
	{
		return this.size;
	}

	/**
	 * Returns a stream with the representation's content.
	 * This method is ensured to return a fresh stream for each invocation unless it 
	 * is a transient representation, in which case null is returned.
	 * @return A stream with the representation's content.
	 * @throws IOException
	 */
	public InputStream getStream() throws IOException
	{
		return null;
	}

	/**
	 * Returns the tag.
	 * @return The tag.
	 */
	public Tag getTag()
	{
		return this.tag;
	}

	/**
	 * Converts the representation to a string value. Be careful when using this method as the conversion of 
	 * large content to a string fully stored in memory can result in OutOfMemoryErrors being thrown.
	 * @return The representation as a string value.
	 */
	public String getValue() throws IOException
	{
		String result = null;

		if (isAvailable())
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			write(baos);

			if (getCharacterSet() != null)
			{
				result = baos.toString(getCharacterSet().getName());
			}
			else
			{
				result = baos.toString();
			}
		}

		return result;
	}

	/**
	 * Indicates if some fresh content is available, without having to actually call one of the content
	 * manipulation method like getStream() that would actually consume it. This is especially useful for
	 * transient representation whose content can only be accessed once and also when the size of the 
	 * representation is not known in advance. 
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
	 * Indicates if some fresh content is available.
	 * @param available True if some fresh content is available.
	 */
	public void setAvailable(boolean available)
	{
		this.contentAvailable = available;
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
	 * Sets the encoding or null if identity encoding applies.
	 * @param encoding The encoding or null if identity encoding applies.
	 */
	public void setEncoding(Encoding encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * Sets the future date when this representation expire. If this information is not known, pass null.
	 * @param expirationDate The expiration date.
	 */
	public void setExpirationDate(Date expirationDate)
	{
		this.expirationDate = ImmutableDate.valueOf(expirationDate);
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
	 * Sets the media type.
	 * @param mediaType The media type.
	 */
	public void setMediaType(MediaType mediaType)
	{
		this.mediaType = mediaType;
	}

	/**
	 * Sets the last date when this representation was modified. If this information is not known, pass null.
	 * @param modificationDate The modification date.
	 */
	public void setModificationDate(Date modificationDate)
	{
		this.modificationDate = ImmutableDate.valueOf(modificationDate);
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
	 * Sets the expected size in bytes if known, -1 otherwise.
	 * @param expectedSize The expected size in bytes if known, -1 otherwise.
	 */
	public void setSize(long expectedSize)
	{
		this.size = expectedSize;
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
	 * Indicates if the representation's content is transient.
	 * @param isTransient True if the representation's content is transient.
	 */
	public void setTransient(boolean isTransient)
	{
		this.contentTransient = isTransient;
	}

	/**
	 * Writes the representation to a byte stream.
	 * This method is ensured to write the full content for each invocation unless it 
	 * is a transient representation, in which case an exception is thrown.
	 * @param outputStream The output stream.
	 * @throws IOException
	 */
	public void write(OutputStream outputStream) throws IOException
	{
		throw new UnsupportedOperationException(
				"You must override this method in order to use it");
	}

	/**
	 * Writes the representation to a byte channel.
	 * This method is ensured to write the full content for each invocation unless it 
	 * is a transient representation, in which case an exception is thrown.
	 * @param writableChannel A writable byte channel.
	 * @throws IOException
	 */
	public void write(WritableByteChannel writableChannel) throws IOException
	{
		throw new UnsupportedOperationException(
				"You must override this method in order to use it");
	}

}
