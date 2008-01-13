/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

import java.util.HashMap;
import java.util.Map;

import org.restlet.util.Engine;
import org.restlet.util.Series;

/**
 * Media type used in representations and preferences.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class MediaType extends Metadata {
    /**
     * The known media types registered with {@link #register(String, String)},
     * retrievable using {@link #valueOf(String)}.
     */
    private static Map<String, MediaType> types = new HashMap<String, MediaType>();

    public static final MediaType ALL = register("*/*", "All media");

    public static final MediaType APPLICATION_ALL = register("application/*",
            "All application documents");

    public static final MediaType APPLICATION_ATOM_XML = register(
            "application/atom+xml", "Atom syndication documents");

    public static final MediaType APPLICATION_CAB = register(
            "application/vnd.ms-cab-compressed", "Microsoft Cabinet archive");

    public static final MediaType APPLICATION_EXCEL = register(
            "application/vnd.ms-excel", "Microsoft Excel document");

    public static final MediaType APPLICATION_FLASH = register(
            "application/x-shockwave-flash", "Shockwave Flash object");

    public static final MediaType APPLICATION_GNU_TAR = register(
            "application/x-gtar", "GNU Tar archive");

    public static final MediaType APPLICATION_GNU_ZIP = register(
            "application/x-gzip", "GNU Zip archive");

    public static final MediaType APPLICATION_HTTP_COOKIES = register(
            "application/x-http-cookies", "HTTP cookies");

    public static final MediaType APPLICATION_JAVA_ARCHIVE = register(
            "application/java-archive", "Java archive");

    public static final MediaType APPLICATION_JAVA_OBJECT = register(
            "application/x-java-serialized-object", "Java serialized object");

    public static final MediaType APPLICATION_JAVASCRIPT = register(
            "application/x-javascript", "Javascript document");

    public static final MediaType APPLICATION_JSON = register(
            "application/json", "JavaScript Object Notation document");

    public static final MediaType APPLICATION_OCTET_STREAM = register(
            "application/octet-stream", "Raw octet stream");

    public static final MediaType APPLICATION_PDF = register("application/pdf",
            "Adobe PDF document");

    public static final MediaType APPLICATION_POSTSCRIPT = register(
            "application/postscript", "Postscript document");

    public static final MediaType APPLICATION_POWERPOINT = register(
            "application/vnd.ms-powerpoint", "Microsoft Powerpoint document");

    public static final MediaType APPLICATION_PROJECT = register(
            "application/vnd.ms-project", "Microsoft Project document");

    public static final MediaType APPLICATION_RDF_XML = register(
            "application/rdf+xml",
            "XML serialized Resource Description Framework document");

    public static final MediaType APPLICATION_RELAXNG_COMPACT = register(
            "application/relax-ng-compact-syntax",
            "Relax NG Schema document, Compact syntax");

    public static final MediaType APPLICATION_RELAXNG_XML = register(
            "application/x-relax-ng+xml",
            "Relax NG Schema document, XML syntax");

    public static final MediaType APPLICATION_RSS_XML = register(
            "application/rss+xml", "Really Simple Syndication document");

    public static final MediaType APPLICATION_RTF = register("application/rtf",
            "Rich Text Format document");

    public static final MediaType APPLICATION_STUFFIT = register(
            "application/x-stuffit", "Stuffit archive");

    public static final MediaType APPLICATION_TAR = register(
            "application/x-tar", "Tar archive");

    public static final MediaType APPLICATION_WADL_XML = register(
            "application/vnd.sun.wadl+xml",
            "Web Application Description Language document");

    public static final MediaType APPLICATION_WORD = register(
            "application/msword", "Microsoft Word document");

    public static final MediaType APPLICATION_W3C_SCHEMA_XML = register(
            "application/x-xsd+xml", "W3C XML Schema document");

    public static final MediaType APPLICATION_WWW_FORM = register(
            "application/x-www-form-urlencoded", "Web form (URL encoded)");

    public static final MediaType APPLICATION_XHTML_XML = register(
            "application/xhtml+xml", "XHTML document");

    public static final MediaType APPLICATION_XML = register("application/xml",
            "XML document");

    public static final MediaType APPLICATION_ZIP = register("application/zip",
            "Zip archive");

    public static final MediaType AUDIO_ALL = register("audio/*", "All audios");

    public static final MediaType AUDIO_MPEG = register("audio/mpeg",
            "MPEG audio (MP3)");

    public static final MediaType AUDIO_REAL = register("audio/x-pn-realaudio",
            "Real audio");

    public static final MediaType AUDIO_WAV = register("audio/x-wav",
            "Waveform audio");

    public static final MediaType IMAGE_ALL = register("image/*", "All images");

    public static final MediaType IMAGE_BMP = register("image/bmp",
            "Windows bitmap");

    public static final MediaType IMAGE_GIF = register("image/gif", "GIF image");

    public static final MediaType IMAGE_ICON = register("image/x-icon",
            "Windows icon (Favicon)");

    public static final MediaType IMAGE_JPEG = register("image/jpeg",
            "JPEG image");

    public static final MediaType IMAGE_PNG = register("image/png", "PNG image");

    public static final MediaType IMAGE_SVG = register("image/svg+xml",
            "Scalable Vector Graphics");

    public static final MediaType MESSAGE_ALL = register("message/*",
            "All messages");

    public static final MediaType MODEL_ALL = register("model/*", "All models");

    public static final MediaType MULTIPART_ALL = register("multipart/*",
            "All multipart data");

    public static final MediaType MULTIPART_FORM_DATA = register(
            "multipart/form-data", "Multipart form data");

    public static final MediaType TEXT_ALL = register("text/*", "All texts");

    public static final MediaType TEXT_CALENDAR = register("text/calendar",
            "iCalendar event");

    public static final MediaType TEXT_CSS = register("text/css",
            "CSS stylesheet");

    public static final MediaType TEXT_HTML = register("text/html",
            "HTML document");

    public static final MediaType TEXT_PLAIN = register("text/plain",
            "Plain text");

    public static final MediaType TEXT_RDF_N3 = register("text/rdf+n3",
            "N3 serialized Resource Description Framework document");

    public static final MediaType TEXT_URI_LIST = register("text/uri-list",
            "List of URIs");

    public static final MediaType TEXT_VCARD = register("text/x-vcard", "vCard");

    public static final MediaType TEXT_XML = register("text/xml", "XML text");

    public static final MediaType VIDEO_ALL = register("video/*", "All videos");

    public static final MediaType VIDEO_AVI = register("video/x-msvideo",
            "AVI video");

    public static final MediaType VIDEO_MPEG = register("video/mpeg",
            "MPEG video");

    public static final MediaType VIDEO_QUICKTIME = register("video/quicktime",
            "Quicktime video");

    public static final MediaType VIDEO_WMV = register("video/x-ms-wmv",
            "Windows movie");

    /**
     * Register a media type as a known type that can later be retrieved using
     * {@link #valueOf(String)}. If the type already exists, the existing type
     * is returned, otherwise a new instance is created.
     * 
     * @param name
     *                The name.
     * @param description
     *                The description.
     * @return The registered media type
     */
    public static MediaType register(String name, String description) {
        synchronized (types) {
            if (!types.containsKey(name)) {
                MediaType type = new MediaType(name, description);
                types.put(name, type);
            }

            return types.get(name);
        }
    }

    /**
     * Returns the media type associated to a name. If an existing constant
     * exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *                The name.
     * @return The associated media type.
     */
    public static MediaType valueOf(String name) {
        MediaType result = null;

        if (name != null) {
            result = types.get(name);
            if (result == null) {
                result = new MediaType(name);
            }
        }

        return result;
    }

    /** The list of parameters. */
    private Series<Parameter> parameters;

    /**
     * Constructor.
     * 
     * @param name
     *                The name.
     */
    public MediaType(String name) {
        this(name, null, "Media type or range of media types");
    }

    /**
     * Constructor.
     * 
     * @param name
     *                The name.
     * @param parameters
     *                The list of parameters.
     */
    public MediaType(String name, Series<Parameter> parameters) {
        this(name, parameters, "Media type or range of media types");
    }

    /**
     * Constructor.
     * 
     * @param name
     *                The name.
     * @param parameters
     *                The list of parameters.
     * @param description
     *                The description.
     */
    public MediaType(String name, Series<Parameter> parameters,
            String description) {
        super((name == null) ? null : name, description);
        this.parameters = parameters;
    }

    /**
     * Constructor.
     * 
     * @param name
     *                The name.
     * @param description
     *                The description.
     */
    public MediaType(String name, String description) {
        this(name, null, description);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return equals(obj, false);
    }

    /**
     * Test the equality of two media types, with the possibility to ignore the
     * parameters.
     * 
     * @param obj
     *                The object to compare to.
     * @param ignoreParameters
     *                Indicates if parameters should be ignored during
     *                comparison.
     * @return True if both media types are equal.
     */
    public boolean equals(Object obj, boolean ignoreParameters) {
        boolean result = (obj == this);

        // if obj == this no need to go further
        if (!result) {
            // test for equality at Metadata level i.e. name and value.
            if (super.equals(obj)) {
                // if obj isn't a mediatype or is null don't evaluate further
                if (obj instanceof MediaType) {
                    MediaType that = (MediaType) obj;
                    result = ignoreParameters
                            || this.getParameters()
                                    .equals(that.getParameters());
                }
            }
        }

        return result;
    }

    /**
     * Returns the main type.
     * 
     * @return The main type.
     */
    public String getMainType() {
        String result = null;

        if (getName() != null) {
            int index = getName().indexOf('/');

            // Some clients appear to use name types without subtypes
            if (index == -1) {
                index = getName().indexOf(';');
            }

            if (index == -1) {
                result = getName();
            } else {
                result = getName().substring(0, index);
            }
        }

        return result;
    }

    /**
     * Returns the modifiable list of parameters. Creates a new instance if no
     * one has been set.
     * 
     * @return The list of parameters.
     */
    public Series<Parameter> getParameters() {
        if (this.parameters == null)
            this.parameters = new Form();
        return this.parameters;
    }

    /**
     * Returns the sub-type.
     * 
     * @return The sub-type.
     */
    public String getSubType() {
        String result = null;

        if (getName() != null) {
            int slash = getName().indexOf('/');

            if (slash == -1) {
                // No subtype found, assume that all subtypes are accepted
                result = "*";
            } else {
                int separator = getName().indexOf(';');
                if (separator == -1) {
                    result = getName().substring(slash + 1);
                } else {
                    result = getName().substring(slash + 1, separator);
                }
            }
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Engine.hashCode(super.hashCode(), getParameters());
    }

    /**
     * Indicates if a given media type is included in the current one. The test
     * is true if both types are equal or if the given media type is within the
     * range of the current one. For example, ALL includes all media types.
     * Parameters are ignored for this comparison. A null media type is
     * considered as included into the current one.
     * 
     * @param included
     *                The media type to test for inclusion.
     * @return True if the given media type is included in the current one.
     */
    public boolean includes(MediaType included) {
        boolean result = equals(ALL) || included == null || equals(included);

        if (!result) {
            // Both media types are different
            result = getMainType().equals(included.getMainType())
                    && (getSubType().equals(included.getSubType()) || getSubType()
                            .equals("*"));
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (getName() != null) {
            sb.append(getName());

            for (Parameter param : getParameters()) {
                sb.append("; ").append(param.getName()).append('=').append(
                        param.getValue());
            }
        }

        return sb.toString();
    }
}
