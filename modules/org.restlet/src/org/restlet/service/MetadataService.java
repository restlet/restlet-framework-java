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

package org.restlet.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.engine.service.MetadataExtension;

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
    /** The default encoding for local representations. */
    private volatile Encoding defaultEncoding;

    /** The default language for local representations. */
    private volatile Language defaultLanguage;

    /** The default media type for local representations. */
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
        this.defaultEncoding = Encoding.IDENTITY;
        this.defaultLanguage = Language.ENGLISH_US;
        this.defaultMediaType = MediaType.APPLICATION_OCTET_STREAM;
        this.mappings = new CopyOnWriteArrayList<MetadataExtension>();
        addCommonExtensions();
    }

    /**
     * Adds a common list of associations from extensions to metadata. The list
     * of languages extensions:<br>
     * <ul>
     * <li>en: English</li>
     * <li>es: Spanish</li>
     * <li>fr: French</li>
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
     * <li>gif: GIF image</li>
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
     * <li>xml: XML document</li>
     * <li>xsd: W3C XML Schema document</li>
     * <li>xslt: XSL Transform file</li>
     * <li>xul: XML User Interface Language file</li>
     * <li>z: UNIX compressed archive file</li>
     * <li>zip: Zip archive</li>
     * </ul>
     */
    public void addCommonExtensions() {
        addExtension("en", Language.ENGLISH);
        addExtension("es", Language.SPANISH);
        addExtension("fr", Language.FRENCH);

        addExtension("ai", MediaType.APPLICATION_POSTSCRIPT);
        addExtension("atom", MediaType.APPLICATION_ATOM);
        addExtension("au", MediaType.AUDIO_BASIC);
        addExtension("bin", MediaType.APPLICATION_OCTET_STREAM);
        addExtension("bmp", MediaType.IMAGE_BMP);
        addExtension("class", MediaType.APPLICATION_JAVA);
        addExtension("css", MediaType.TEXT_CSS);
        addExtension("csv", MediaType.TEXT_CSV);
        addExtension("dat", MediaType.TEXT_DAT);
        addExtension("dib", MediaType.IMAGE_BMP);
        addExtension("doc", MediaType.APPLICATION_WORD);
        addExtension("docm", MediaType.APPLICATION_MSOFFICE_DOCM);
        addExtension("docx", MediaType.APPLICATION_MSOFFICE_DOCX);
        addExtension("dotm", MediaType.APPLICATION_MSOFFICE_DOTM);
        addExtension("dotx", MediaType.APPLICATION_MSOFFICE_DOTX);
        addExtension("dtd", MediaType.APPLICATION_XML_DTD);
        addExtension("eps", MediaType.APPLICATION_POSTSCRIPT);
        addExtension("exe", MediaType.APPLICATION_OCTET_STREAM);
        addExtension("fmt", Encoding.FREEMARKER);
        addExtension("form", MediaType.APPLICATION_WWW_FORM);
        addExtension("gif", MediaType.IMAGE_GIF);
        addExtension("hqx", MediaType.APPLICATION_MAC_BINHEX40);
        addExtension("htm", MediaType.TEXT_HTML);
        addExtension("html", MediaType.TEXT_HTML);
        addExtension("ico", MediaType.IMAGE_ICON);
        addExtension("jad", MediaType.TEXT_J2ME_APP_DESCRIPTOR);
        addExtension("jar", MediaType.APPLICATION_JAVA_ARCHIVE);
        addExtension("java", MediaType.TEXT_PLAIN);
        addExtension("jnlp", MediaType.APPLICATION_JNLP);
        addExtension("jpe", MediaType.IMAGE_JPEG);
        addExtension("jpeg", MediaType.IMAGE_JPEG);
        addExtension("jpg", MediaType.IMAGE_JPEG);
        addExtension("js", MediaType.APPLICATION_JAVASCRIPT);
        addExtension("jsf", MediaType.TEXT_PLAIN);
        addExtension("json", MediaType.APPLICATION_JSON);
        addExtension("kar", MediaType.AUDIO_MIDI);
        addExtension("latex", MediaType.APPLICATION_LATEX);
        addExtension("man", MediaType.APPLICATION_TROFF_MAN);
        addExtension("mathml", MediaType.APPLICATION_MATHML);
        addExtension("mid", MediaType.AUDIO_MIDI);
        addExtension("midi", MediaType.AUDIO_MIDI);
        addExtension("mov", MediaType.VIDEO_QUICKTIME);
        addExtension("mp2", MediaType.AUDIO_MPEG);
        addExtension("mp3", MediaType.AUDIO_MPEG);
        addExtension("mp4", MediaType.VIDEO_MP4);
        addExtension("mpe", MediaType.VIDEO_MPEG);
        addExtension("mpeg", MediaType.VIDEO_MPEG);
        addExtension("mpg", MediaType.VIDEO_MPEG);
        addExtension("n3", MediaType.TEXT_RDF_N3);
        addExtension("nt", MediaType.TEXT_PLAIN);
        addExtension("odb", MediaType.APPLICATION_OPENOFFICE_ODB);
        addExtension("odc", MediaType.APPLICATION_OPENOFFICE_ODC);
        addExtension("odf", MediaType.APPLICATION_OPENOFFICE_ODF);
        addExtension("odi", MediaType.APPLICATION_OPENOFFICE_ODI);
        addExtension("odm", MediaType.APPLICATION_OPENOFFICE_ODM);
        addExtension("odg", MediaType.APPLICATION_OPENOFFICE_ODG);
        addExtension("odp", MediaType.APPLICATION_OPENOFFICE_ODP);
        addExtension("ods", MediaType.APPLICATION_OPENOFFICE_ODS);
        addExtension("odt", MediaType.APPLICATION_OPENOFFICE_ODT);
        addExtension("onetoc", MediaType.APPLICATION_MSOFFICE_ONETOC);
        addExtension("onetoc2", MediaType.APPLICATION_MSOFFICE_ONETOC2);
        addExtension("otg", MediaType.APPLICATION_OPENOFFICE_OTG);
        addExtension("oth", MediaType.APPLICATION_OPENOFFICE_OTH);
        addExtension("otp", MediaType.APPLICATION_OPENOFFICE_OTP);
        addExtension("ots", MediaType.APPLICATION_OPENOFFICE_OTS);
        addExtension("ott", MediaType.APPLICATION_OPENOFFICE_OTT);
        addExtension("oxt", MediaType.APPLICATION_OPENOFFICE_OXT);
        addExtension("pdf", MediaType.APPLICATION_PDF);
        addExtension("png", MediaType.IMAGE_PNG);
        addExtension("potx", MediaType.APPLICATION_MSOFFICE_POTX);
        addExtension("potm", MediaType.APPLICATION_MSOFFICE_POTM);
        addExtension("ppam", MediaType.APPLICATION_MSOFFICE_PPAM);
        addExtension("pps", MediaType.APPLICATION_POWERPOINT);
        addExtension("ppsm", MediaType.APPLICATION_MSOFFICE_PPSM);
        addExtension("ppsx", MediaType.APPLICATION_MSOFFICE_PPSX);
        addExtension("ppt", MediaType.APPLICATION_POWERPOINT);
        addExtension("pptm", MediaType.APPLICATION_MSOFFICE_PPTM);
        addExtension("pptx", MediaType.APPLICATION_MSOFFICE_PPTX);
        addExtension("ps", MediaType.APPLICATION_POSTSCRIPT);
        addExtension("qt", MediaType.VIDEO_QUICKTIME);
        addExtension("rdf", MediaType.APPLICATION_RDF_XML);
        addExtension("rnc", MediaType.APPLICATION_RELAXNG_COMPACT);
        addExtension("rng", MediaType.APPLICATION_RELAXNG_XML);
        addExtension("rss", MediaType.APPLICATION_RSS);
        addExtension("rtf", MediaType.APPLICATION_RTF);
        addExtension("sav", MediaType.APPLICATION_SPSS_SAV);
        addExtension("sit", MediaType.APPLICATION_STUFFIT);
        addExtension("sldm", MediaType.APPLICATION_MSOFFICE_SLDM);
        addExtension("sldx", MediaType.APPLICATION_MSOFFICE_SLDX);
        addExtension("snd", MediaType.AUDIO_BASIC);
        addExtension("sps", MediaType.APPLICATION_SPSS_SPS);
        addExtension("sta", MediaType.APPLICATION_STATA_STA);
        addExtension("svg", MediaType.IMAGE_SVG);
        addExtension("swf", MediaType.APPLICATION_FLASH);
        addExtension("tar", MediaType.APPLICATION_TAR);
        addExtension("tex", MediaType.APPLICATION_TEX);
        addExtension("tif", MediaType.IMAGE_TIFF);
        addExtension("tiff", MediaType.IMAGE_TIFF);
        addExtension("tsv", MediaType.TEXT_TSV);
        addExtension("txt", MediaType.TEXT_PLAIN, true);
        addExtension("ulw", MediaType.AUDIO_BASIC);
        addExtension("vm", Encoding.VELOCITY);
        addExtension("vrml", MediaType.MODEL_VRML);
        addExtension("vxml", MediaType.APPLICATION_VOICEXML);
        addExtension("wadl", MediaType.APPLICATION_WADL);
        addExtension("wav", MediaType.AUDIO_WAV);
        addExtension("wrl", MediaType.MODEL_VRML);
        addExtension("xht", MediaType.APPLICATION_XHTML);
        addExtension("xhtml", MediaType.APPLICATION_XHTML);
        addExtension("xls", MediaType.APPLICATION_EXCEL);
        addExtension("xlsx", MediaType.APPLICATION_MSOFFICE_XLSX);
        addExtension("xlsm", MediaType.APPLICATION_MSOFFICE_XLSM);
        addExtension("xltx", MediaType.APPLICATION_MSOFFICE_XLTX);
        addExtension("xltm", MediaType.APPLICATION_MSOFFICE_XLTM);
        addExtension("xlsb", MediaType.APPLICATION_MSOFFICE_XLSB);
        addExtension("xlam", MediaType.APPLICATION_MSOFFICE_XLAM);
        addExtension("xml", MediaType.TEXT_XML);
        addExtension("xml", MediaType.APPLICATION_XML);
        addExtension("xsd", MediaType.APPLICATION_W3C_SCHEMA);
        addExtension("xslt", MediaType.APPLICATION_W3C_XSLT);
        addExtension("xul", MediaType.APPLICATION_XUL);
        addExtension("z", MediaType.APPLICATION_COMPRESS);
        addExtension("zip", MediaType.APPLICATION_ZIP);
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
            for (final MetadataExtension metadataExtension : this.mappings) {
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
     * Returns the default encoding for local representations.
     * 
     * @return The default encoding for local representations.
     */
    public Encoding getDefaultEncoding() {
        return this.defaultEncoding;
    }

    /**
     * Returns the default language for local representations.
     * 
     * @return The default language for local representations.
     */
    public Language getDefaultLanguage() {
        return this.defaultLanguage;
    }

    /**
     * Returns the default media type for local representations.
     * 
     * @return The default media type for local representations.
     */
    public MediaType getDefaultMediaType() {
        return this.defaultMediaType;
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
