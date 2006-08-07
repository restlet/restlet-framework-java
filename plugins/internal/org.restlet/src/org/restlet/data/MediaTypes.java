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

import java.util.List;

/**
 * The data format of a representation.
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public enum MediaTypes implements MediaType
{
   ALL,

   APPLICATION_ALL, APPLICATION_ATOM_XML, APPLICATION_CABINET, APPLICATION_EXCEL, APPLICATION_GNU_TAR, 
   APPLICATION_GNU_ZIP, APPLICATION_HTTP_COOKIES, APPLICATION_JAVA_ARCHIVE, APPLICATION_JAVA_OBJECT, 
   APPLICATION_JAVASCRIPT, APPLICATION_OCTET_STREAM, APPLICATION_PDF, APPLICATION_POSTSCRIPT, 
   APPLICATION_POWERPOINT, APPLICATION_PROJECT, APPLICATION_RESOURCE_DESCRIPTION_FRAMEWORK, 
   APPLICATION_RICH_TEXT_FORMAT, APPLICATION_SHOCKWAVE_FLASH, APPLICATION_STUFFIT, APPLICATION_TAR, 
   APPLICATION_WORD, APPLICATION_WWW_FORM, APPLICATION_XHTML_XML, APPLICATION_XML, APPLICATION_ZIP,
   
   AUDIO_ALL, AUDIO_MPEG, AUDIO_REAL, AUDIO_WAV,

   IMAGE_ALL, IMAGE_BMP, IMAGE_GIF, IMAGE_ICON, IMAGE_JPEG, IMAGE_PNG, IMAGE_SVG,
   
   MESSAGE_ALL, MODEL_ALL, 
   
   MULTIPART_ALL, MULTIPART_FORM_DATA,

   TEXT_ALL, TEXT_CSS, TEXT_HTML, TEXT_PLAIN, TEXT_URI_LIST, TEXT_VCARD, TEXT_XML,
   
   VIDEO_ALL, VIDEO_MPEG, VIDEO_QUICKTIME, VIDEO_AVI, VIDEO_WMV;

   /**
    * Returns the name (ex: "text/html" or "application/x-gzip" or "application/*").
    * @return The name (ex: "text/html" or "application/x-gzip" or "application/*").
    */
   public String getName()
   {
      String result = null;

      switch(this)
      {
         case ALL:
            result = "*/*";
            break;
         case APPLICATION_ALL:
            result = "application/*";
            break;
         case APPLICATION_ATOM_XML:
            result = "application/atom+xml";
            break;
         case APPLICATION_CABINET:
         	result = "application/vnd.ms-cab-compressed";
         	break;
         case APPLICATION_EXCEL:
         	result = "application/vnd.ms-excel";
         	break;
         case APPLICATION_GNU_TAR:
         	result = "application/x-gtar";
         	break;
         case APPLICATION_GNU_ZIP:
         	result = "application/x-gzip";
         	break;
         case APPLICATION_HTTP_COOKIES:
            result = "application/x-http-cookies"; // Guessed!
            break;
         case APPLICATION_JAVA_ARCHIVE:
         	result = "application/java-archive";
         	break;
         case APPLICATION_JAVA_OBJECT:
            result = "application/x-java-serialized-object";
            break;
         case APPLICATION_JAVASCRIPT:
         	result = "application/x-javascript";
         	break;
         case APPLICATION_OCTET_STREAM:
         	result = "application/octet-stream";
         	break;
         case APPLICATION_PDF:
            result = "application/pdf";
            break;
         case APPLICATION_POSTSCRIPT:
         	result = "application/postscript";
         	break;
         case APPLICATION_POWERPOINT:
         	result = "application/vnd.ms-powerpoint";
         	break;
         case APPLICATION_PROJECT:
         	result = "application/vnd.ms-project";
         	break;
         case APPLICATION_RESOURCE_DESCRIPTION_FRAMEWORK:
         	result = "application/rdf-xml";
         	break;
         case APPLICATION_RICH_TEXT_FORMAT:
         	result = "application/rtf";
         	break;
         case APPLICATION_SHOCKWAVE_FLASH:
         	result = "application/x-shockwave-flash";
         	break;
         case APPLICATION_STUFFIT:
         	result = "application/x-stuffit";
         	break;
         case APPLICATION_TAR:
         	result = "application/x-tar";
         	break;
         case APPLICATION_WORD:
            result = "application/msword";
            break;
         case APPLICATION_WWW_FORM:
            result = "application/x-www-form-urlencoded";
            break;
         case APPLICATION_XHTML_XML:
            result = "application/xhtml+xml";
            break;
         case APPLICATION_XML:
            result = "application/xml";
            break;
         case APPLICATION_ZIP:
            result = "application/zip";
            break;
            
         case AUDIO_ALL:
            result = "audio/*";
            break;
         case AUDIO_MPEG:
            result = "audio/mpeg";
            break;
         case AUDIO_REAL:
            result = "audio/x-pn-realaudio";
            break;
         case AUDIO_WAV:
            result = "audio/x-wav";
            break;
            
         case IMAGE_ALL:
            result = "image/*";
            break;
         case IMAGE_BMP:
         	result = "image/bmp";
         	break;
         case IMAGE_GIF:
            result = "image/gif";
            break;
         case IMAGE_ICON:
            result = "image/x-icon";
            break;
         case IMAGE_JPEG:
            result = "image/jpeg";
            break;
         case IMAGE_PNG:
            result = "image/png";
            break;
         case IMAGE_SVG:
         	result = "image/svg+xml";
         	break;
            
         case MESSAGE_ALL:
            result = "message/*";
            break;
            
         case MODEL_ALL:
            result = "model/*";
            break;
            
         case MULTIPART_ALL:
            result = "multipart/*";
            break;
         case MULTIPART_FORM_DATA:
         	result = "multipart/form-data";
         	break;
         	
         case TEXT_ALL:
            result = "text/*";
            break;
         case TEXT_CSS:
            result = "text/css";
            break;
         case TEXT_HTML:
            result = "text/html";
            break;
         case TEXT_PLAIN:
            result = "text/plain";
            break;
         case TEXT_URI_LIST:
            result = "text/uri-list";
            break;
         case TEXT_VCARD:
         	result = "text/x-vcard";
         	break;
         case TEXT_XML:
            result = "text/xml";
            break;
            
         case VIDEO_ALL:
            result = "video/*";
            break;
         case VIDEO_AVI:
            result = "video/x-msvideo";
            break;
         case VIDEO_MPEG:
            result = "video/mpeg";
            break;
         case VIDEO_QUICKTIME:
            result = "video/quicktime";
            break;
         case VIDEO_WMV:
            result = "video/x-ms-wmv";
            break;
      }

      return result;
   }

   /**
    * Returns the main type.
    * @return The main type.
    */
   public String getMainType()
   {
      return getName().substring(0, getName().indexOf('/'));
   }

   /**
    * Returns the sub-type.
    * @return The sub-type.
    */
   public String getSubType()
   {
      int separator = getName().indexOf(';');

      if(separator == -1)
      {
         return getName().substring(getName().indexOf('/') + 1);
      }
      else
      {
         return getName().substring(getName().indexOf('/') + 1, separator);
      }
   }

   /**
    * Returns the description.
    * @return The description.
    */
   public String getDescription()
   {
      String result = null;

      switch(this)
      {
         case ALL:
            result = "All media";
            break;
         case APPLICATION_ALL:
            result = "All application documents";
            break;
         case APPLICATION_ATOM_XML:
            result = "Atom syndication documents";
            break;
         case APPLICATION_CABINET:
         	result = "Microsoft Cabinet archive";
         	break;
         case APPLICATION_EXCEL:
         	result = "Microsoft Excel document";
         	break;
         case APPLICATION_GNU_TAR:
         	result = "GNU Tar archive";
         	break;
         case APPLICATION_GNU_ZIP:
         	result = "GNU Zip archive";
         	break;
         case APPLICATION_HTTP_COOKIES:
            result = "HTTP cookies"; // Guessed!
            break;
         case APPLICATION_JAVA_ARCHIVE:
         	result = "Java archive";
         	break;
         case APPLICATION_JAVA_OBJECT:
            result = "Java serialized object";
            break;
         case APPLICATION_JAVASCRIPT:
         	result = "Javascript document";
         	break;
         case APPLICATION_OCTET_STREAM:
         	result = "Raw octet stream";
         	break;
         case APPLICATION_PDF:
            result = "Adobe PDF document";
            break;
         case APPLICATION_POSTSCRIPT:
         	result = "Postscript document";
         	break;
         case APPLICATION_POWERPOINT:
         	result = "Microsoft Powerpoint document";
         	break;
         case APPLICATION_PROJECT:
         	result = "Microsoft Project document";
         	break;
         case APPLICATION_RESOURCE_DESCRIPTION_FRAMEWORK:
         	result = "Resource Description Framework document";
         	break;
         case APPLICATION_RICH_TEXT_FORMAT:
         	result = "Rich Text Format document";
         	break;
         case APPLICATION_SHOCKWAVE_FLASH:
         	result = "Shockwave Flash object";
         	break;
         case APPLICATION_STUFFIT:
         	result = "Stuffit archive";
         	break;
         case APPLICATION_TAR:
         	result = "Tar archive";
         	break;
         case APPLICATION_WORD:
            result = "Microsoft Word document";
            break;
         case APPLICATION_WWW_FORM:
            result = "Web form (URL encoded)";
            break;
         case APPLICATION_XHTML_XML:
            result = "XHTML document";
            break;
         case APPLICATION_XML:
            result = "XML document";
            break;
         case APPLICATION_ZIP:
            result = "Zip archive";
            break;
            
         case AUDIO_ALL:
            result = "All audios";
            break;
         case AUDIO_MPEG:
            result = "MPEG audio (MP3)";
            break;
         case AUDIO_REAL:
            result = "Real audio";
            break;
         case AUDIO_WAV:
            result = "Waveform audio";
            break;
            
         case IMAGE_ALL:
            result = "All images";
            break;
         case IMAGE_GIF:
            result = "GIF image";
            break;
         case IMAGE_ICON:
            result = "Windows icon (Favicon)";
            break;
         case IMAGE_JPEG:
            result = "JPEG image";
            break;
         case IMAGE_PNG:
            result = "PNG image";
            break;
         case IMAGE_BMP:
         	result = "Windows bitmap";
         	break;
         case IMAGE_SVG:
         	result = "Scalable Vector Graphics";
         	break;
            
         case MESSAGE_ALL:
            result = "All messages";
            break;
            
         case MODEL_ALL:
            result = "All models";
            break;
            
         case MULTIPART_ALL:
            result = "All multipart data";
            break;
         case MULTIPART_FORM_DATA:
         	result = "Multipart form data";
         	break;
         	
         case TEXT_ALL:
            result = "All texts";
            break;
         case TEXT_CSS:
            result = "CSS stylesheet";
            break;
         case TEXT_HTML:
            result = "HTML document";
            break;
         case TEXT_PLAIN:
            result = "Plain text";
            break;
         case TEXT_URI_LIST:
            result = "List of URIs";
            break;
         case TEXT_VCARD:
         	result = "vCard";
         	break;
         case TEXT_XML:
            result = "XML text";
            break;
            
         case VIDEO_ALL:
            result = "All videos";
            break;
         case VIDEO_AVI:
         	result = "AVI video";
         	break;
         case VIDEO_MPEG:
         	result = "MPEG video";
         	break;
         case VIDEO_QUICKTIME:
         	result = "Quicktime video";
         	break;
         case VIDEO_WMV:
         	result = "Windows movie";
         	break;
      }

      return result;
   }

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    */
   public List<Parameter> getParameters()
   {
      return null;
   }

   /**
    * Returns the value of a parameter with a given name.
    * @param name The name of the parameter to return.
    * @return The value of the parameter with a given name.
    */
   public String getParameterValue(String name)
   {
      return null;
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
   	return includes(this, included);
   }
   
   /**
    * Indicates if a given media type is included in the current one.
    * The test is true if both types are equal or if the given media type is within the range of the 
    * current one. For example, @link{ALL} includes all media types. 
    * Parameters are ignored for this comparison. 
    * @param including The including media type.
    * @param included The media type to test for inclusion.
    * @return True if the given media type is included in the current one.
    */
   public static boolean includes(MediaType including, MediaType included)
   {
   	boolean result = including.equals(ALL);
   	
   	if(result)
   	{
   		// The ALL media type includes all other types.
   	}
   	else
   	{
   		result = including.equals(included);
   		
   		if(result)
   		{
   			// Both media types are equal
   		}
   		else
   		{
   			result = including.getMainType().equals(included.getMainType()) && 
   						including.getSubType().equals("*");
   			
   			if(result)
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

   /**
    * Returns the media type name.
    * @return The media type name.
    */
   public String toString()
   {
      return getName();
   }

}
