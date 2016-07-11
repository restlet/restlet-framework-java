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

package org.restlet.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.engine.application.MetadataExtension;

/**
 * Application service providing access to metadata and their associated
 * extension names. The list of default mappings is documented in the
 * {@link #addCommonExtensions()} method.<br>
 * <br>
 * Internally, the mappings are stored as a list of "extension, metadata" pairs.
 * 
 * @author Jerome Louvel
 */
public class MetadataService extends Service {
    /** The default character set for textual representations. */
    private volatile CharacterSet defaultCharacterSet;

    /** The default encoding for representations. */
    private volatile Encoding defaultEncoding;

    /** The default language for representations. */
    private volatile Language defaultLanguage;

    /** The default media type for representations. */
    private volatile MediaType defaultMediaType;

    /** The list of mappings between extension names and metadata. */
    private final List<MetadataExtension> mappings;

    /**
     * Constructor. Sets the default language to {@link Language#ENGLISH_US},
     * the default encoding to {@link Encoding#IDENTITY} (no encoding) and the
     * default media type to {@link MediaType#APPLICATION_OCTET_STREAM}. It also
     * calls the {@link #addCommonExtensions()} method.
     */
    public MetadataService() {
        this.defaultCharacterSet = CharacterSet.DEFAULT;
        this.defaultEncoding = Encoding.IDENTITY;
        this.defaultLanguage = Language.DEFAULT;
        // [ifndef gwt] instruction
        this.defaultMediaType = MediaType.APPLICATION_OCTET_STREAM;
        // [ifdef gwt] instruction uncomment
        // this.defaultMediaType = MediaType.APPLICATION_JSON;
        this.mappings = new CopyOnWriteArrayList<MetadataExtension>();
        addCommonExtensions();
    }

    /**
     * Adds a common list of associations from extensions to metadata.<br>
     * 
     * The list of languages extensions:<br>
     * <ul>
     * <li>en: English</li>
     * <li>es: Spanish</li>
     * <li>fr: French</li>
     * </ul>
     * <br>
     * The list of character set extensions:<br>
     * <ul>
     * <li>ascii: US-ASCII</li>
     * </ul>
     * <br>
     * The list of media type extensions:<br>
     * <ul>
     * <li>ai: PostScript document</li>
     * <li>atom: Atom syndication document</li>
     * <li>au: AU audio file</li>
     * <li>bin: Binary file</li>
     * <li>bmp: Bitmap graphics</li>
     * <li>class: Java bytecode</li>
     * <li>css: CSS stylesheet</li>
     * <li>csv: Comma-separated Values</li>
     * <li>dat: Fixed-width Values</li>
     * <li>dib: Device-Independent Bitmap Graphics</li>
     * <li>doc: Microsoft Word document</li>
     * <li>docx: Microsoft Office Word 2007 document</li>
     * <li>docm: Office Word 2007 macro-enabled document</li>
     * <li>dotx: Office Word 2007 template</li>
     * <li>dotm: Office Word 2007 macro-enabled document template</li>
     * <li>dtd: XML Document Type Definition</li>
     * <li>eps: Encapsulated PostScript</li>
     * <li>exe: Executable File (Microsoft Corporation)</li>
     * <li>fmt: FreeMarker encoding</li>
     * <li>form: Web forms (URL encoded)</li>
     * <li>ftl: FreeMarker encoding</li>
     * <li>gif: GIF image</li>
     * <li>gwt: Java serialized object (using GWT-RPC encoder)</li>
     * <li>hqx: BinHex 4 Compressed Archive (Macintosh)</li>
     * <li>htm, html: HTML document</li>
     * <li>ico: Windows icon (Favicon)</li>
     * <li>jad: Java Application Descriptor file</li>
     * <li>jar: Java Archive</li>
     * <li>java: Java source code</li>
     * <li>jnlp: Java Web start launch file</li>
     * <li>jpe, jpeg, jpg: JPEG image</li>
     * <li>js: JavaScript document</li>
     * <li>jsf: Java Server Faces file</li>
     * <li>json: JavaScript Object Notation document</li>
     * <li>jsonsmile: JavaScript Object Notation smile document</li>
     * <li>kar: Karaoke MIDI file</li>
     * <li>latex: LaTeX document</li>
     * <li>man: Manual file</li>
     * <li>mathml: Mathml XML document</li>
     * <li>mid, midi: MIDI Audio</li>
     * <li>mov, qt: QuickTime video clip (Apple Computer, Inc.)</li>
     * <li>mp2, mp3: MPEG Audio Stream file</li>
     * <li>mp4: MPEG-4 video file</li>
     * <li>mpe, mpeg, mpg: MPEG video clip</li>
     * <li>n3: RDF N3 document</li>
     * <li>nt: RDF N-Triples document</li>
     * <li>odb: OpenDocument Database</li>
     * <li>odc: OpenDocument Chart</li>
     * <li>odf: OpenDocument Formula</li>
     * <li>odg: OpenDocument Drawing</li>
     * <li>odi: OpenDocument Image</li>
     * <li>odm: OpenDocument Master Document</li>
     * <li>odp: OpenDocument Presentation</li>
     * <li>ods: OpenDocument Spreadsheet</li>
     * <li>odt: OpenDocument Text</li>
     * <li>onetoc: Microsoft Office OneNote 2007 TOC</li>
     * <li>onetoc2: Office OneNote 2007 TOC</li>
     * <li>otg: OpenDocument Drawing Template</li>
     * <li>oth: HTML Document Template</li>
     * <li>otp: OpenDocument Presentation Template</li>
     * <li>ots: OpenDocument Spreadsheet Template</li>
     * <li>ott: OpenDocument Text Template</li>
     * <li>oxt: OpenOffice.org extension</li>
     * <li>pdf: Adobe PDF document</li>
     * <li>png: PNG image</li>
     * <li>potm: Office PowerPoint 2007 macro-enabled presentation template</li>
     * <li>potx: Office PowerPoint 2007 template</li>
     * <li>ppam: Office PowerPoint 2007 add-in</li>
     * <li>pps, ppt: Microsoft Powerpoint document</li>
     * <li>ppsm: Office PowerPoint 2007 macro-enabled slide show</li>
     * <li>ppsx: Office PowerPoint 2007 slide show</li>
     * <li>pptm: Office PowerPoint 2007 macro-enabled presentation</li>
     * <li>pptx: Microsoft Office PowerPoint 2007 presentation</li>
     * <li>ps: PostScript document</li>
     * <li>rdf: Description Framework document</li>
     * <li>rnc: Relax NG Schema document, Compact syntax</li>
     * <li>rng: Relax NG Schema document, XML syntax</li>
     * <li>rss: RSS file</li>
     * <li>rtf: Rich Text Format document</li>
     * <li>sav: SPSS Data</li>
     * <li>sit: StuffIt compressed archive file</li>
     * <li>sldm: Office PowerPoint 2007 macro-enabled slide</li>
     * <li>sldx: Office PowerPoint 2007 slide</li>
     * <li>snd: Amiga sound</li>
     * <li>sps: SPSS Script Syntax</li>
     * <li>sta: Stata data file</li>
     * <li>svg: Scalable Vector Graphics file</li>
     * <li>swf: Adobe Flash file</li>
     * <li>tar: Tape Archive file</li>
     * <li>tex: Tex file</li>
     * <li>tif, tiff: Tagged Image Format File</li>
     * <li>tsv: Tab-separated Values</li>
     * <li>txt: Plain text</li>
     * <li>ulw: MU-LAW (US telephony format)</li>
     * <li>vm: Velocity encoding</li>
     * <li>vrml: Virtual Reality Modeling Language file</li>
     * <li>vxml: VoiceXML source file</li>
     * <li>wadl: Web Application Description Language document</li>
     * <li>wav: Waveform audio</li>
     * <li>wrl: Plain text VRML file</li>
     * <li>xht, xhtml: XHTML document</li>
     * <li>xlam: Office Excel 2007 add-in</li>
     * <li>xls: Microsoft Excel document</li>
     * <li>xlsb: Office Excel 2007 binary workbook</li>
     * <li>xlsm: Office Excel 2007 macro-enabled workbook</li>
     * <li>xlsx: Microsoft Office Excel 2007 workbook</li>
     * <li>xltm: Office Excel 2007 macro-enabled workbook template</li>
     * <li>xltx: Office Excel 2007 template</li>
     * <li>xmi: XMI document</li>
     * <li>xml: XML document</li>
     * <li>xsd: W3C XML Schema document</li>
     * <li>xsl, xslt: XSL Transform file</li>
     * <li>xul: XML User Interface Language file</li>
     * <li>yaml: YAML text format</li>
     * <li>z: UNIX compressed archive file</li>
     * <li>zip: Zip archive</li>
     * </ul>
     */
    public void addCommonExtensions() {
        List<MetadataExtension> dm = new ArrayList<MetadataExtension>();

        ext(dm, "en", Language.ENGLISH);
        ext(dm, "es", Language.SPANISH);
        ext(dm, "fr", Language.FRENCH);

        // [ifndef gwt]
        ext(dm, "ascii", CharacterSet.US_ASCII);

        ext(dm, "ai", MediaType.APPLICATION_POSTSCRIPT);
        ext(dm, "atom", MediaType.APPLICATION_ATOM);
        ext(dm, "atomcat", MediaType.APPLICATION_ATOMPUB_CATEGORY);
        ext(dm, "atomsvc", MediaType.APPLICATION_ATOMPUB_SERVICE);
        ext(dm, "au", MediaType.AUDIO_BASIC);
        ext(dm, "bin", MediaType.APPLICATION_OCTET_STREAM);
        ext(dm, "bmp", MediaType.IMAGE_BMP);
        ext(dm, "class", MediaType.APPLICATION_JAVA);
        ext(dm, "css", MediaType.TEXT_CSS);
        ext(dm, "csv", MediaType.TEXT_CSV);
        ext(dm, "dat", MediaType.TEXT_DAT);
        ext(dm, "dib", MediaType.IMAGE_BMP);
        ext(dm, "doc", MediaType.APPLICATION_WORD);
        ext(dm, "docm", MediaType.APPLICATION_MSOFFICE_DOCM);
        ext(dm, "docx", MediaType.APPLICATION_MSOFFICE_DOCX);
        ext(dm, "dotm", MediaType.APPLICATION_MSOFFICE_DOTM);
        ext(dm, "dotx", MediaType.APPLICATION_MSOFFICE_DOTX);
        ext(dm, "dtd", MediaType.APPLICATION_XML_DTD);
        ext(dm, "ecore", MediaType.APPLICATION_ECORE);
        ext(dm, "eps", MediaType.APPLICATION_POSTSCRIPT);
        ext(dm, "exe", MediaType.APPLICATION_OCTET_STREAM);
        ext(dm, "fmt", Encoding.FREEMARKER);
        ext(dm, "form", MediaType.APPLICATION_WWW_FORM);
        ext(dm, "ftl", Encoding.FREEMARKER, true);
        ext(dm, "gif", MediaType.IMAGE_GIF);
        ext(dm, "gwt", MediaType.APPLICATION_JAVA_OBJECT_GWT);
        ext(dm, "hqx", MediaType.APPLICATION_MAC_BINHEX40);
        ext(dm, "ico", MediaType.IMAGE_ICON);
        ext(dm, "jad", MediaType.TEXT_J2ME_APP_DESCRIPTOR);
        ext(dm, "jar", MediaType.APPLICATION_JAVA_ARCHIVE);
        ext(dm, "java", MediaType.TEXT_PLAIN);
        ext(dm, "jnlp", MediaType.APPLICATION_JNLP);
        ext(dm, "jpe", MediaType.IMAGE_JPEG);
        ext(dm, "jpeg", MediaType.IMAGE_JPEG);
        ext(dm, "jpg", MediaType.IMAGE_JPEG);
        ext(dm, "js", MediaType.APPLICATION_JAVASCRIPT);
        ext(dm, "jsf", MediaType.TEXT_PLAIN);
        ext(dm, "kar", MediaType.AUDIO_MIDI);
        ext(dm, "latex", MediaType.APPLICATION_LATEX);
        ext(dm, "latin1", CharacterSet.ISO_8859_1);
        ext(dm, "mac", CharacterSet.MACINTOSH);
        ext(dm, "man", MediaType.APPLICATION_TROFF_MAN);
        ext(dm, "mathml", MediaType.APPLICATION_MATHML);
        ext(dm, "mid", MediaType.AUDIO_MIDI);
        ext(dm, "midi", MediaType.AUDIO_MIDI);
        ext(dm, "mov", MediaType.VIDEO_QUICKTIME);
        ext(dm, "mp2", MediaType.AUDIO_MPEG);
        ext(dm, "mp3", MediaType.AUDIO_MPEG);
        ext(dm, "mp4", MediaType.VIDEO_MP4);
        ext(dm, "mpe", MediaType.VIDEO_MPEG);
        ext(dm, "mpeg", MediaType.VIDEO_MPEG);
        ext(dm, "mpg", MediaType.VIDEO_MPEG);
        ext(dm, "n3", MediaType.TEXT_RDF_N3);
        ext(dm, "nt", MediaType.TEXT_PLAIN);
        ext(dm, "odb", MediaType.APPLICATION_OPENOFFICE_ODB);
        ext(dm, "odc", MediaType.APPLICATION_OPENOFFICE_ODC);
        ext(dm, "odf", MediaType.APPLICATION_OPENOFFICE_ODF);
        ext(dm, "odi", MediaType.APPLICATION_OPENOFFICE_ODI);
        ext(dm, "odm", MediaType.APPLICATION_OPENOFFICE_ODM);
        ext(dm, "odg", MediaType.APPLICATION_OPENOFFICE_ODG);
        ext(dm, "odp", MediaType.APPLICATION_OPENOFFICE_ODP);
        ext(dm, "ods", MediaType.APPLICATION_OPENOFFICE_ODS);
        ext(dm, "odt", MediaType.APPLICATION_OPENOFFICE_ODT);
        ext(dm, "onetoc", MediaType.APPLICATION_MSOFFICE_ONETOC);
        ext(dm, "onetoc2", MediaType.APPLICATION_MSOFFICE_ONETOC2);
        ext(dm, "otg", MediaType.APPLICATION_OPENOFFICE_OTG);
        ext(dm, "oth", MediaType.APPLICATION_OPENOFFICE_OTH);
        ext(dm, "otp", MediaType.APPLICATION_OPENOFFICE_OTP);
        ext(dm, "ots", MediaType.APPLICATION_OPENOFFICE_OTS);
        ext(dm, "ott", MediaType.APPLICATION_OPENOFFICE_OTT);
        ext(dm, "oxt", MediaType.APPLICATION_OPENOFFICE_OXT);
        ext(dm, "pdf", MediaType.APPLICATION_PDF);
        ext(dm, "png", MediaType.IMAGE_PNG);
        ext(dm, "potx", MediaType.APPLICATION_MSOFFICE_POTX);
        ext(dm, "potm", MediaType.APPLICATION_MSOFFICE_POTM);
        ext(dm, "ppam", MediaType.APPLICATION_MSOFFICE_PPAM);
        ext(dm, "pps", MediaType.APPLICATION_POWERPOINT);
        ext(dm, "ppsm", MediaType.APPLICATION_MSOFFICE_PPSM);
        ext(dm, "ppsx", MediaType.APPLICATION_MSOFFICE_PPSX);
        ext(dm, "ppt", MediaType.APPLICATION_POWERPOINT);
        ext(dm, "pptm", MediaType.APPLICATION_MSOFFICE_PPTM);
        ext(dm, "pptx", MediaType.APPLICATION_MSOFFICE_PPTX);
        ext(dm, "ps", MediaType.APPLICATION_POSTSCRIPT);
        ext(dm, "qt", MediaType.VIDEO_QUICKTIME);
        ext(dm, "rdf", MediaType.APPLICATION_RDF_XML);
        ext(dm, "rnc", MediaType.APPLICATION_RELAXNG_COMPACT);
        ext(dm, "rng", MediaType.APPLICATION_RELAXNG_XML);
        ext(dm, "rss", MediaType.APPLICATION_RSS);
        ext(dm, "rtf", MediaType.APPLICATION_RTF);
        ext(dm, "sav", MediaType.APPLICATION_SPSS_SAV);
        ext(dm, "sit", MediaType.APPLICATION_STUFFIT);
        ext(dm, "sldm", MediaType.APPLICATION_MSOFFICE_SLDM);
        ext(dm, "sldx", MediaType.APPLICATION_MSOFFICE_SLDX);
        ext(dm, "snd", MediaType.AUDIO_BASIC);
        ext(dm, "sps", MediaType.APPLICATION_SPSS_SPS);
        ext(dm, "sta", MediaType.APPLICATION_STATA_STA);
        ext(dm, "svg", MediaType.IMAGE_SVG);
        ext(dm, "swf", MediaType.APPLICATION_FLASH);
        ext(dm, "tar", MediaType.APPLICATION_TAR);
        ext(dm, "tex", MediaType.APPLICATION_TEX);
        ext(dm, "tif", MediaType.IMAGE_TIFF);
        ext(dm, "tiff", MediaType.IMAGE_TIFF);
        ext(dm, "tsv", MediaType.TEXT_TSV);
        ext(dm, "ulw", MediaType.AUDIO_BASIC);
        ext(dm, "utf16", CharacterSet.UTF_16);
        ext(dm, "utf8", CharacterSet.UTF_8);
        ext(dm, "vm", Encoding.VELOCITY);
        ext(dm, "vrml", MediaType.MODEL_VRML);
        ext(dm, "vxml", MediaType.APPLICATION_VOICEXML);
        ext(dm, "wadl", MediaType.APPLICATION_WADL);
        ext(dm, "wav", MediaType.AUDIO_WAV);
        ext(dm, "win", CharacterSet.WINDOWS_1252);
        ext(dm, "wrl", MediaType.MODEL_VRML);
        ext(dm, "xht", MediaType.APPLICATION_XHTML);
        ext(dm, "xls", MediaType.APPLICATION_EXCEL);
        ext(dm, "xlsx", MediaType.APPLICATION_MSOFFICE_XLSX);
        ext(dm, "xlsm", MediaType.APPLICATION_MSOFFICE_XLSM);
        ext(dm, "xltx", MediaType.APPLICATION_MSOFFICE_XLTX);
        ext(dm, "xltm", MediaType.APPLICATION_MSOFFICE_XLTM);
        ext(dm, "xlsb", MediaType.APPLICATION_MSOFFICE_XLSB);
        ext(dm, "xlam", MediaType.APPLICATION_MSOFFICE_XLAM);
        ext(dm, "xmi", MediaType.APPLICATION_XMI);
        ext(dm, "xsd", MediaType.APPLICATION_W3C_SCHEMA);
        ext(dm, "xsl", MediaType.APPLICATION_W3C_XSLT);
        ext(dm, "xslt", MediaType.APPLICATION_W3C_XSLT);
        ext(dm, "xul", MediaType.APPLICATION_XUL);
        ext(dm, "yaml", MediaType.APPLICATION_YAML);
        ext(dm, "yaml", MediaType.TEXT_YAML);
        ext(dm, "z", MediaType.APPLICATION_COMPRESS);
        ext(dm, "zip", MediaType.APPLICATION_ZIP);
        // [enddef]
        ext(dm, "htm", MediaType.TEXT_HTML);
        ext(dm, "html", MediaType.TEXT_HTML);
        ext(dm, "json", MediaType.APPLICATION_JSON);
        ext(dm, "jsonsmile", MediaType.APPLICATION_JSON_SMILE);
        ext(dm, "txt", MediaType.TEXT_PLAIN, true);
        ext(dm, "xhtml", MediaType.APPLICATION_XHTML);
        ext(dm, "xml", MediaType.TEXT_XML);
        ext(dm, "xml", MediaType.APPLICATION_XML);

        // Add all those mappings
        this.mappings.addAll(dm);
    }

    /**
     * Maps an extension to some metadata (media type, language or character
     * set) to an extension.
     * 
     * @param extension
     *            The extension name.
     * @param metadata
     *            The metadata to map.
     */
    public void addExtension(String extension, Metadata metadata) {
        addExtension(extension, metadata, false);
    }

    /**
     * Maps an extension to some metadata (media type, language or character
     * set) to an extension.
     * 
     * @param extension
     *            The extension name.
     * @param metadata
     *            The metadata to map.
     * @param preferred
     *            indicates if this mapping is the preferred one.
     */
    public void addExtension(String extension, Metadata metadata,
            boolean preferred) {
        if (preferred) {
            // Add the mapping at the beginning of the list
            this.mappings.add(0, new MetadataExtension(extension, metadata));
        } else {
            // Add the mapping at the end of the list
            this.mappings.add(new MetadataExtension(extension, metadata));
        }
    }

    /**
     * clears the mappings for all extensions.
     */
    public void clearExtensions() {
        this.mappings.clear();
    }

    /**
     * Creates a new extension mapping.
     * 
     * @param extensions
     *            The extensions list to update.
     * @param extension
     *            The extension name.
     * @param metadata
     *            The associated metadata.
     * @param preferred
     *            indicates if this mapping is the preferred one.
     * @return The new extension mapping.
     */
    private void ext(List<MetadataExtension> extensions, String extension,
            Metadata metadata) {
        ext(extensions, extension, metadata, false);
    }

    /**
     * Creates a new extension mapping.
     * 
     * @param extensions
     *            The extensions list to update.
     * @param extension
     *            The extension name.
     * @param metadata
     *            The associated metadata.
     * @param preferred
     *            indicates if this mapping is the preferred one.
     * @return The new extension mapping.
     */
    private void ext(List<MetadataExtension> extensions, String extension,
            Metadata metadata, boolean preferred) {
        if (preferred) {
            // Add the mapping at the beginning of the list
            extensions.add(0, new MetadataExtension(extension, metadata));
        } else {
            // Add the mapping at the end of the list
            extensions.add(new MetadataExtension(extension, metadata));
        }
    }

    /**
     * Return the ordered list of extension names mapped to character set.
     * 
     * @return The ordered list of extension names mapped to character set.
     */
    public List<String> getAllCharacterSetExtensionNames() {
        List<String> result = new ArrayList<String>();

        for (MetadataExtension mapping : this.mappings) {
            if ((mapping.getMetadata() instanceof CharacterSet)
                    && !result.contains(mapping.getName())) {
                result.add(mapping.getName());
            }
        }

        return result;
    }

    /**
     * Returns all the character sets associated to this extension. It returns
     * null if the extension was not declared.
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The list of character sets associated to this extension.
     */
    public List<CharacterSet> getAllCharacterSets(String extension) {
        List<CharacterSet> result = null;

        if (extension != null) {
            // Look for all registered convenient mapping.
            for (MetadataExtension metadataExtension : this.mappings) {
                if (extension.equals(metadataExtension.getName())
                        && (metadataExtension.getMetadata() instanceof CharacterSet)) {
                    if (result == null) {
                        result = new ArrayList<CharacterSet>();
                    }

                    result.add(metadataExtension.getCharacterSet());
                }
            }
        }

        return result;
    }

    /**
     * Return the ordered list of extension names mapped to encodings.
     * 
     * @return The ordered list of extension names mapped to encodings.
     */
    public List<String> getAllEncodingExtensionNames() {
        List<String> result = new ArrayList<String>();

        for (MetadataExtension mapping : this.mappings) {
            if ((mapping.getMetadata() instanceof Encoding)
                    && !result.contains(mapping.getName())) {
                result.add(mapping.getName());
            }
        }

        return result;
    }

    /**
     * Return the ordered list of extension names.
     * 
     * @return The ordered list of extension names.
     */
    public List<String> getAllExtensionNames() {
        List<String> result = new ArrayList<String>();

        for (MetadataExtension mapping : this.mappings) {
            if (!result.contains(mapping.getName())) {
                result.add(mapping.getName());
            }
        }

        return result;
    }

    /**
     * Return the ordered list of extension names mapped to languages.
     * 
     * @return The ordered list of extension names mapped to languages.
     */
    public List<String> getAllLanguageExtensionNames() {
        List<String> result = new ArrayList<String>();

        for (MetadataExtension mapping : this.mappings) {
            if ((mapping.getMetadata() instanceof Language)
                    && !result.contains(mapping.getName())) {
                result.add(mapping.getName());
            }
        }

        return result;
    }

    /**
     * Returns all the languages associated to this extension. It returns null
     * if the extension was not declared.
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The list of languages associated to this extension.
     */
    public List<Language> getAllLanguages(String extension) {
        List<Language> result = null;

        if (extension != null) {
            // Look for all registered convenient mapping.
            for (MetadataExtension metadataExtension : this.mappings) {
                if (extension.equals(metadataExtension.getName())
                        && (metadataExtension.getMetadata() instanceof Language)) {
                    if (result == null) {
                        result = new ArrayList<Language>();
                    }

                    result.add(metadataExtension.getLanguage());
                }
            }
        }

        return result;
    }

    /**
     * Return the ordered list of extension names mapped to media types.
     * 
     * @return The ordered list of extension names mapped to media types.
     */
    public List<String> getAllMediaTypeExtensionNames() {
        List<String> result = new ArrayList<String>();

        for (MetadataExtension mapping : this.mappings) {
            if ((mapping.getMetadata() instanceof MediaType)
                    && !result.contains(mapping.getName())) {
                result.add(mapping.getName());
            }
        }

        return result;
    }

    /**
     * Returns all the media types associated to this extension. It returns null
     * if the extension was not declared.
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The list of media type associated to this extension.
     */
    public List<MediaType> getAllMediaTypes(String extension) {
        List<MediaType> result = null;

        if (extension != null) {
            // Look for all registered convenient mapping.
            for (MetadataExtension metadataExtension : this.mappings) {
                if (extension.equals(metadataExtension.getName())
                        && (metadataExtension.getMetadata() instanceof MediaType)) {
                    if (result == null) {
                        result = new ArrayList<MediaType>();
                    }

                    result.add(metadataExtension.getMediaType());
                }
            }
        }

        return result;
    }

    /**
     * Returns all the metadata associated to this extension. It returns null if
     * the extension was not declared.
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The list of metadata associated to this extension.
     */
    public List<Metadata> getAllMetadata(String extension) {
        List<Metadata> result = null;

        if (extension != null) {
            // Look for all registered convenient mapping.
            for (MetadataExtension metadataExtension : this.mappings) {
                if (extension.equals(metadataExtension.getName())) {
                    if (result == null) {
                        result = new ArrayList<Metadata>();
                    }

                    result.add(metadataExtension.getMetadata());
                }
            }
        }

        return result;
    }

    /**
     * Returns the character set associated to this extension. It returns null
     * if the extension was not declared of it is corresponds to another type of
     * medatata such as a media type. If several metadata are associated to the
     * same extension then only the first matching metadata is returned.
     * 
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The character set associated to this extension.
     */
    public CharacterSet getCharacterSet(String extension) {
        // [ifndef gwt] instruction
        return getMetadata(extension, CharacterSet.class);
        // [ifdef gwt] uncomment
        // Metadata metadata = getMetadata(extension);
        // if (metadata instanceof CharacterSet) {
        // return (CharacterSet) metadata;
        // } else {
        // return null;
        // }
        // [enddef]
    }

    /**
     * Returns the default character set for textual representations.
     * 
     * @return The default character set for textual representations.
     */
    public CharacterSet getDefaultCharacterSet() {
        return this.defaultCharacterSet;
    }

    /**
     * Returns the default encoding for representations.
     * 
     * @return The default encoding for representations.
     */
    public Encoding getDefaultEncoding() {
        return this.defaultEncoding;
    }

    /**
     * Returns the default language for representations.
     * 
     * @return The default language for representations.
     */
    public Language getDefaultLanguage() {
        return this.defaultLanguage;
    }

    /**
     * Returns the default media type for representations.
     * 
     * @return The default media type for representations.
     */
    public MediaType getDefaultMediaType() {
        return this.defaultMediaType;
    }

    /**
     * Returns the encoding associated to this extension. It returns null if the
     * extension was not declared of it is corresponds to another type of
     * medatata such as a media type. If several metadata are associated to the
     * same extension then only the first matching metadata is returned.
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The encoding associated to this extension.
     */
    public Encoding getEncoding(String extension) {
        // [ifndef gwt] instruction
        return getMetadata(extension, Encoding.class);
        // [ifdef gwt] uncomment
        // Metadata metadata = getMetadata(extension);
        // if (metadata instanceof Encoding) {
        // return (Encoding) metadata;
        // } else {
        // return null;
        // }
        // [enddef]
    }

    /**
     * Returns the first extension mapping to this metadata.
     * 
     * @param metadata
     *            The metadata to find.
     * @return The first extension mapping to this metadata.
     */
    public String getExtension(Metadata metadata) {
        if (metadata != null) {
            // Look for the first registered convenient mapping.
            for (final MetadataExtension metadataExtension : this.mappings) {
                if (metadata.equals(metadataExtension.getMetadata())) {
                    return metadataExtension.getName();
                }
            }
        }
        return null;
    }

    /**
     * Returns the language associated to this extension. It returns null if the
     * extension was not declared of it is corresponds to another type of
     * medatata such as a media type. If several metadata are associated to the
     * same extension then only the first matching metadata is returned.
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The language associated to this extension.
     */
    public Language getLanguage(String extension) {
        // [ifndef gwt] instruction
        return getMetadata(extension, Language.class);
        // [ifdef gwt] uncomment
        // Metadata metadata = getMetadata(extension);
        // if (metadata instanceof Language) {
        // return (Language) metadata;
        // } else {
        // return null;
        // }
        // [enddef]
    }

    /**
     * Returns the mediatype associated to this extension. It returns null if
     * the extension was not declared of it is corresponds to another type of
     * medatata such as a language. If several metadata are associated to the
     * same extension (ex: 'xml' for both 'text/xml' and 'application/xml') then
     * only the first matching metadata is returned.
     * 
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The media type associated to this extension.
     */
    public MediaType getMediaType(String extension) {
        // [ifndef gwt] instruction
        return getMetadata(extension, MediaType.class);
        // [ifdef gwt] uncomment
        // Metadata metadata = getMetadata(extension);
        // if (metadata instanceof MediaType) {
        // return (MediaType) metadata;
        // } else {
        // return null;
        // }
        // [enddef]
    }

    /**
     * Returns the metadata associated to this extension. It returns null if the
     * extension was not declared. If several metadata are associated to the
     * same extension (ex: 'xml' for both 'text/xml' and 'application/xml') then
     * only the first matching metadata is returned.
     * 
     * @param extension
     *            The extension name without any delimiter.
     * @return The metadata associated to this extension.
     */
    public Metadata getMetadata(String extension) {
        if (extension != null) {
            // Look for the first registered convenient mapping.
            for (final MetadataExtension metadataExtension : this.mappings) {
                if (extension.equals(metadataExtension.getName())) {
                    return metadataExtension.getMetadata();
                }
            }
        }

        return null;
    }

    // [ifndef gwt] method
    /**
     * Returns the metadata associated to this extension. It returns null if the
     * extension was not declared or is not of the target metadata type.
     * 
     * @param <T>
     * @param extension
     *            The extension name without any delimiter.
     * @param metadataType
     *            The target metadata type.
     * @return The metadata associated to this extension.
     */
    public <T extends Metadata> T getMetadata(String extension,
            Class<T> metadataType) {
        Metadata metadata = getMetadata(extension);

        if (metadata != null
                && metadataType.isAssignableFrom(metadata.getClass())) {
            return metadataType.cast(metadata);
        }

        return null;
    }

    /**
     * Sets the default character set for local representations.
     * 
     * @param defaultCharacterSet
     *            The default character set for local representations.
     */
    public void setDefaultCharacterSet(CharacterSet defaultCharacterSet) {
        this.defaultCharacterSet = defaultCharacterSet;
    }

    /**
     * Sets the default encoding for local representations.
     * 
     * @param defaultEncoding
     *            The default encoding for local representations.
     */
    public void setDefaultEncoding(Encoding defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Sets the default language for local representations.
     * 
     * @param defaultLanguage
     *            The default language for local representations.
     */
    public void setDefaultLanguage(Language defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * Sets the default media type for local representations.
     * 
     * @param defaultMediaType
     *            The default media type for local representations.
     */
    public void setDefaultMediaType(MediaType defaultMediaType) {
        this.defaultMediaType = defaultMediaType;
    }

}
