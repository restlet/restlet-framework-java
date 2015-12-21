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

package org.restlet.ext.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * XML writer doing the opposite work of a SAX-based XML reader. The
 * implementation is based on the work of David Megginson, the creator of SAX
 * who placed the original code in the public domain.
 * 
 * <p>
 * This class can be used by itself or as part of a SAX event stream: it takes
 * as input a series of SAX2 ContentHandler events and uses the information in
 * those events to write an XML document. Since this class is a filter, it can
 * also pass the events on down a filter chain for further processing (you can
 * use the XmlWriter to take a snapshot of the current state at any point in a
 * filter chain), and it can be used directly as a ContentHandler for a SAX2
 * XMLReader.
 * </p>
 * 
 * <p>
 * The client creates a document by invoking the methods for standard SAX2
 * events, always beginning with the {@link #startDocument startDocument} method
 * and ending with the {@link #endDocument endDocument} method. There are
 * convenience methods provided so that clients to not have to create empty
 * attribute lists or provide empty strings as parameters; for example, the
 * method invocation
 * </p>
 * 
 * <pre>
 * w.startElement(&quot;foo&quot;);
 * </pre>
 * 
 * <p>
 * is equivalent to the regular SAX2 ContentHandler method
 * </p>
 * 
 * <pre>
 * w.startElement(&quot;&quot;, &quot;foo&quot;, &quot;&quot;, new AttributesImpl());
 * </pre>
 * 
 * <p>
 * Except that it is more efficient because it does not allocate a new empty
 * attribute list each time. The following code will send a simple XML document
 * to standard output:
 * </p>
 * 
 * <pre>
 * XmlWriter w = new XmlWriter();
 * 
 * w.startDocument();
 * w.startElement(&quot;greeting&quot;);
 * w.characters(&quot;Hello, world!&quot;);
 * w.endElement(&quot;greeting&quot;);
 * w.endDocument();
 * </pre>
 * 
 * <p>
 * The resulting document will look like this:
 * </p>
 * 
 * <pre>
 *           &lt;?xml version=&quot;1.0&quot; standalone='yes'?&gt;
 *          
 *           &lt;greeting&gt;Hello, world!&lt;/greeting&gt;
 * </pre>
 * 
 * <p>
 * In fact, there is an even simpler convenience method, <var>dataElement</var>,
 * designed for writing elements that contain only character data, so the code
 * to generate the document could be shortened to
 * </p>
 * 
 * <pre>
 * XmlWriter w = new XmlWriter();
 * 
 * w.startDocument();
 * w.dataElement(&quot;greeting&quot;, &quot;Hello, world!&quot;);
 * w.endDocument();
 * </pre>
 * 
 * <h2>Whitespace</h2>
 * 
 * <p>
 * According to the XML Recommendation, <em>all</em> whitespace in an XML
 * document is potentially significant to an application, so this class never
 * adds newlines or indentation. If you insert three elements in a row, as in
 * </p>
 * 
 * <pre>
 * w.dataElement(&quot;item&quot;, &quot;1&quot;);
 * w.dataElement(&quot;item&quot;, &quot;2&quot;);
 * w.dataElement(&quot;item&quot;, &quot;3&quot;);
 * </pre>
 * 
 * <p>
 * you will end up with
 * </p>
 * 
 * <pre>
 *           &lt;item&gt;1&lt;/item&gt;&lt;item&gt;3&lt;/item&gt;&lt;item&gt;3&lt;/item&gt;
 * </pre>
 * 
 * <p>
 * You need to invoke one of the <var>characters</var> methods explicitly to add
 * newlines or indentation. Alternatively, you can use the data format mode (set
 * the "dataFormat" property) which is optimized for writing purely
 * data-oriented (or field-oriented) XML, and does automatic linebreaks and
 * indentation (but does not support mixed content properly). See details below.
 * </p>
 * 
 * <h2>Namespace Support</h2>
 * 
 * <p>
 * The writer contains extensive support for XML Namespaces, so that a client
 * application does not have to keep track of prefixes and supply
 * <var>xmlns</var> attributes. By default, the XML writer will generate
 * Namespace declarations in the form _NS1, _NS2, etc., wherever they are
 * needed, as in the following example:
 * </p>
 * 
 * <pre>
 * w.startDocument();
 * w.emptyElement(&quot;http://www.foo.com/ns/&quot;, &quot;foo&quot;);
 * w.endDocument();
 * </pre>
 * 
 * <p>
 * The resulting document will look like this:
 * </p>
 * 
 * <pre>
 *           &lt;?xml version=&quot;1.0&quot; standalone='yes'?&gt;
 *          
 *           &lt;_NS1:foo xmlns:_NS1=&quot;http://www.foo.com/ns/&quot;/&gt;
 * </pre>
 * 
 * <p>
 * In many cases, document authors will prefer to choose their own prefixes
 * rather than using the (ugly) default names. The XML writer allows two methods
 * for selecting prefixes:
 * </p>
 * 
 * <ol>
 * <li>the qualified name</li>
 * <li>the {@link #setPrefix setPrefix} method.</li>
 * </ol>
 * 
 * <p>
 * Whenever the XML writer finds a new Namespace URI, it checks to see if a
 * qualified (prefixed) name is also available; if so it attempts to use the
 * name's prefix (as long as the prefix is not already in use for another
 * Namespace URI).
 * </p>
 * 
 * <p>
 * Before writing a document, the client can also pre-map a prefix to a
 * Namespace URI with the setPrefix method:
 * </p>
 * 
 * <pre>
 * w.setPrefix(&quot;http://www.foo.com/ns/&quot;, &quot;foo&quot;);
 * w.startDocument();
 * w.emptyElement(&quot;http://www.foo.com/ns/&quot;, &quot;foo&quot;);
 * w.endDocument();
 * </pre>
 * 
 * <p>
 * The resulting document will look like this:
 * </p>
 * 
 * <pre>
 *           &lt;?xml version=&quot;1.0&quot; standalone='yes'?&gt;
 *          
 *           &lt;foo:foo xmlns:foo=&quot;http://www.foo.com/ns/&quot;/&gt;
 * </pre>
 * 
 * <p>
 * The default Namespace simply uses an empty string as the prefix:
 * </p>
 * 
 * <pre>
 * w.setPrefix(&quot;http://www.foo.com/ns/&quot;, &quot;&quot;);
 * w.startDocument();
 * w.emptyElement(&quot;http://www.foo.com/ns/&quot;, &quot;foo&quot;);
 * w.endDocument();
 * </pre>
 * 
 * <p>
 * The resulting document will look like this:
 * </p>
 * 
 * <pre>
 *           &lt;?xml version=&quot;1.0&quot; standalone='yes'?&gt;
 *          
 *           &lt;foo xmlns=&quot;http://www.foo.com/ns/&quot;/&gt;
 * </pre>
 * 
 * <p>
 * By default, the XML writer will not declare a Namespace until it is actually
 * used. Sometimes, this approach will create a large number of Namespace
 * declarations, as in the following example:
 * </p>
 * 
 * <pre>
 *           &lt;xml version=&quot;1.0&quot; standalone='yes'?&gt;
 *          
 *           &lt;rdf:RDF xmlns:rdf=&quot;http://www.w3.org/1999/02/22-rdf-syntax-ns#&quot;&gt;
 *            &lt;rdf:Description about=&quot;http://www.foo.com/ids/books/12345&quot;&gt;
 *             &lt;dc:title xmlns:dc=&quot;http://www.purl.org/dc/&quot;&gt;A Dark Night&lt;/dc:title&gt;
 *             &lt;dc:creator xmlns:dc=&quot;http://www.purl.org/dc/&quot;&gt;Jane Smith&lt;/dc:title&gt;
 *             &lt;dc:date xmlns:dc=&quot;http://www.purl.org/dc/&quot;&gt;2000-09-09&lt;/dc:title&gt;
 *            &lt;/rdf:Description&gt;
 *           &lt;/rdf:RDF&gt;
 * </pre>
 * 
 * <p>
 * The "rdf" prefix is declared only once, because the RDF Namespace is used by
 * the root element and can be inherited by all of its descendants; the "dc"
 * prefix, on the other hand, is declared three times, because no higher element
 * uses the Namespace. To solve this problem, you can instruct the XML writer to
 * predeclare Namespaces on the root element even if they are not used there:
 * </p>
 * 
 * <pre>
 * w.forceNSDecl(&quot;http://www.purl.org/dc/&quot;);
 * </pre>
 * 
 * <p>
 * Now, the "dc" prefix will be declared on the root element even though it's
 * not needed there, and can be inherited by its descendants:
 * </p>
 * 
 * <pre>
 *           &lt;xml version=&quot;1.0&quot; standalone='yes'?&gt;
 *          
 *           &lt;rdf:RDF xmlns:rdf=&quot;http://www.w3.org/1999/02/22-rdf-syntax-ns#&quot;
 *                       xmlns:dc=&quot;http://www.purl.org/dc/&quot;&gt;
 *            &lt;rdf:Description about=&quot;http://www.foo.com/ids/books/12345&quot;&gt;
 *             &lt;dc:title&gt;A Dark Night&lt;/dc:title&gt;
 *             &lt;dc:creator&gt;Jane Smith&lt;/dc:title&gt;
 *             &lt;dc:date&gt;2000-09-09&lt;/dc:title&gt;
 *            &lt;/rdf:Description&gt;
 *           &lt;/rdf:RDF&gt;
 * </pre>
 * 
 * <p>
 * This approach is also useful for declaring Namespace prefixes that be used by
 * qualified names appearing in attribute values or character data.
 * </p>
 * 
 * <h2>Data Format</h2>
 * 
 * <p>
 * This mode, enabled by the "dataFormat" property, pretty-prints field-oriented
 * XML without mixed content. All added indentation and newlines will be passed
 * on down the filter chain (if any).
 * </p>
 * 
 * <p>
 * In general, all whitespace in an XML document is potentially significant, so
 * a general-purpose XML writing tool cannot add newlines or indentation.
 * </p>
 * 
 * <p>
 * There is, however, a large class of XML documents where information is
 * strictly fielded: each element contains either character data or other
 * elements, but not both. For this special case, it is possible for a writing
 * tool to provide automatic indentation and newlines without requiring extra
 * work from the user. Note that this class will likely not yield appropriate
 * results for document-oriented XML like XHTML pages, which mix character data
 * and elements together.
 * </p>
 * 
 * <p>
 * This writer mode will automatically place each start tag on a new line,
 * optionally indented if an indent step is provided (by default, there is no
 * indentation). If an element contains other elements, the end tag will also
 * appear on a new line with leading indentation. Consider, for example, the
 * following code:
 * </p>
 * 
 * <pre>
 * XmlWriter w = new XmlWriter();
 * w.setDataFormat(true);
 * w.setIndentStep(2);
 * w.startDocument();
 * w.startElement(&quot;Person&quot;);
 * w.dataElement(&quot;name&quot;, &quot;Jane Smith&quot;);
 * w.dataElement(&quot;date-of-birth&quot;, &quot;1965-05-23&quot;);
 * w.dataElement(&quot;citizenship&quot;, &quot;US&quot;);
 * w.endElement(&quot;Person&quot;);
 * w.endDocument();
 * </pre>
 * 
 * <p>
 * This code will produce the following document:
 * </p>
 * 
 * <pre>
 *           &lt;?xml version=&quot;1.0&quot; standalone='yes'?&gt;
 *          
 *           &lt;Person&gt;
 *             &lt;name&gt;Jane Smith&lt;/name&gt;
 *             &lt;date-of-birth&gt;1965-05-23&lt;/date-of-birth&gt;
 *             &lt;citizenship&gt;US&lt;/citizenship&gt;
 *           &lt;/Person&gt;
 * </pre>
 * 
 * @see org.xml.sax.XMLFilter
 * @see org.xml.sax.ContentHandler
 * @author David Megginson, Jerome Louvel (contact@restlet.com)
 */
public final class XmlWriter extends XMLFilterImpl {
    private static final Object SEEN_DATA = new Object();

    private static final Object SEEN_ELEMENT = new Object();

    private static final Object SEEN_NOTHING = new Object();

    private volatile boolean dataFormat = false;

    private volatile int depth = 0;

    /**
     * The document declarations table.
     */
    private volatile Map<String, String> doneDeclTable;

    /**
     * The element level.
     */
    private volatile int elementLevel = 0;

    /**
     * Constant representing empty attributes.
     */
    private final Attributes EMPTY_ATTS = new AttributesImpl();

    /**
     * The forced declarations table.
     */
    private volatile Map<String, Boolean> forcedDeclTable;

    private volatile int indentStep = 0;

    /**
     * The namespace support.
     */
    private volatile NamespaceSupport nsSupport;

    /**
     * The underlying writer.
     */
    private volatile Writer output;

    /**
     * The prefix counter.
     */
    private volatile int prefixCounter = 0;

    /**
     * The prefixes table.
     */
    private volatile Map<String, String> prefixTable;

    private volatile Object state = SEEN_NOTHING;

    private volatile Stack<Object> stateStack = new Stack<Object>();

    /**
     * Create a new XML writer.
     * <p>
     * Write to standard output.
     * </p>
     */
    public XmlWriter() {
        init(null);
    }

    /**
     * Constructor.
     * 
     * @param out
     *            The underlying output stream.
     */
    public XmlWriter(OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    /**
     * Constructor.
     * 
     * @param out
     *            The underlying output stream.
     */
    public XmlWriter(OutputStream out, Charset cs) {
        this(new OutputStreamWriter(out, cs));
    }

    /**
     * Constructor.
     * 
     * @param out
     *            The underlying output stream.
     */
    public XmlWriter(OutputStream out, CharsetEncoder enc) {
        this(new OutputStreamWriter(out, enc));
    }

    /**
     * Constructor.
     * 
     * @param out
     *            The underlying output stream.
     */
    public XmlWriter(OutputStream out, String charsetName)
            throws UnsupportedEncodingException {
        this(new OutputStreamWriter(out, charsetName));
    }

    /**
     * Create a new XML writer.
     * <p>
     * Write to the writer provided.
     * </p>
     * 
     * @param writer
     *            The output destination, or null to use standard output.
     */
    public XmlWriter(Writer writer) {
        init(writer);
    }

    /**
     * Create a new XML writer.
     * <p>
     * Use the specified XML reader as the parent.
     * </p>
     * 
     * @param xmlreader
     *            The parent in the filter chain, or null for no parent.
     */
    public XmlWriter(XMLReader xmlreader) {
        super(xmlreader);
        init(null);
    }

    /**
     * Create a new XML writer.
     * <p>
     * Use the specified XML reader as the parent, and write to the specified
     * writer.
     * </p>
     * 
     * @param xmlreader
     *            The parent in the filter chain, or null for no parent.
     * @param writer
     *            The output destination, or null to use standard output.
     */
    public XmlWriter(XMLReader xmlreader, Writer writer) {
        super(xmlreader);
        init(writer);
    }

    /**
     * Write character data. Pass the event on down the filter chain for further
     * processing.
     * 
     * @param ch
     *            The array of characters to write.
     * @param start
     *            The starting position in the array.
     * @param len
     *            The number of characters to write.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the characters, or if a
     *                restlet further down the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    private void characters(boolean dataFormat, char ch[], int start, int len)
            throws SAXException {
        if (dataFormat) {
            this.state = SEEN_DATA;
        }

        writeEsc(ch, start, len, false);
        super.characters(ch, start, len);
    }

    // //////////////////////////////////////////////////////////////////
    // Public methods.
    // //////////////////////////////////////////////////////////////////

    /**
     * Write a string of character data, with XML escaping.
     * <p>
     * This is a convenience method that takes an XML String, converts it to a
     * character array, then invokes {@link #characters(char[], int, int)}.
     * </p>
     * 
     * @param data
     *            The character data.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the string, or if a restlet
     *                further down the filter chain raises an exception.
     * @see #characters(char[], int, int)
     */
    private void characters(boolean dataFormat, String data)
            throws SAXException {
        final char ch[] = data.toCharArray();
        characters(dataFormat, ch, 0, ch.length);
    }

    /**
     * Write character data. Pass the event on down the filter chain for further
     * processing.
     * 
     * @param ch
     *            The array of characters to write.
     * @param start
     *            The starting position in the array.
     * @param len
     *            The number of characters to write.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the characters, or if a
     *                restlet further down the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    @Override
    public void characters(char ch[], int start, int len) throws SAXException {
        characters(isDataFormat(), ch, start, len);
    }

    /**
     * Write a string of character data, with XML escaping.
     * <p>
     * This is a convenience method that takes an XML String, converts it to a
     * character array, then invokes {@link #characters(char[], int, int)}.
     * </p>
     * 
     * @param data
     *            The character data.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the string, or if a restlet
     *                further down the filter chain raises an exception.
     * @see #characters(char[], int, int)
     */
    public void characters(String data) throws SAXException {
        characters(false, data);
    }

    /**
     * Write an element with character data content but no attributes or
     * Namespace URI.
     * 
     * <p>
     * This is a convenience method to write a complete element with character
     * data content, including the start tag and end tag. The method provides an
     * empty string for the Namespace URI, and empty string for the qualified
     * name, and an empty attribute list.
     * </p>
     * 
     * <p>
     * This method invokes
     * {@link #startElement(String, String, String, Attributes)}, followed by
     * {@link #characters(String)}, followed by
     * {@link #endElement(String, String, String)}.
     * </p>
     * 
     * @param localName
     *            The element's local name.
     * @param content
     *            The character data content.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the empty tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     * @see #characters(String)
     * @see #endElement(String, String, String)
     */
    public void dataElement(String localName, String content)
            throws SAXException {
        dataElement("", localName, "", this.EMPTY_ATTS, content);
    }

    /**
     * Write an element with character data content but no attributes.
     * 
     * <p>
     * This is a convenience method to write a complete element with character
     * data content, including the start tag and end tag. This method provides
     * an empty string for the qname and an empty attribute list.
     * </p>
     * 
     * <p>
     * This method invokes
     * {@link #startElement(String, String, String, Attributes)}, followed by
     * {@link #characters(String)}, followed by
     * {@link #endElement(String, String, String)}.
     * </p>
     * 
     * @param uri
     *            The element's Namespace URI.
     * @param localName
     *            The element's local name.
     * @param content
     *            The character data content.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the empty tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     * @see #characters(String)
     * @see #endElement(String, String, String)
     */
    public void dataElement(String uri, String localName, String content)
            throws SAXException {
        dataElement(uri, localName, "", this.EMPTY_ATTS, content);
    }

    /**
     * Write an element with character data content.
     * 
     * <p>
     * This is a convenience method to write a complete element with character
     * data content, including the start tag and end tag.
     * </p>
     * 
     * <p>
     * This method invokes
     * {@link #startElement(String, String, String, Attributes)}, followed by
     * {@link #characters(String)}, followed by
     * {@link #endElement(String, String, String)}.
     * </p>
     * 
     * @param uri
     *            The element's Namespace URI.
     * @param localName
     *            The element's local name.
     * @param qName
     *            The element's default qualified name.
     * @param atts
     *            The element's attributes.
     * @param content
     *            The character data content.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the empty tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     * @see #characters(String)
     * @see #endElement(String, String, String)
     */
    public void dataElement(String uri, String localName, String qName,
            Attributes atts, String content) throws SAXException {
        startElement(uri, localName, qName, atts);
        characters(content);
        endElement(uri, localName, qName);
    }

    /**
     * Print indentation for the current level.
     * 
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the indentation characters,
     *                or if a filter further down the chain raises an exception.
     */
    private void doIndent() throws SAXException {
        if ((this.indentStep > 0) && (this.depth > 0)) {
            final int n = this.indentStep * this.depth;
            final char ch[] = new char[n];
            for (int i = 0; i < n; i++) {
                ch[i] = ' ';
            }
            characters(ch, 0, n);
        }
    }

    /**
     * Determine the prefix for an element or attribute name. TODO: this method
     * probably needs some cleanup.
     * 
     * @param uri
     *            The Namespace URI.
     * @param qName
     *            The qualified name (optional); this will be used to indicate
     *            the preferred prefix if none is currently bound.
     * @param isElement
     *            true if this is an element name, false if it is an attribute
     *            name (which cannot use the default Namespace).
     */
    private String doPrefix(String uri, String qName, boolean isElement) {
        final String defaultNS = this.nsSupport.getURI("");
        if ("".equals(uri) || uri == null) {
            if (isElement && (defaultNS != null)) {
                this.nsSupport.declarePrefix("", "");
            }
            return null;
        }
        String prefix;
        if (isElement && (defaultNS != null) && uri.equals(defaultNS)) {
            prefix = "";
        } else {
            prefix = this.nsSupport.getPrefix(uri);
        }
        if (prefix != null) {
            return prefix;
        }
        prefix = this.doneDeclTable.get(uri);
        if ((prefix != null)
                && (((!isElement || (defaultNS != null)) && "".equals(prefix)) || (this.nsSupport
                        .getURI(prefix) != null))) {
            prefix = null;
        }
        if (prefix == null) {
            prefix = this.prefixTable.get(uri);
            if ((prefix != null)
                    && (((!isElement || (defaultNS != null)) && ""
                            .equals(prefix)) || (this.nsSupport.getURI(prefix) != null))) {
                prefix = null;
            }
        }
        if ((prefix == null) && (qName != null) && !"".equals(qName)) {
            final int i = qName.indexOf(':');
            if (i == -1) {
                if (isElement && (defaultNS == null)) {
                    prefix = "";
                }
            } else {
                prefix = qName.substring(0, i);
            }
        }
        for (; (prefix == null) || (this.nsSupport.getURI(prefix) != null); prefix = "__NS"
                + ++this.prefixCounter) {
            // Do nothing
        }

        this.nsSupport.declarePrefix(prefix, uri);
        this.doneDeclTable.put(uri, prefix);
        return prefix;
    }

    // //////////////////////////////////////////////////////////////////
    // Methods from org.xml.sax.ContentHandler.
    // //////////////////////////////////////////////////////////////////

    /**
     * Add an empty element without a Namespace URI, qname or attributes.
     * 
     * <p>
     * This method will supply an empty string for the qname, and empty string
     * for the Namespace URI, and an empty attribute list. It invokes
     * {@link #emptyElement(String, String, String, Attributes)} directly.
     * </p>
     * 
     * @param localName
     *            The element's local name.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the empty tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see #emptyElement(String, String, String, Attributes)
     */
    public void emptyElement(String localName) throws SAXException {
        emptyElement("", localName, "", this.EMPTY_ATTS);
    }

    /**
     * Add an empty element without a qname or attributes.
     * 
     * <p>
     * This method will supply an empty string for the qname and an empty
     * attribute list. It invokes
     * {@link #emptyElement(String, String, String, Attributes)} directly.
     * </p>
     * 
     * @param uri
     *            The element's Namespace URI.
     * @param localName
     *            The element's local name.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the empty tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see #emptyElement(String, String, String, Attributes)
     */
    public void emptyElement(String uri, String localName) throws SAXException {
        emptyElement(uri, localName, "", this.EMPTY_ATTS);
    }

    /**
     * Write an empty element. This method writes an empty element tag rather
     * than a start tag followed by an end tag. Both a {@link #startElement
     * startElement} and an {@link #endElement endElement} event will be passed
     * on down the filter chain.
     * 
     * @param uri
     *            The element's Namespace URI, or the empty string if the
     *            element has no Namespace or if Namespace processing is not
     *            being performed.
     * @param localName
     *            The element's local name (without prefix). This parameter must
     *            be provided.
     * @param qName
     *            The element's qualified name (with prefix), or the empty
     *            string if none is available. This parameter is strictly
     *            advisory: the writer may or may not use the prefix attached.
     * @param atts
     *            The element's attribute list.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the empty tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see #startElement
     * @see #endElement
     */
    public void emptyElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (isDataFormat()) {
            this.state = SEEN_ELEMENT;
            if (this.depth > 0) {
                characters(false, "\n");
            }
            doIndent();
        }

        this.nsSupport.pushContext();
        write('<');
        writeName(uri, localName, qName, true);
        writeAttributes(atts);
        if (this.elementLevel == 1) {
            forceNSDecls();
        }
        writeNSDecls();
        write("/>");
        super.startElement(uri, localName, qName, atts);
        super.endElement(uri, localName, qName);
    }

    /**
     * Write a newline at the end of the document. Pass the event on down the
     * filter chain for further processing.
     * 
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the newline, or if a restlet
     *                further down the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    @Override
    public void endDocument() throws SAXException {
        write('\n');
        super.endDocument();
        try {
            flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * End an element without a Namespace URI or qname.
     * 
     * <p>
     * This method will supply an empty string for the qName and an empty string
     * for the Namespace URI. It invokes
     * {@link #endElement(String, String, String)} directly.
     * </p>
     * 
     * @param localName
     *            The element's local name.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the end tag, or if a restlet
     *                further down the filter chain raises an exception.
     * @see #endElement(String, String, String)
     */
    public void endElement(String localName) throws SAXException {
        endElement("", localName, "");
    }

    /**
     * End an element without a qname.
     * 
     * <p>
     * This method will supply an empty string for the qName. It invokes
     * {@link #endElement(String, String, String)} directly.
     * </p>
     * 
     * @param uri
     *            The element's Namespace URI.
     * @param localName
     *            The element's local name.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the end tag, or if a restlet
     *                further down the filter chain raises an exception.
     * @see #endElement(String, String, String)
     */
    public void endElement(String uri, String localName) throws SAXException {
        endElement(uri, localName, "");
    }

    /**
     * Write an end tag. Pass the event on down the filter chain for further
     * processing.
     * 
     * @param uri
     *            The Namespace URI, or the empty string if none is available.
     * @param localName
     *            The element's local (unprefixed) name (required).
     * @param qName
     *            The element's qualified (prefixed) name, or the empty string
     *            is none is available. This method will use the qName as a
     *            template for generating a prefix if necessary, but it is not
     *            guaranteed to use the same qName.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the end tag, or if a restlet
     *                further down the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (isDataFormat()) {
            this.depth--;
            if (this.state == SEEN_ELEMENT) {
                characters(false, "\n");
                doIndent();
            }
        }

        write("</");
        writeName(uri, localName, qName, true);
        write('>');
        if (this.elementLevel == 1) {
            write('\n');
        }
        super.endElement(uri, localName, qName);
        this.nsSupport.popContext();
        this.elementLevel--;

        if (isDataFormat()) {
            this.state = this.stateStack.pop();
        }
    }

    /**
     * Flush the output.
     * <p>
     * This method flushes the output stream. It is especially useful when you
     * need to make certain that the entire document has been written to output
     * but do not want to close the output stream.
     * </p>
     * <p>
     * This method is invoked automatically by the {@link #endDocument
     * endDocument} method after writing a document.
     * </p>
     * 
     * @see #reset
     */
    public void flush() throws IOException {
        this.output.flush();
    }

    // //////////////////////////////////////////////////////////////////
    // Additional markup.
    // //////////////////////////////////////////////////////////////////

    /**
     * Force a Namespace to be declared on the root element.
     * <p>
     * By default, the XMLWriter will declare only the Namespaces needed for an
     * element; as a result, a Namespace may be declared many places in a
     * document if it is not used on the root element.
     * </p>
     * <p>
     * This method forces a Namespace to be declared on the root element even if
     * it is not used there, and reduces the number of xmlns attributes in the
     * document.
     * </p>
     * 
     * @param uri
     *            The Namespace URI to declare.
     * @see #forceNSDecl(java.lang.String,java.lang.String)
     * @see #setPrefix
     */
    public void forceNSDecl(String uri) {
        this.forcedDeclTable.put(uri, Boolean.TRUE);
    }

    // //////////////////////////////////////////////////////////////////
    // Convenience methods.
    // //////////////////////////////////////////////////////////////////

    /**
     * Force a Namespace declaration with a preferred prefix.
     * <p>
     * This is a convenience method that invokes {@link #setPrefix setPrefix}
     * then {@link #forceNSDecl(java.lang.String) forceNSDecl}.
     * </p>
     * 
     * @param uri
     *            The Namespace URI to declare on the root element.
     * @param prefix
     *            The preferred prefix for the Namespace, or "" for the default
     *            Namespace.
     * @see #setPrefix
     * @see #forceNSDecl(java.lang.String)
     */
    public void forceNSDecl(String uri, String prefix) {
        setPrefix(uri, prefix);
        forceNSDecl(uri);
    }

    /**
     * Force all Namespaces to be declared. This method is used on the root
     * element to ensure that the predeclared Namespaces all appear.
     */
    private void forceNSDecls() {
        for (final String prefix : this.forcedDeclTable.keySet()) {
            doPrefix(prefix, null, true);
        }
    }

    /**
     * Return the current indent step.
     * <p>
     * Return the current indent step: each start tag will be indented by this
     * number of spaces times the number of ancestors that the element has.
     * </p>
     * 
     * @return The number of spaces in each indentation step, or 0 or less for
     *         no indentation.
     */
    public int getIndentStep() {
        return this.indentStep;
    }

    /**
     * Get the current or preferred prefix for a Namespace URI.
     * 
     * @param uri
     *            The Namespace URI.
     * @return The preferred prefix, or "" for the default Namespace.
     * @see #setPrefix
     */
    public String getPrefix(String uri) {
        return this.prefixTable.get(uri);
    }

    /**
     * Returns the underlying writer.
     * 
     * @return The underlying writer.
     */
    public Writer getWriter() {
        return this.output;
    }

    /**
     * Write ignorable whitespace. Pass the event on down the filter chain for
     * further processing.
     * 
     * @param ch
     *            The array of characters to write.
     * @param start
     *            The starting position in the array.
     * @param length
     *            The number of characters to write.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the whitespace, or if a
     *                restlet further down the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace
     */
    @Override
    public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException {
        writeEsc(ch, start, length, false);
        super.ignorableWhitespace(ch, start, length);
    }

    /**
     * Internal initialization method.
     * 
     * <p>
     * All of the public constructors invoke this method.
     * 
     * @param writer
     *            The output destination, or null to use standard output.
     */
    private void init(Writer writer) {
        setOutput(writer);
        this.nsSupport = new NamespaceSupport();
        this.prefixTable = new ConcurrentHashMap<String, String>();
        this.forcedDeclTable = new ConcurrentHashMap<String, Boolean>();
        this.doneDeclTable = new ConcurrentHashMap<String, String>();
    }

    public boolean isDataFormat() {
        return this.dataFormat;
    }

    /**
     * Write a processing instruction. Pass the event on down the filter chain
     * for further processing.
     * 
     * @param target
     *            The PI target.
     * @param data
     *            The PI data.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the PI, or if a restlet
     *                further down the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        write("<?");
        write(target);
        write(' ');
        write(data);
        write("?>");
        if (this.elementLevel < 1) {
            write('\n');
        }
        super.processingInstruction(target, data);
    }

    /**
     * Reset the writer.
     * 
     * <p>
     * This method is especially useful if the writer throws an exception before
     * it is finished, and you want to reuse the writer for a new document. It
     * is usually a good idea to invoke {@link #flush flush} before resetting
     * the writer, to make sure that no output is lost.
     * </p>
     * 
     * <p>
     * This method is invoked automatically by the {@link #startDocument
     * startDocument} method before writing a new document.
     * </p>
     * 
     * <p>
     * <strong>Note:</strong> this method will <em>not</em> clear the prefix or
     * URI information in the writer or the selected output writer.
     * </p>
     * 
     * @see #flush
     */
    public void reset() {
        if (isDataFormat()) {
            this.depth = 0;
            this.state = SEEN_NOTHING;
            this.stateStack = new Stack<Object>();
        }

        this.elementLevel = 0;
        this.prefixCounter = 0;
        this.nsSupport.reset();
    }

    public void setDataFormat(boolean dataFormat) {
        this.dataFormat = dataFormat;
    }

    // //////////////////////////////////////////////////////////////////
    // Internal methods.
    // //////////////////////////////////////////////////////////////////

    /**
     * Set the current indent step.
     * 
     * @param indentStep
     *            The new indent step (0 or less for no indentation).
     */
    public void setIndentStep(int indentStep) {
        this.indentStep = indentStep;
    }

    /**
     * Set a new output destination for the document.
     * 
     * @param writer
     *            The output destination, or null to use standard output.
     * @see #flush
     */
    public void setOutput(Writer writer) {
        if (writer == null) {
            this.output = new OutputStreamWriter(System.out);
        } else {
            this.output = writer;
        }
    }

    /**
     * Specify a preferred prefix for a Namespace URI.
     * <p>
     * Note that this method does not actually force the Namespace to be
     * declared; to do that, use the {@link #forceNSDecl(java.lang.String)
     * forceNSDecl} method as well.
     * </p>
     * 
     * @param uri
     *            The Namespace URI.
     * @param prefix
     *            The preferred prefix, or "" to select the default Namespace.
     * @see #getPrefix
     * @see #forceNSDecl(java.lang.String)
     * @see #forceNSDecl(java.lang.String,java.lang.String)
     */
    public void setPrefix(String uri, String prefix) {
        this.prefixTable.put(uri, prefix);
    }

    /**
     * Write the XML declaration at the beginning of the document. Pass the
     * event on down the filter chain for further processing.
     * 
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the XML declaration, or if a
     *                restlet further down the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    @Override
    public void startDocument() throws SAXException {
        reset();
        write("<?xml version=\"1.0\" standalone='yes'?>\n");
        super.startDocument();
    }

    /**
     * Start a new element without a qname, attributes or a Namespace URI.
     * 
     * <p>
     * This method will provide an empty string for the Namespace URI, and empty
     * string for the qualified name, and a default empty attribute list. It
     * invokes #startElement(String, String, String, Attributes)} directly.
     * </p>
     * 
     * @param localName
     *            The element's local name.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the start tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     */
    public void startElement(String localName) throws SAXException {
        startElement("", localName, "", this.EMPTY_ATTS);
    }

    /**
     * Start a new element without a qname or attributes.
     * 
     * <p>
     * This method will provide a default empty attribute list and an empty
     * string for the qualified name. It invokes
     * {@link #startElement(String, String, String, Attributes)} directly.
     * </p>
     * 
     * @param uri
     *            The element's Namespace URI.
     * @param localName
     *            The element's local name.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the start tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String localName) throws SAXException {
        startElement(uri, localName, "", this.EMPTY_ATTS);
    }

    /**
     * Write a start tag. Pass the event on down the filter chain for further
     * processing.
     * 
     * @param uri
     *            The Namespace URI, or the empty string if none is available.
     * @param localName
     *            The element's local (unprefixed) name (required).
     * @param qName
     *            The element's qualified (prefixed) name, or the empty string
     *            is none is available. This method will use the qName as a
     *            template for generating a prefix if necessary, but it is not
     *            guaranteed to use the same qName.
     * @param atts
     *            The element's attribute list (must not be null).
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the start tag, or if a
     *                restlet further down the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (isDataFormat()) {
            this.stateStack.push(SEEN_ELEMENT);
            this.state = SEEN_NOTHING;
            if (this.depth > 0) {
                characters("\n");
            }
            doIndent();
        }

        this.elementLevel++;
        this.nsSupport.pushContext();
        write('<');
        writeName(uri, localName, qName, true);
        writeAttributes(atts);
        if (this.elementLevel == 1) {
            forceNSDecls();
        }
        writeNSDecls();
        write('>');
        super.startElement(uri, localName, qName, atts);

        if (isDataFormat()) {
            this.depth++;
        }
    }

    /**
     * Write a raw character.
     * 
     * @param c
     *            The character to write.
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the character, this method
     *                will throw an IOException wrapped in a SAXException.
     */
    private void write(char c) throws SAXException {
        try {
            this.output.write(c);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Write a raw string.
     * 
     * @param s
     * @exception org.xml.sax.SAXException
     *                If there is an error writing the string, this method will
     *                throw an IOException wrapped in a SAXException
     */
    private void write(String s) throws SAXException {
        try {
            this.output.write(s);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Write out an attribute list, escaping values. The names will have
     * prefixes added to them.
     * 
     * @param atts
     *            The attribute list to write.
     * @exception org.xml.SAXException
     *                If there is an error writing the attribute list, this
     *                method will throw an IOException wrapped in a
     *                SAXException.
     */
    private void writeAttributes(Attributes atts) throws SAXException {
        final int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            if ("xmlns".equals(atts.getQName(i))) {
                // Redefines the default namespace.
                forceNSDecl(atts.getValue(i));
            } else if (atts.getQName(i) != null
                    && atts.getQName(i).startsWith("xmlns")) {
                // Defines the namespace using its prefix.
                forceNSDecl(atts.getValue(i), atts.getLocalName(i));
            } else {
                final char ch[] = atts.getValue(i).toCharArray();
                write(' ');
                writeName(atts.getURI(i), atts.getLocalName(i),
                        atts.getQName(i), false);
                write("=\"");
                writeEsc(ch, 0, ch.length, true);
                write('"');
            }
        }
    }

    /**
     * Write an array of data characters with escaping.
     * 
     * @param ch
     *            The array of characters.
     * @param start
     *            The starting position.
     * @param length
     *            The number of characters to use.
     * @param isAttVal
     *            true if this is an attribute value literal.
     * @exception org.xml.SAXException
     *                If there is an error writing the characters, this method
     *                will throw an IOException wrapped in a SAXException.
     */
    private void writeEsc(char ch[], int start, int length, boolean isAttVal)
            throws SAXException {
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
            case '&':
                write("&amp;");
                break;
            case '<':
                write("&lt;");
                break;
            case '>':
                write("&gt;");
                break;
            case '\"':
                if (isAttVal) {
                    write("&quot;");
                } else {
                    write('\"');
                }
                break;
            default:
                if (ch[i] > '\u007f') {
                    write("&#");
                    write(Integer.toString(ch[i]));
                    write(';');
                } else {
                    write(ch[i]);
                }
            }
        }
    }

    /**
     * Write an element or attribute name.
     * 
     * @param uri
     *            The Namespace URI.
     * @param localName
     *            The local name.
     * @param qName
     *            The prefixed name, if available, or the empty string.
     * @param isElement
     *            true if this is an element name, false if it is an attribute
     *            name.
     * @exception org.xml.sax.SAXException
     *                This method will throw an IOException wrapped in a
     *                SAXException if there is an error writing the name.
     */
    private void writeName(String uri, String localName, String qName,
            boolean isElement) throws SAXException {

        final String prefix = doPrefix(uri, qName, isElement);
        if ((prefix != null) && !"".equals(prefix)) {
            write(prefix);
            write(':');
        }
        write(localName);
    }

    /**
     * Write out the list of Namespace declarations.
     * 
     * @exception org.xml.sax.SAXException
     *                This method will throw an IOException wrapped in a
     *                SAXException if there is an error writing the Namespace
     *                declarations.
     */
    @SuppressWarnings("unchecked")
    private void writeNSDecls() throws SAXException {
        final Enumeration<String> prefixes = this.nsSupport
                .getDeclaredPrefixes();
        while (prefixes.hasMoreElements()) {
            final String prefix = prefixes.nextElement();
            String uri = this.nsSupport.getURI(prefix);
            if (uri == null) {
                uri = "";
            }
            final char ch[] = uri.toCharArray();
            write(' ');
            if ("".equals(prefix)) {
                write("xmlns=\"");
            } else {
                write("xmlns:");
                write(prefix);
                write("=\"");
            }
            writeEsc(ch, 0, ch.length, true);
            write('\"');
        }
    }

}
