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


/**
 * Media type used in representations and preferences.
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class MediaType extends Metadata
{
	public static final MediaType ALL = new MediaType(
			"*/*", "All media");

	public static final MediaType APPLICATION_ALL = new MediaType(
			"application/*", "All application documents");

	public static final MediaType APPLICATION_ATOM_XML = new MediaType(
			"application/atom+xml", "Atom syndication documents");

	public static final MediaType APPLICATION_CABINET = new MediaType(
			"application/vnd.ms-cab-compressed", "Microsoft Cabinet archive");

	public static final MediaType APPLICATION_EXCEL = new MediaType(
			"application/vnd.ms-excel", "Microsoft Excel document");

	public static final MediaType APPLICATION_GNU_TAR = new MediaType(
			"application/x-gtar", "GNU Tar archive");

	public static final MediaType APPLICATION_GNU_ZIP = new MediaType(
			"application/x-gzip", "GNU Zip archive");

	public static final MediaType APPLICATION_HTTP_COOKIES = new MediaType(
			"application/x-http-cookies", "HTTP cookies");

	public static final MediaType APPLICATION_JAVA_ARCHIVE = new MediaType(
			"application/java-archive", "Java archive");

	public static final MediaType APPLICATION_JAVA_OBJECT = new MediaType(
			"application/x-java-serialized-object", "Java serialized object");

	public static final MediaType APPLICATION_JAVASCRIPT = new MediaType(
			"application/x-javascript", "Javascript document");

	public static final MediaType APPLICATION_OCTET_STREAM = new MediaType(
			"application/octet-stream", "Raw octet stream");

	public static final MediaType APPLICATION_PDF = new MediaType(
			"application/pdf", "Adobe PDF document");

	public static final MediaType APPLICATION_POSTSCRIPT = new MediaType(
			"application/postscript", "Postscript document");

	public static final MediaType APPLICATION_POWERPOINT = new MediaType(
			"application/vnd.ms-powerpoint", "Microsoft Powerpoint document");

	public static final MediaType APPLICATION_PROJECT = new MediaType(
			"application/vnd.ms-project", "Microsoft Project document");

	public static final MediaType APPLICATION_RESOURCE_DESCRIPTION_FRAMEWORK = new MediaType(
			"application/rdf-xml", "Resource Description Framework document");

	public static final MediaType APPLICATION_RICH_TEXT_FORMAT = new MediaType(
			"application/rtf", "Rich Text Format document");

	public static final MediaType APPLICATION_SHOCKWAVE_FLASH = new MediaType(
			"application/x-shockwave-flash", "Shockwave Flash object");

	public static final MediaType APPLICATION_STUFFIT = new MediaType(
			"application/x-stuffit", "Stuffit archive");

	public static final MediaType APPLICATION_TAR = new MediaType(
			"application/x-tar",	"Tar archive");

	public static final MediaType APPLICATION_WORD = new MediaType(
			"application/msword", "Microsoft Word document");

	public static final MediaType APPLICATION_WWW_FORM = new MediaType(
			"application/x-www-form-urlencoded", "Web form (URL encoded)");

	public static final MediaType APPLICATION_XHTML_XML = new MediaType(
			"application/xhtml+xml", "XHTML document");

	public static final MediaType APPLICATION_XML = new MediaType(
			"application/xml", "XML document");

	public static final MediaType APPLICATION_ZIP = new MediaType(
			"application/zip", "Zip archive");

	public static final MediaType AUDIO_ALL = new MediaType(
			"audio/*", "All audios");

	public static final MediaType AUDIO_MPEG = new MediaType(
			"audio/mpeg", "MPEG audio (MP3)");

	public static final MediaType AUDIO_REAL = new MediaType(
			"audio/x-pn-realaudio",	"Real audio");

	public static final MediaType AUDIO_WAV = new MediaType(
			"audio/x-wav",	"Waveform audio");

	public static final MediaType IMAGE_ALL = new MediaType(
			"image/*", "All images");

	public static final MediaType IMAGE_BMP = new MediaType(
			"image/bmp", "Windows bitmap");

	public static final MediaType IMAGE_GIF = new MediaType(
			"image/gif", "GIF image");

	public static final MediaType IMAGE_ICON = new MediaType(
			"image/x-icon", "Windows icon (Favicon)");

	public static final MediaType IMAGE_JPEG = new MediaType(
			"image/jpeg", "JPEG image");

	public static final MediaType IMAGE_PNG = new MediaType(
			"image/png", "PNG image");

	public static final MediaType IMAGE_SVG = new MediaType(
			"image/svg+xml", "Scalable Vector Graphics");

	public static final MediaType MESSAGE_ALL = new MediaType(
			"message/*", "All messages");

	public static final MediaType MODEL_ALL = new MediaType(
			"model/*", "All models");

	public static final MediaType MULTIPART_ALL = new MediaType(
			"multipart/*",	"All multipart data");

	public static final MediaType MULTIPART_FORM_DATA = new MediaType(
			"multipart/form-data", "Multipart form data");

	public static final MediaType TEXT_ALL = new MediaType(
			"text/*", "All texts");

	public static final MediaType TEXT_CSS = new MediaType(
			"text/css", "CSS stylesheet");

	public static final MediaType TEXT_HTML = new MediaType(
			"text/html", "HTML document");

	public static final MediaType TEXT_PLAIN = new MediaType(
			"text/plain", "Plain text");

	public static final MediaType TEXT_URI_LIST = new MediaType(
			"text/uri-list", "List of URIs");

	public static final MediaType TEXT_VCARD = new MediaType(
			"text/x-vcard", "vCard");

	public static final MediaType TEXT_XML = new MediaType(
			"text/xml", "XML text");

	public static final MediaType VIDEO_ALL = new MediaType(
			"video/*", "All videos");

	public static final MediaType VIDEO_AVI = new MediaType(
			"video/x-msvideo", "AVI video");

	public static final MediaType VIDEO_MPEG = new MediaType(
			"video/mpeg", "MPEG video");

	public static final MediaType VIDEO_QUICKTIME = new MediaType(
			"video/quicktime", "Quicktime video");

	public static final MediaType VIDEO_WMV = new MediaType(
			"video/x-ms-wmv", "Windows movie");

	/** The list of parameters. */
	private ParameterList parameters;

	/**
	 * Constructor.
	 * @param name The name.
	 */
	public MediaType(String name)
	{
		this(name, null, "Media type or range of media types");
	}

	/**
	 * Constructor.
	 * @param name The name.
	 * @param description The description. 
	 */
	public MediaType(String name, String description)
	{
		this(name, null, description);
	}

	/**
	 * Constructor.
	 * @param name The name.
	 * @param parameters The list of parameters.
	 */
	public MediaType(String name, ParameterList parameters)
	{
		this(name, parameters, "Media type or range of media types");
	}

	/**
	 * Constructor.
	 * @param name The name.
	 * @param parameters The list of parameters.
	 * @param description The description.
	 */
	public MediaType(String name, ParameterList parameters, String description)
	{
		super((name == null) ? null : name, description);
		this.parameters = parameters;
	}

	/**
	 * Returns the main type.
	 * @return The main type.
	 */
	public String getMainType()
	{
		String result = null;

		if (getName() != null)
		{
			int index = getName().indexOf('/');

			// Some clients appear to use name types without subtypes
			if (index == -1)
			{
				index = getName().indexOf(';');
			}

			if (index == -1)
			{
				result = getName();
			}
			else
			{
				result = getName().substring(0, index);
			}
		}

		return result;
	}

	/**
	 * Returns the sub-type.
	 * @return The sub-type.
	 */
	public String getSubType()
	{
		String result = null;

		if (getName() != null)
		{
			int slash = getName().indexOf('/');

			if (slash == -1)
			{
				// No subtype found, assume that all subtypes are accepted
				result = "*";
			}
			else
			{
				int separator = getName().indexOf(';');
				if (separator == -1)
				{
					result = getName().substring(slash + 1);
				}
				else
				{
					result = getName().substring(slash + 1, separator);
				}
			}
		}

		return result;
	}

	/**
	 * Returns the list of parameters.
	 * @return The list of parameters.
	 */
	public ParameterList getParameters()
	{
   	if(this.parameters == null) this.parameters = new ParameterList();
		return this.parameters;
	}

	/**
	 * Indicates if a given media type is included in the current one.
	 * The test is true if both types are equal or if the given media type is within the range of the 
	 * current one. For example, @link{ALL} includes all media types. 
	 * Parameters are ignored for this comparison. 
	 * @param included The media type to test for inclusion.
	 * @return True if the given media type is included in the current one.
	 */
	public boolean includes(MediaType included)
	{
		boolean result = equals(ALL);

		if (result)
		{
			// The ALL media type includes all other types.
		}
		else
		{
			result = equals(included);

			if (result)
			{
				// Both media types are equal
			}
			else
			{
				result = getMainType().equals(included.getMainType())
						&& getSubType().equals("*");

				if (result)
				{
					// Both media types have the same main type
					// and the subtype of current media type includes all subtypes. 
				}
				else
				{
					// Both media types are not equal
				}
			}
		}

		return result;
	}

}
