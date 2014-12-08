/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.header.HeaderWriter;
import org.restlet.engine.util.SystemUtils;
import org.restlet.util.Series;

/**
 * Metadata used to specify the format of representations. The
 * {@link #getName()} method returns a full String representation of the media
 * type including the parameters.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 * @author Jerome Louvel
 */
public final class MediaType extends Metadata {

    /**
     * Illegal ASCII characters as defined in RFC 1521.<br>
     * Keep the underscore for the ordering
     * 
     * @see http://www.ietf.org/rfc/rfc1521.txt
     */
    private static final String _TSPECIALS = "()<>@,;:/[]?=\\\"";

    /**
     * The known media types registered with {@link #register(String, String)},
     * retrievable using {@link #valueOf(String)}.<br>
     * Keep the underscore for the ordering.
     */
    private static volatile Map<String, MediaType> _types = null;

    public static final MediaType ALL = register("*/*", "All media");

    public static final MediaType APPLICATION_ALL = register("application/*",
            "All application documents");

    public static final MediaType APPLICATION_ALL_JSON = register(
            "application/*+json", "All application/*+json documents");

    public static final MediaType APPLICATION_ALL_XML = register(
            "application/*+xml", "All application/*+xml documents");

    public static final MediaType APPLICATION_ATOM = register(
            "application/atom+xml", "Atom document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_ATOMPUB_CATEGORY = register(
            "application/atomcat+xml", "Atom category document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_ATOMPUB_SERVICE = register(
            "application/atomsvc+xml", "Atom service document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_CAB = register(
            "application/vnd.ms-cab-compressed", "Microsoft Cabinet archive");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_COMPRESS = register(
            "application/x-compress", "Compressed file");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_ECORE = register(
            "application/x-ecore+xmi+xml", "EMOF ECore metamodel");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_EXCEL = register(
            "application/vnd.ms-excel", "Microsoft Excel document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_FLASH = register(
            "application/x-shockwave-flash", "Shockwave Flash object");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_GNU_TAR = register(
            "application/x-gtar", "GNU Tar archive");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_GNU_ZIP = register(
            "application/x-gzip", "GNU Zip archive");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_HTTP_COOKIES = register(
            "application/x-http-cookies", "HTTP cookies");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_JAVA = register(
            "application/java", "Java class");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_JAVA_ARCHIVE = register(
            "application/java-archive", "Java archive");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_JAVA_OBJECT = register(
            "application/x-java-serialized-object", "Java serialized object");

    public static final MediaType APPLICATION_JAVA_OBJECT_GWT = register(
            "application/x-java-serialized-object+gwt",
            "Java serialized object (using GWT-RPC encoder)");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_JAVA_OBJECT_XML = register(
            "application/x-java-serialized-object+xml",
            "Java serialized object (using JavaBeans XML encoder)");

    public static final MediaType APPLICATION_JAVASCRIPT = register(
            "application/x-javascript", "Javascript document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_JNLP = register(
            "application/x-java-jnlp-file", "JNLP");

    public static final MediaType APPLICATION_JSON = register(
            "application/json", "JavaScript Object Notation document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_JSON_ACTIVITY = register(
            "application/activity+json", "Activity Streams JSON document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_JSON_PATCH = register(
            "application/json-patch", "JSON patch document");

    public static final MediaType APPLICATION_JSON_SMILE = register(
            "application/x-json-smile",
            "JavaScript Object Notation smile document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_KML = register(
            "application/vnd.google-earth.kml+xml",
            "Google Earth/Maps KML document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_KMZ = register(
            "application/vnd.google-earth.kmz",
            "Google Earth/Maps KMZ document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_LATEX = register(
            "application/x-latex", "LaTeX");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MAC_BINHEX40 = register(
            "application/mac-binhex40", "Mac binhex40");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MATHML = register(
            "application/mathml+xml", "MathML XML document");

    public static final MediaType APPLICATION_MSML = register(
            "application/msml+xml", "Media Server Markup Language");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_DOCM = register(
            "application/vnd.ms-word.document.macroEnabled.12",
            "Office Word 2007 macro-enabled document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_DOCX = register(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "Microsoft Office Word 2007 document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_DOTM = register(
            "application/vnd.ms-word.template.macroEnabled.12",
            "Office Word 2007 macro-enabled document template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_DOTX = register(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            "Office Word 2007 template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_ONETOC = register(
            "application/onenote", "Microsoft Office OneNote 2007 TOC");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_ONETOC2 = register(
            "application/onenote", "Office OneNote 2007 TOC");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_POTM = register(
            "application/vnd.ms-powerpoint.template.macroEnabled.12",
            "Office PowerPoint 2007 macro-enabled presentation template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_POTX = register(
            "application/vnd.openxmlformats-officedocument.presentationml.template",
            "Office PowerPoint 2007 template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_PPAM = register(
            "application/vnd.ms-powerpoint.addin.macroEnabled.12",
            "Office PowerPoint 2007 add-in");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_PPSM = register(
            "application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
            "Office PowerPoint 2007 macro-enabled slide show");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_PPSX = register(
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "Office PowerPoint 2007 slide show");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_PPTM = register(
            "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
            "Office PowerPoint 2007 macro-enabled presentation");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_PPTX = register(
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "Microsoft Office PowerPoint 2007 presentation");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_SLDM = register(
            "application/vnd.ms-powerpoint.slide.macroEnabled.12",
            "Office PowerPoint 2007 macro-enabled slide");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_SLDX = register(
            "application/vnd.openxmlformats-officedocument.presentationml.slide",
            "Office PowerPoint 2007 slide");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_XLAM = register(
            "application/vnd.ms-excel.addin.macroEnabled.12",
            "Office Excel 2007 add-in");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_XLSB = register(
            "application/vnd.ms-excel.sheet.binary.macroEnabled.12",
            "Office Excel 2007 binary workbook");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_XLSM = register(
            "application/vnd.ms-excel.sheet.macroEnabled.12",
            "Office Excel 2007 macro-enabled workbook");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_XLSX = register(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Microsoft Office Excel 2007 workbook");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_XLTM = register(
            "application/vnd.ms-excel.template.macroEnabled.12",
            "Office Excel 2007 macro-enabled workbook template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_MSOFFICE_XLTX = register(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            "Office Excel 2007 template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OCTET_STREAM = register(
            "application/octet-stream", "Raw octet stream");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODB = register(
            "application/vnd.oasis.opendocument.database",
            "OpenDocument Database");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODC = register(
            "application/vnd.oasis.opendocument.chart", "OpenDocument Chart");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODF = register(
            "application/vnd.oasis.opendocument.formula",
            "OpenDocument Formula");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODG = register(
            "application/vnd.oasis.opendocument.graphics",
            "OpenDocument Drawing");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODI = register(
            "application/vnd.oasis.opendocument.image", "OpenDocument Image ");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODM = register(
            "application/vnd.oasis.opendocument.text-master",
            "OpenDocument Master Document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODP = register(
            "application/vnd.oasis.opendocument.presentation",
            "OpenDocument Presentation ");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODS = register(
            "application/vnd.oasis.opendocument.spreadsheet",
            "OpenDocument Spreadsheet");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_ODT = register(
            "application/vnd.oasis.opendocument.text ", "OpenDocument Text");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_OTG = register(
            "application/vnd.oasis.opendocument.graphics-template",
            "OpenDocument Drawing Template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_OTH = register(
            "application/vnd.oasis.opendocument.text-web",
            "HTML Document Template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_OTP = register(
            "application/vnd.oasis.opendocument.presentation-template",
            "OpenDocument Presentation Template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_OTS = register(
            "application/vnd.oasis.opendocument.spreadsheet-template",
            "OpenDocument Spreadsheet Template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_OTT = register(
            "application/vnd.oasis.opendocument.text-template",
            "OpenDocument Text Template");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_OPENOFFICE_OXT = register(
            "application/vnd.openofficeorg.extension",
            "OpenOffice.org extension");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_PDF = register("application/pdf",
            "Adobe PDF document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_POSTSCRIPT = register(
            "application/postscript", "Postscript document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_POWERPOINT = register(
            "application/vnd.ms-powerpoint", "Microsoft Powerpoint document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_PROJECT = register(
            "application/vnd.ms-project", "Microsoft Project document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_RDF_TRIG = register(
            "application/x-trig",
            "Plain text serialized Resource Description Framework document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_RDF_TRIX = register(
            "application/trix",
            "Simple XML serialized Resource Description Framework document");

    // [ifndef gwt] member
    /**
     * @deprecated Replaced by the official {@link #TEXT_TURTLE} media type.
     */
    @Deprecated
    public static final MediaType APPLICATION_RDF_TURTLE = register(
            "application/x-turtle",
            "Plain text serialized Resource Description Framework document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_RDF_XML = register(
            "application/rdf+xml",
            "Normalized XML serialized Resource Description Framework document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_RELAXNG_COMPACT = register(
            "application/relax-ng-compact-syntax",
            "Relax NG Schema document, Compact syntax");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_RELAXNG_XML = register(
            "application/x-relax-ng+xml",
            "Relax NG Schema document, XML syntax");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_RSS = register(
            "application/rss+xml", "Really Simple Syndication document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_RTF = register("application/rtf",
            "Rich Text Format document");

    public static final MediaType APPLICATION_SDP = register("application/sdp",
            "Session Description Protocol");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_SPARQL_RESULTS_JSON = register(
            "application/sparql-results+json",
            "SPARQL Query Results JSON document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_SPARQL_RESULTS_XML = register(
            "application/sparql-results+xml",
            "SPARQL Query Results XML document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_SPSS_SAV = register(
            "application/x-spss-sav", "SPSS Data");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_SPSS_SPS = register(
            "application/x-spss-sps", "SPSS Script Syntax");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_STATA_STA = register(
            "application/x-stata", "Stata data file");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_STUFFIT = register(
            "application/x-stuffit", "Stuffit archive");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_TAR = register(
            "application/x-tar", "Tar archive");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_TEX = register(
            "application/x-tex", "Tex file");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_TROFF_MAN = register(
            "application/x-troff-man", "LaTeX");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_VOICEXML = register(
            "application/voicexml+xml", "VoiceXML");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_W3C_SCHEMA = register(
            "application/x-xsd+xml", "W3C XML Schema document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_W3C_XSLT = register(
            "application/xslt+xml", "W3C XSLT Stylesheet");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_WADL = register(
            "application/vnd.sun.wadl+xml",
            "Web Application Description Language document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_WORD = register(
            "application/msword", "Microsoft Word document");

    public static final MediaType APPLICATION_WWW_FORM = register(
            "application/x-www-form-urlencoded", "Web form (URL encoded)");

    public static final MediaType APPLICATION_XHTML = register(
            "application/xhtml+xml", "XHTML document");

    public static final MediaType APPLICATION_XMI = register(
            "application/xmi+xml", "XMI document");

    public static final MediaType APPLICATION_XML = register("application/xml",
            "XML document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_XML_DTD = register(
            "application/xml-dtd", "XML DTD");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_XQUERY = register(
            "application/xquery", "XQuery document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_XUL = register(
            "application/vnd.mozilla.xul+xml", "XUL document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_YAML = register(
            "application/x-yaml", "YAML document");

    // [ifndef gwt] member
    public static final MediaType APPLICATION_ZIP = register("application/zip",
            "Zip archive");

    // [ifndef gwt] member
    public static final MediaType AUDIO_ALL = register("audio/*", "All audios");

    // [ifndef gwt] member
    public static final MediaType AUDIO_BASIC = register("audio/basic",
            "AU audio");

    // [ifndef gwt] member
    public static final MediaType AUDIO_MIDI = register("audio/midi",
            "MIDI audio");

    // [ifndef gwt] member
    public static final MediaType AUDIO_MPEG = register("audio/mpeg",
            "MPEG audio (MP3)");

    // [ifndef gwt] member
    public static final MediaType AUDIO_REAL = register("audio/x-pn-realaudio",
            "Real audio");

    // [ifndef gwt] member
    public static final MediaType AUDIO_WAV = register("audio/x-wav",
            "Waveform audio");

    // [ifndef gwt] member
    public static final MediaType IMAGE_ALL = register("image/*", "All images");

    // [ifndef gwt] member
    public static final MediaType IMAGE_BMP = register("image/bmp",
            "Windows bitmap");

    // [ifndef gwt] member
    public static final MediaType IMAGE_GIF = register("image/gif", "GIF image");

    // [ifndef gwt] member
    public static final MediaType IMAGE_ICON = register("image/x-icon",
            "Windows icon (Favicon)");

    // [ifndef gwt] member
    public static final MediaType IMAGE_JPEG = register("image/jpeg",
            "JPEG image");

    // [ifndef gwt] member
    public static final MediaType IMAGE_PNG = register("image/png", "PNG image");

    // [ifndef gwt] member
    public static final MediaType IMAGE_SVG = register("image/svg+xml",
            "Scalable Vector Graphics");

    // [ifndef gwt] member
    public static final MediaType IMAGE_TIFF = register("image/tiff",
            "TIFF image");

    // [ifndef gwt] member
    public static final MediaType MESSAGE_ALL = register("message/*",
            "All messages");

    // [ifndef gwt] member
    public static final MediaType MESSAGE_HTTP = register("message/http",
            "HTTP message");

    // [ifndef gwt] member
    public static final MediaType MODEL_ALL = register("model/*", "All models");

    // [ifndef gwt] member
    public static final MediaType MODEL_VRML = register("model/vrml", "VRML");

    // [ifndef gwt] member
    public static final MediaType MULTIPART_ALL = register("multipart/*",
            "All multipart data");

    // [ifndef gwt] member
    public static final MediaType MULTIPART_FORM_DATA = register(
            "multipart/form-data", "Multipart form data");

    public static final MediaType TEXT_ALL = register("text/*", "All texts");

    // [ifndef gwt] member
    public static final MediaType TEXT_CALENDAR = register("text/calendar",
            "iCalendar event");

    public static final MediaType TEXT_CSS = register("text/css",
            "CSS stylesheet");

    // [ifndef gwt] member
    public static final MediaType TEXT_CSV = register("text/csv",
            "Comma-separated Values");

    // [ifndef gwt] member
    public static final MediaType TEXT_DAT = register("text/x-fixed-field",
            "Fixed-width Values");

    public static final MediaType TEXT_HTML = register("text/html",
            "HTML document");

    // [ifndef gwt] member
    public static final MediaType TEXT_J2ME_APP_DESCRIPTOR = register(
            "text/vnd.sun.j2me.app-descriptor", "J2ME Application Descriptor");

    public static final MediaType TEXT_JAVASCRIPT = register("text/javascript",
            "Javascript document");

    public static final MediaType TEXT_PLAIN = register("text/plain",
            "Plain text");

    // [ifndef gwt] member
    public static final MediaType TEXT_RDF_N3 = register("text/n3",
            "N3 serialized Resource Description Framework document");

    // [ifndef gwt] member
    public static final MediaType TEXT_RDF_NTRIPLES = register(
            "text/n-triples",
            "N-Triples serialized Resource Description Framework document");

    // [ifndef gwt] member
    public static final MediaType TEXT_TSV = register(
            "text/tab-separated-values", "Tab-separated Values");

    // [ifndef gwt] member
    public static final MediaType TEXT_TURTLE = register("text/turtle",
            "Plain text serialized Resource Description Framework document");

    public static final MediaType TEXT_URI_LIST = register("text/uri-list",
            "List of URIs");

    // [ifndef gwt] member
    public static final MediaType TEXT_VCARD = register("text/x-vcard", "vCard");

    public static final MediaType TEXT_XML = register("text/xml", "XML text");

    // [ifndef gwt] member
    public static final MediaType TEXT_YAML = register("text/x-yaml",
            "YAML document");

    // [ifndef gwt] member
    public static final MediaType VIDEO_ALL = register("video/*", "All videos");

    // [ifndef gwt] member
    public static final MediaType VIDEO_AVI = register("video/x-msvideo",
            "AVI video");

    // [ifndef gwt] member
    public static final MediaType VIDEO_MP4 = register("video/mp4",
            "MPEG-4 video");

    // [ifndef gwt] member
    public static final MediaType VIDEO_MPEG = register("video/mpeg",
            "MPEG video");

    // [ifndef gwt] member
    public static final MediaType VIDEO_QUICKTIME = register("video/quicktime",
            "Quicktime video");

    // [ifndef gwt] member
    public static final MediaType VIDEO_WMV = register("video/x-ms-wmv",
            "Windows movie");

    /**
     * Returns the first of the most specific media type of the given array of
     * {@link MediaType}s.
     * <p>
     * Examples:
     * <ul>
     * <li>"text/plain" is more specific than "text/*" or "image/*"</li>
     * <li>"text/html" is same specific as "application/pdf" or "image/jpg"</li>
     * <li>"text/*" is same specific than "application/*" or "image/*"</li>
     * <li>"*<!---->/*" is the most unspecific MediaType</li>
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

        MediaType mostSpecific = mediaTypes[0];

        for (int i = 1; i < mediaTypes.length; i++) {
            MediaType mediaType = mediaTypes[i];

            if (mediaType != null) {
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
        }

        return mostSpecific;
    }

    /**
     * Returns the known media types map.
     * 
     * @return the known media types map.
     */
    private static Map<String, MediaType> getTypes() {
        if (_types == null) {
            _types = new HashMap<String, MediaType>();
        }
        return _types;
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
            if (c <= 32 || c >= 127 || _TSPECIALS.indexOf(c) != -1)
                throw new IllegalArgumentException("Illegal token: " + token);
        }

        return token;
    }

    /**
     * Normalizes the specified media type.
     * 
     * @param name
     *            The name of the type to normalize.
     * @param parameters
     *            The parameters of the type to normalize.
     * @return The normalized type.
     */
    private static String normalizeType(String name,
            Series<Parameter> parameters) {
        int slashIndex;
        int colonIndex;
        String mainType;
        String subType;
        StringBuilder params = null;

        // Ignore null names (backward compatibility).
        if (name == null)
            return null;

        // Check presence of parameters
        if ((colonIndex = name.indexOf(';')) != -1) {
            params = new StringBuilder(name.substring(colonIndex));
            name = name.substring(0, colonIndex);
        }

        // No main / sub separator, assumes name/*.
        if ((slashIndex = name.indexOf('/')) == -1) {
            mainType = normalizeToken(name);
            subType = "*";
        } else {
            // Normalizes the main and sub types.
            mainType = normalizeToken(name.substring(0, slashIndex));
            subType = normalizeToken(name.substring(slashIndex + 1));
        }

        // Merge parameters taken from the name and the method argument.
        if (parameters != null && !parameters.isEmpty()) {
            try {
                if (params == null) {
                    params = new StringBuilder();
                }
                HeaderWriter<Parameter> hw = new HeaderWriter<Parameter>() {
                    @Override
                    public HeaderWriter<Parameter> append(Parameter value) {
                        return appendExtension(value);
                    }
                };
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter p = parameters.get(i);
                    hw.appendParameterSeparator();
                    hw.appendSpace();
                    hw.append(p);
                }
                params.append(hw.toString());
                hw.close();
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.INFO,
                        "Unable to parse the media type parameter", e);
            }
        }

        return (params == null) ? mainType + '/' + subType : mainType + '/'
                + subType + params.toString();
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
        super(normalizeType(name, parameters), description);

        if (parameters != null) {
            // [ifndef gwt] instruction
            this.parameters = (Series<Parameter>) Series
                    .unmodifiableSeries(parameters);
            // [ifdef gwt] instruction uncomment
            // this.parameters =
            // org.restlet.engine.util.ParameterSeries.unmodifiableSeries(parameters);
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
            // if obj isn't a mediatype or is null don't evaluate further
            if (obj instanceof MediaType) {
                final MediaType that = (MediaType) obj;
                if (getMainType().equals(that.getMainType())
                        && getSubType().equals(that.getSubType())) {
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
     * Returns the unmodifiable list of parameters corresponding to subtype
     * modifiers. Creates a new instance if no one has been set.
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
                    Series<Parameter> params = null;

                    if (getName() != null) {
                        int index = getName().indexOf(';');

                        if (index != -1) {
                            params = new Form(getName().substring(index + 1)
                                    .trim(), ';');
                        }
                    }

                    if (params == null) {
                        // [ifndef gwt] instruction
                        params = new Series<Parameter>(Parameter.class);
                        // [ifdef gwt] instruction uncomment
                        // params = new
                        // org.restlet.engine.util.ParameterSeries();
                    }

                    // [ifndef gwt] instruction
                    this.parameters = p = (Series<Parameter>) Series
                            .unmodifiableSeries(params);
                    // [ifdef gwt] instruction uncomment
                    // this.parameters = p =
                    // org.restlet.engine.util.ParameterSeries.unmodifiableSeries(params);
                }
            }
        }
        return p;
    }

    /**
     * {@inheritDoc}<br>
     * In case the media type has parameters, this method returns the
     * concatenation of the main type and the subtype. If the subtype is not
     * equal to "*", it returns the concatenation of the main type and "*".
     * Otherwise, it returns either the {@link #ALL} media type if it is already
     * the {@link #ALL} media type, or null.
     */
    @Override
    public MediaType getParent() {
        MediaType result = null;

        if (getParameters().size() > 0) {
            result = MediaType.valueOf(getMainType() + "/" + getSubType());
        } else {
            if (getSubType().equals("*")) {
                result = equals(ALL) ? null : ALL;
            } else {
                result = MediaType.valueOf(getMainType() + "/*");
            }
        }

        return result;
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
     * Indicates if a given media type is included in the current one @see
     * {@link #includes(Metadata, boolean)}. It ignores the parameters.
     * 
     * @param included
     *            The media type to test for inclusion.
     * @return True if the given media type is included in the current one.
     * @see #isCompatible(Metadata)
     */
    @Override
    public boolean includes(Metadata included) {
        return includes(included, true);
    }

    /**
     * Indicates if a given media type is included in the current one @see
     * {@link #includes(Metadata, boolean)}. The test is true if both types are
     * equal or if the given media type is within the range of the current one.
     * For example, ALL includes all media types. Parameters are ignored for
     * this comparison. A null media type is considered as included into the
     * current one. It ignores the parameters.
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
     * @see #isCompatible(Metadata)
     */
    public boolean includes(Metadata included, boolean ignoreParameters) {
        boolean result = equals(ALL) || equals(included);

        if (!result && (included instanceof MediaType)) {
            MediaType includedMediaType = (MediaType) included;

            if (getMainType().equals(includedMediaType.getMainType())) {
                // Both media types are different
                if (getSubType().equals(includedMediaType.getSubType())) {
                    if (ignoreParameters) {
                        result = true;
                    } else {
                        // Check parameters:
                        // Media type A includes media type B if for each param
                        // name/value pair in A, B contains the same name/value.
                        result = true;
                        for (int i = 0; result && i < getParameters().size(); i++) {
                            Parameter param = getParameters().get(i);
                            Parameter includedParam = includedMediaType
                                    .getParameters().getFirst(param.getName());

                            // If there was no param with the same name, or the
                            // param with the same name had a different value,
                            // then no match.
                            result = (includedParam != null && param.getValue()
                                    .equals(includedParam.getValue()));
                        }
                    }
                } else if (getSubType().equals("*")) {
                    result = true;
                } else if (getSubType().startsWith("*+")
                        && includedMediaType.getSubType().endsWith(
                                getSubType().substring(2))) {
                    result = true;
                }
            }
        }

        return result;
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

}
