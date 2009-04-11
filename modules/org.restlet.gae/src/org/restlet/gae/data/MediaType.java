/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gae.data;

import java.util.HashMap;
import java.util.Map;

import org.restlet.gae.engine.util.SystemUtils;
import org.restlet.gae.util.Series;

/**
 * Media type used in representations and preferences.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia< /a>
 * @author Jerome Louvel
 */
public final class MediaType extends Metadata {

    /**
     * The known media types registered with {@link #register(String, String)},
     * retrievable using {@link #valueOf(String)}.
     */
    private static Map<String, MediaType> types = null;

    public static final MediaType ALL = register("*/*", "All media");

    public static final MediaType APPLICATION_ALL = register("application/*",
            "All application documents");

    public static final MediaType APPLICATION_ALL_XML = register(
            "application/*+xml", "All application/*+xml documents");

    public static final MediaType APPLICATION_ATOM = register(
            "application/atom+xml", "Atom document");

    /**
     * @deprecated Use {@link #APPLICATION_ATOMPUB_SERVICE} instead.
     */
    @Deprecated
    public static final MediaType APPLICATION_ATOM_SERVICE_XML = register(
            "application/atomsvc+xml", "Atom service document");

    /**
     * @deprecated Use {@link #APPLICATION_ATOMPUB_SERVICE} instead.
     */
    @Deprecated
    public static final MediaType APPLICATION_ATOM_XML = register(
            "application/atom+xml", "Atom document");

    public static final MediaType APPLICATION_ATOMPUB_CATEGORY = register(
            "application/atomcat+xml", "Atom category document");

    public static final MediaType APPLICATION_ATOMPUB_SERVICE = register(
            "application/atomsvc+xml", "Atom service document");

    public static final MediaType APPLICATION_CAB = register(
            "application/vnd.ms-cab-compressed", "Microsoft Cabinet archive");

    public static final MediaType APPLICATION_COMPRESS = register(
            "application/x-compress", "Compressed file");

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

    public static final MediaType APPLICATION_JAVA = register(
            "application/java", "Java class");

    public static final MediaType APPLICATION_JAVA_ARCHIVE = register(
            "application/java-archive", "Java archive");

    public static final MediaType APPLICATION_JAVA_OBJECT = register(
            "application/x-java-serialized-object", "Java serialized object");

    public static final MediaType APPLICATION_JAVASCRIPT = register(
            "application/x-javascript", "Javascript document");

    public static final MediaType APPLICATION_JNLP = register(
            "application/x-java-jnlp-file", "JNLP");

    public static final MediaType APPLICATION_JSON = register(
            "application/json", "JavaScript Object Notation document");

    public static final MediaType APPLICATION_KML = register(
            "application/vnd.google-earth.kml+xml",
            "Google Earth/Maps KML document");

    public static final MediaType APPLICATION_KMZ = register(
            "application/vnd.google-earth.kmz",
            "Google Earth/Maps KMZ document");

    public static final MediaType APPLICATION_LATEX = register(
            "application/x-latex", "LaTeX");

    public static final MediaType APPLICATION_MAC_BINHEX40 = register(
            "application/mac-binhex40", "Mac binhex40");

    public static final MediaType APPLICATION_MATHML = register(
            "application/mathml+xml", "MathML XML document");

    /**
     * @deprecated Use {@link #APPLICATION_MATHML} instead.
     */
    @Deprecated
    public static final MediaType APPLICATION_MATHML_XML = register(
            "application/mathml+xml", "MathML XML document");

    public static final MediaType APPLICATION_MSOFFICE_DOCM = register(
            "application/vnd.ms-word.document.macroEnabled.12",
            "Office Word 2007 macro-enabled document");

    public static final MediaType APPLICATION_MSOFFICE_DOCX = register(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "Microsoft Office Word 2007 document");

    public static final MediaType APPLICATION_MSOFFICE_DOTM = register(
            "application/vnd.ms-word.template.macroEnabled.12",
            "Office Word 2007 macro-enabled document template");

    public static final MediaType APPLICATION_MSOFFICE_DOTX = register(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            "Office Word 2007 template");

    public static final MediaType APPLICATION_MSOFFICE_ONETOC = register(
            "application/onenote", "Microsoft Office OneNote 2007 TOC");

    public static final MediaType APPLICATION_MSOFFICE_ONETOC2 = register(
            "application/onenote", "Office OneNote 2007 TOC");

    public static final MediaType APPLICATION_MSOFFICE_POTM = register(
            "application/vnd.ms-powerpoint.template.macroEnabled.12",
            "Office PowerPoint 2007 macro-enabled presentation template");

    public static final MediaType APPLICATION_MSOFFICE_POTX = register(
            "application/vnd.openxmlformats-officedocument.presentationml.template",
            "Office PowerPoint 2007 template");

    public static final MediaType APPLICATION_MSOFFICE_PPAM = register(
            "application/vnd.ms-powerpoint.addin.macroEnabled.12",
            "Office PowerPoint 2007 add-in");

    public static final MediaType APPLICATION_MSOFFICE_PPSM = register(
            "application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
            "Office PowerPoint 2007 macro-enabled slide show");

    public static final MediaType APPLICATION_MSOFFICE_PPSX = register(
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "Office PowerPoint 2007 slide show");

    public static final MediaType APPLICATION_MSOFFICE_PPTM = register(
            "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
            "Office PowerPoint 2007 macro-enabled presentation");

    public static final MediaType APPLICATION_MSOFFICE_PPTX = register(
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "Microsoft Office PowerPoint 2007 presentation");

    public static final MediaType APPLICATION_MSOFFICE_SLDM = register(
            "application/vnd.ms-powerpoint.slide.macroEnabled.12",
            "Office PowerPoint 2007 macro-enabled slide");

    public static final MediaType APPLICATION_MSOFFICE_SLDX = register(
            "application/vnd.openxmlformats-officedocument.presentationml.slide",
            "Office PowerPoint 2007 slide");

    public static final MediaType APPLICATION_MSOFFICE_XLAM = register(
            "application/vnd.ms-excel.addin.macroEnabled.12",
            "Office Excel 2007 add-in");

    public static final MediaType APPLICATION_MSOFFICE_XLSB = register(
            "application/vnd.ms-excel.sheet.binary.macroEnabled.12",
            "Office Excel 2007 binary workbook");

    public static final MediaType APPLICATION_MSOFFICE_XLSM = register(
            "application/vnd.ms-excel.sheet.macroEnabled.12",
            "Office Excel 2007 macro-enabled workbook");

    public static final MediaType APPLICATION_MSOFFICE_XLSX = register(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Microsoft Office Excel 2007 workbook");

    public static final MediaType APPLICATION_MSOFFICE_XLTM = register(
            "application/vnd.ms-excel.template.macroEnabled.12",
            "Office Excel 2007 macro-enabled workbook template");

    public static final MediaType APPLICATION_MSOFFICE_XLTX = register(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            "Office Excel 2007 template");

    public static final MediaType APPLICATION_OCTET_STREAM = register(
            "application/octet-stream", "Raw octet stream");

    public static final MediaType APPLICATION_OPENOFFICE_ODB = register(
            "application/vnd.oasis.opendocument.database",
            "OpenDocument Database");

    public static final MediaType APPLICATION_OPENOFFICE_ODC = register(
            "application/vnd.oasis.opendocument.chart", "OpenDocument Chart");

    public static final MediaType APPLICATION_OPENOFFICE_ODF = register(
            "application/vnd.oasis.opendocument.formula",
            "OpenDocument Formula");

    public static final MediaType APPLICATION_OPENOFFICE_ODG = register(
            "application/vnd.oasis.opendocument.graphics",
            "OpenDocument Drawing");

    public static final MediaType APPLICATION_OPENOFFICE_ODI = register(
            "application/vnd.oasis.opendocument.image", "OpenDocument Image ");

    public static final MediaType APPLICATION_OPENOFFICE_ODM = register(
            "application/vnd.oasis.opendocument.text-master",
            "OpenDocument Master Document");

    public static final MediaType APPLICATION_OPENOFFICE_ODP = register(
            "application/vnd.oasis.opendocument.presentation",
            "OpenDocument Presentation ");

    public static final MediaType APPLICATION_OPENOFFICE_ODS = register(
            "application/vnd.oasis.opendocument.spreadsheet",
            "OpenDocument Spreadsheet");

    public static final MediaType APPLICATION_OPENOFFICE_ODT = register(
            "application/vnd.oasis.opendocument.text ", "OpenDocument Text");

    public static final MediaType APPLICATION_OPENOFFICE_OTG = register(
            "application/vnd.oasis.opendocument.graphics-template",
            "OpenDocument Drawing Template");

    public static final MediaType APPLICATION_OPENOFFICE_OTH = register(
            "application/vnd.oasis.opendocument.text-web",
            "HTML Document Template");

    public static final MediaType APPLICATION_OPENOFFICE_OTP = register(
            "application/vnd.oasis.opendocument.presentation-template",
            "OpenDocument Presentation Template");

    public static final MediaType APPLICATION_OPENOFFICE_OTS = register(
            "application/vnd.oasis.opendocument.spreadsheet-template",
            "OpenDocument Spreadsheet Template");

    public static final MediaType APPLICATION_OPENOFFICE_OTT = register(
            "application/vnd.oasis.opendocument.text-template",
            "OpenDocument Text Template");

    public static final MediaType APPLICATION_OPENOFFICE_OXT = register(
            "application/vnd.openofficeorg.extension",
            "OpenOffice.org extension");

    public static final MediaType APPLICATION_PDF = register("application/pdf",
            "Adobe PDF document");

    public static final MediaType APPLICATION_POSTSCRIPT = register(
            "application/postscript", "Postscript document");

    public static final MediaType APPLICATION_POWERPOINT = register(
            "application/vnd.ms-powerpoint", "Microsoft Powerpoint document");

    public static final MediaType APPLICATION_PROJECT = register(
            "application/vnd.ms-project", "Microsoft Project document");

    public static final MediaType APPLICATION_RDF_TRIG = register(
            "application/x-trig",
            "Plain text serialized Resource Description Framework document");

    public static final MediaType APPLICATION_RDF_TRIX = register(
            "application/trix",
            "Simple XML serialized Resource Description Framework document");

    public static final MediaType APPLICATION_RDF_TURTLE = register(
            "application/x-turtle",
            "Plain text serialized Resource Description Framework document");

    public static final MediaType APPLICATION_RDF_XML = register(
            "application/rdf+xml",
            "Normalized XML serialized Resource Description Framework document");

    public static final MediaType APPLICATION_RELAXNG_COMPACT = register(
            "application/relax-ng-compact-syntax",
            "Relax NG Schema document, Compact syntax");

    public static final MediaType APPLICATION_RELAXNG_XML = register(
            "application/x-relax-ng+xml",
            "Relax NG Schema document, XML syntax");

    public static final MediaType APPLICATION_RSS = register(
            "application/rss+xml", "Really Simple Syndication document");

    /**
     * @deprecated Use {@link #APPLICATION_RSS} instead.
     */
    @Deprecated
    public static final MediaType APPLICATION_RSS_XML = register(
            "application/rss+xml", "Really Simple Syndication document");

    public static final MediaType APPLICATION_RTF = register("application/rtf",
            "Rich Text Format document");

    public static final MediaType APPLICATION_SPARQL_RESULTS_JSON = register(
            "application/sparql-results+json",
            "SPARQL Query Results JSON document");

    public static final MediaType APPLICATION_SPARQL_RESULTS_XML = register(
            "application/sparql-results+xml",
            "SPARQL Query Results XML document");

    public static final MediaType APPLICATION_SPSS_SAV = register(
            "application/x-spss-sav", "SPSS Data");

    public static final MediaType APPLICATION_SPSS_SPS = register(
            "application/x-spss-sps", "SPSS Script Syntax");

    public static final MediaType APPLICATION_STATA_STA = register(
            "application/x-stata", "Stata data file");

    public static final MediaType APPLICATION_STUFFIT = register(
            "application/x-stuffit", "Stuffit archive");

    public static final MediaType APPLICATION_TAR = register(
            "application/x-tar", "Tar archive");

    public static final MediaType APPLICATION_TEX = register(
            "application/x-tex", "Tex file");

    public static final MediaType APPLICATION_TROFF_MAN = register(
            "application/x-troff-man", "LaTeX");

    public static final MediaType APPLICATION_VOICEXML = register(
            "application/voicexml+xml", "VoiceXML");

    public static final MediaType APPLICATION_W3C_SCHEMA = register(
            "application/x-xsd+xml", "W3C XML Schema document");

    /**
     * @deprecated Use {@link #APPLICATION_W3C_SCHEMA} instead.
     */
    @Deprecated
    public static final MediaType APPLICATION_W3C_SCHEMA_XML = register(
            "application/x-xsd+xml", "W3C XML Schema document");

    public static final MediaType APPLICATION_W3C_XSLT = register(
            "application/xsd+xml", "W3C XSLT Stylesheet");

    public static final MediaType APPLICATION_WADL = register(
            "application/vnd.sun.wadl+xml",
            "Web Application Description Language document");

    /**
     * @deprecated Use {@link #APPLICATION_WADL} instead.
     */
    @Deprecated
    public static final MediaType APPLICATION_WADL_XML = register(
            "application/vnd.sun.wadl+xml",
            "Web Application Description Language document");

    public static final MediaType APPLICATION_WORD = register(
            "application/msword", "Microsoft Word document");

    public static final MediaType APPLICATION_WWW_FORM = register(
            "application/x-www-form-urlencoded", "Web form (URL encoded)");

    public static final MediaType APPLICATION_XHTML = register(
            "application/xhtml+xml", "XHTML document");

    /**
     * @deprecated Use {@link #APPLICATION_XHTML} instead.
     */
    @Deprecated
    public static final MediaType APPLICATION_XHTML_XML = register(
            "application/xhtml+xml", "XHTML document");

    public static final MediaType APPLICATION_XML = register("application/xml",
            "XML document");

    public static final MediaType APPLICATION_XML_DTD = register(
            "application/xml-dtd", "XML DTD");

    public static final MediaType APPLICATION_XUL = register(
            "application/vnd.mozilla.xul+xml", "XUL document");

    public static final MediaType APPLICATION_ZIP = register("application/zip",
            "Zip archive");

    public static final MediaType AUDIO_ALL = register("audio/*", "All audios");

    public static final MediaType AUDIO_BASIC = register("audio/basic",
            "AU audio");

    public static final MediaType AUDIO_MIDI = register("audio/midi",
            "MIDI audio");

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

    public static final MediaType IMAGE_TIFF = register("image/tiff",
            "TIFF image");

    public static final MediaType MESSAGE_ALL = register("message/*",
            "All messages");

    public static final MediaType MODEL_ALL = register("model/*", "All models");

    public static final MediaType MODEL_VRML = register("model/vrml", "VRML");

    public static final MediaType MULTIPART_ALL = register("multipart/*",
            "All multipart data");

    public static final MediaType MULTIPART_FORM_DATA = register(
            "multipart/form-data", "Multipart form data");

    public static final MediaType TEXT_ALL = register("text/*", "All texts");

    public static final MediaType TEXT_CALENDAR = register("text/calendar",
            "iCalendar event");

    public static final MediaType TEXT_CSS = register("text/css",
            "CSS stylesheet");

    public static final MediaType TEXT_CSV = register("text/csv",
            "Comma-separated Values");

    public static final MediaType TEXT_DAT = register("text/x-fixed-field",
            "Fixed-width Values");

    public static final MediaType TEXT_HTML = register("text/html",
            "HTML document");

    public static final MediaType TEXT_J2ME_APP_DESCRIPTOR = register(
            "text/vnd.sun.j2me.app-descriptor", "J2ME Application Descriptor");

    public static final MediaType TEXT_JAVASCRIPT = register("text/javascript",
            "Javascript document");

    public static final MediaType TEXT_PLAIN = register("text/plain",
            "Plain text");

    public static final MediaType TEXT_RDF_N3 = register("text/n3",
            "N3 serialized Resource Description Framework document");

    public static final MediaType TEXT_TSV = register(
            "text/tab-separated-values", "Tab-separated Values");

    public static final MediaType TEXT_URI_LIST = register("text/uri-list",
            "List of URIs");

    public static final MediaType TEXT_VCARD = register("text/x-vcard", "vCard");

    public static final MediaType TEXT_XML = register("text/xml", "XML text");

    /**
     * Illegal ASCII characters as defined in RFC 1521.
     * http://www.ietf.org/rfc/rfc1521.txt
     */
    private static final String TSPECIALS = "()<>@,;:/[]?=\\\"";

    public static final MediaType VIDEO_ALL = register("video/*", "All videos");

    public static final MediaType VIDEO_AVI = register("video/x-msvideo",
            "AVI video");

    public static final MediaType VIDEO_MP4 = register("video/mp4",
            "MPEG-4 video");

    public static final MediaType VIDEO_MPEG = register("video/mpeg",
            "MPEG video");

    public static final MediaType VIDEO_QUICKTIME = register("video/quicktime",
            "Quicktime video");

    public static final MediaType VIDEO_WMV = register("video/x-ms-wmv",
            "Windows movie");

    /**
     * Returns the first of the most specific MediaTypes of the given array of
     * MediaTypes.
     * <p>
     * Examples:
     * <ul>
     * <li>"text/plain" is more specific than "text/*" or "image/*"</li>
     * <li>"text/html" is same specific as "application/pdf" or "image/jpg"</li>
     * <li>"text/*" is same specific than "application/*" or "image/*"</li>
     * <li>"*<!----->/*" is the must unspecific MediaType</li>
     * </ul>
     * 
     * @param mediaTypes
     *            An array of media types.
     * @return The most concrete MediaType.
     * @throws IllegalArgumentException
     *             If the array is null or empty.
     */
    public static MediaType getMostSpecific(MediaType... mediaTypes)
            throws IllegalArgumentException {
        if ((mediaTypes == null) || (mediaTypes.length == 0)) {
            throw new IllegalArgumentException(
                    "You must give at least one MediaType");
        }

        if (mediaTypes.length == 1) {
            return mediaTypes[0];
        }

        MediaType mostSpecific = mediaTypes[mediaTypes.length - 1];

        for (int i = mediaTypes.length - 2; i >= 0; i--) {
            final MediaType mediaType = mediaTypes[i];

            if (mediaType.getMainType().equals("*")) {
                continue;
            }

            if (mostSpecific.getMainType().equals("*")) {
                mostSpecific = mediaType;
                continue;
            }

            if (mostSpecific.getSubType().contains("*")) {
                mostSpecific = mediaType;
                continue;
            }
        }

        return mostSpecific;
    }

    /**
     * Returns the known media types map.
     * 
     * @return the known media types map.
     */
    private static Map<String, MediaType> getTypes() {
        if (types == null) {
            types = new HashMap<String, MediaType>();
        }
        return types;
    }

    /**
     * Normalizes the specified token.
     * 
     * @param token
     *            Token to normalize.
     * @return The normalized token.
     * @throws IllegalArgumentException
     *             if <code>token</code> is not legal.
     */
    private static String normalizeToken(String token) {
        int length;
        char c;

        // Makes sure we're not dealing with a "*" token.
        token = token.trim();
        if ("".equals(token) || "*".equals(token))
            return "*";

        // Makes sure the token is RFC compliant.
        length = token.length();
        for (int i = 0; i < length; i++) {
            c = token.charAt(i);
            if (c <= 32 || c >= 127 || TSPECIALS.indexOf(c) != -1)
                throw new IllegalArgumentException("Illegal token: " + token);
        }

        return token;
    }

    /**
     * Normalizes the specified media type.
     * 
     * @param name
     *            The name of the type to normalize.
     * @return The normalized type.
     */
    private static String normalizeType(String name) {
        int index;
        String mainType;
        String subType;

        // Ignore null names (backward compatibility).
        if (name == null)
            return null;

        // No main / sub separator, assumes name/*.
        if ((index = name.indexOf('/')) == -1) {
            mainType = normalizeToken(name);
            subType = "*";
        }

        // Normalizes the main and sub types.
        else {
            mainType = normalizeToken(name.substring(0, index));
            subType = normalizeToken(name.substring(index + 1));
        }

        return mainType + '/' + subType;
    }

    /**
     * Register a media type as a known type that can later be retrieved using
     * {@link #valueOf(String)}. If the type already exists, the existing type
     * is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @param description
     *            The description.
     * @return The registered media type
     */
    public static synchronized MediaType register(String name,
            String description) {

        if (!getTypes().containsKey(name)) {
            final MediaType type = new MediaType(name, description);
            getTypes().put(name, type);
        }

        return getTypes().get(name);
    }

    /**
     * Returns the media type associated to a name. If an existing constant
     * exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The associated media type.
     */
    public static MediaType valueOf(String name) {
        MediaType result = null;

        if ((name != null) && !name.equals("")) {
            result = getTypes().get(name);
            if (result == null) {
                result = new MediaType(name);
            }
        }

        return result;
    }

    /** The list of parameters. */
    private volatile Series<Parameter> parameters;

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     */
    public MediaType(String name) {
        this(name, null, "Media type or range of media types");
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param parameters
     *            The list of parameters.
     */
    public MediaType(String name, Series<Parameter> parameters) {
        this(name, parameters, "Media type or range of media types");
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param parameters
     *            The list of parameters.
     * @param description
     *            The description.
     */
    @SuppressWarnings("unchecked")
    public MediaType(String name, Series<Parameter> parameters,
            String description) {
        super(normalizeType(name), description);

        if (parameters != null) {
            this.parameters = (Series<Parameter>) Series
                    .unmodifiableSeries(parameters);
        }
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param description
     *            The description.
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
     *            The object to compare to.
     * @param ignoreParameters
     *            Indicates if parameters should be ignored during comparison.
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
                    final MediaType that = (MediaType) obj;
                    result = ignoreParameters
                            || getParameters().equals(that.getParameters());
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
     * Returns the unmodifiable list of parameters. Creates a new instance if no
     * one has been set.
     * 
     * @return The list of parameters.
     */
    @SuppressWarnings("unchecked")
    public Series<Parameter> getParameters() {
        // Lazy initialization with double-check.
        Series<Parameter> p = this.parameters;
        if (p == null) {
            synchronized (this) {
                p = this.parameters;
                if (p == null) {
                    this.parameters = p = (Series<Parameter>) Series
                            .unmodifiableSeries(new Form());
                }
            }
        }
        return p;
    }

    /**
     * Returns the sub-type.
     * 
     * @return The sub-type.
     */
    public String getSubType() {
        String result = null;

        if (getName() != null) {
            final int slash = getName().indexOf('/');

            if (slash == -1) {
                // No subtype found, assume that all subtypes are accepted
                result = "*";
            } else {
                final int separator = getName().indexOf(';');
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
        return SystemUtils.hashCode(super.hashCode(), getParameters());
    }

    /**
     * Indicates if a given media type is included in the current one. The test
     * is true if both types are equal or if the given media type is within the
     * range of the current one. For example, ALL includes all media types.
     * Parameters are ignored for this comparison. A null media type is
     * considered as included into the current one.
     * <p>
     * Examples:
     * <ul>
     * <li>TEXT_ALL.includes(TEXT_PLAIN) -> true</li>
     * <li>TEXT_PLAIN.includes(TEXT_ALL) -> false</li>
     * </ul>
     * 
     * @param included
     *            The media type to test for inclusion.
     * @return True if the given media type is included in the current one.
     * @see #isCompatible(MediaType)
     */
    public boolean includes(MediaType included) {
        boolean result = equals(ALL) || (included == null) || equals(included);

        if (!result) {
            // Both media types are different
            if (getMainType().equals(included.getMainType())) {
                if (getSubType().equals(included.getSubType())) {
                    result = true;
                } else if (getSubType().equals("*")) {
                    result = true;
                } else if (getSubType().startsWith("*+")
                        && included.getSubType().endsWith(
                                getSubType().substring(2))) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Checks if this MediaType is compatible with the given media type.
     * <p>
     * Examples:
     * <ul>
     * <li>TEXT_ALL.isCompatible(TEXT_PLAIN) -> true</li>
     * <li>TEXT_PLAIN.isCompatible(TEXT_ALL) -> true</li>
     * <li>TEXT_PLAIN.isCompatible(APPLICATION_ALL) -> false</li>
     * </ul>
     * 
     * @param otherMediaType
     *            The other media type to compare.
     * @return True if the media types are compatible.
     * @see #includes(MediaType)
     */
    public boolean isCompatible(MediaType otherMediaType) {
        return (otherMediaType != null)
                && (includes(otherMediaType) || otherMediaType.includes(this));
    }

    /**
     * Checks if the current media type is concrete. A media type is concrete if
     * neither the main type nor the sub-type are equal to "*".
     * 
     * @return True if this media type is concrete.
     */
    public boolean isConcrete() {
        return !getName().contains("*");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        if (getName() != null) {
            sb.append(getName());

            for (final Parameter param : getParameters()) {
                sb.append("; ").append(param.getName()).append('=').append(
                        param.getValue());
            }
        }
        return sb.toString();
    }
}
