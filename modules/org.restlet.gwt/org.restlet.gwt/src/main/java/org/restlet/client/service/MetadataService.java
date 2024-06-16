/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.client.data.CharacterSet;
import org.restlet.client.data.Encoding;
import org.restlet.client.data.Language;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Metadata;
import org.restlet.client.engine.application.MetadataExtension;

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
         this.defaultMediaType = MediaType.APPLICATION_JSON;
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
         Metadata metadata = getMetadata(extension);
         if (metadata instanceof CharacterSet) {
         return (CharacterSet) metadata;
         } else {
         return null;
         }
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
         Metadata metadata = getMetadata(extension);
         if (metadata instanceof Encoding) {
         return (Encoding) metadata;
         } else {
         return null;
         }
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
         Metadata metadata = getMetadata(extension);
         if (metadata instanceof Language) {
         return (Language) metadata;
         } else {
         return null;
         }
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
         Metadata metadata = getMetadata(extension);
         if (metadata instanceof MediaType) {
         return (MediaType) metadata;
         } else {
         return null;
         }
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
