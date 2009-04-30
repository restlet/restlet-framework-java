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

package org.restlet.representation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.data.MediaType;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

/**
 * Representation based on an XML document. It knows how to evaluate XPath
 * expressions and how to manage a namespace context. This class also offers
 * convenient methods to validate the document against a specified XML scheme.
 * 
 * @author Jerome Louvel
 */
public abstract class XmlRepresentation extends OutputRepresentation {

	/**
	 * A SAX {@link EntityResolver} to use when resolving external entity
	 * references while parsing this type of XML representations.
	 * 
	 * @see DocumentBuilder#setEntityResolver(EntityResolver)
	 */
	private volatile EntityResolver entityResolver;

	/**
	 * A SAX {@link ErrorHandler} to use for signaling SAX exceptions while
	 * parsing this type of XML representations.
	 * 
	 * @see DocumentBuilder#setErrorHandler(ErrorHandler)
	 */
	private volatile ErrorHandler errorHandler;

	/** Indicates if processing is namespace aware. */
	private volatile boolean namespaceAware;

	/** Internal map of namespaces. */
	private volatile Map<String, String> namespaces;

	/**
	 * Indicates the desire for validating this type of XML representations
	 * against an XML schema if one is referenced within the contents.
	 * 
	 * @see DocumentBuilderFactory#setValidating(boolean)
	 */
	private volatile boolean validating;

	/**
	 * Indicates the desire for processing <em>XInclude</em> if found in this
	 * type of XML representations.
	 * 
	 * @see DocumentBuilderFactory#setXIncludeAware(boolean)
	 */
	private volatile boolean xIncludeAware;

	/**
	 * Constructor.
	 * 
	 * @param mediaType
	 *            The representation's mediaType.
	 */
	public XmlRepresentation(MediaType mediaType) {
		super(mediaType);
		this.namespaces = null;
		this.namespaceAware = false;
	}

	/**
	 * Constructor.
	 * 
	 * @param mediaType
	 *            The representation's mediaType.
	 * @param expectedSize
	 *            The expected input stream size.
	 */
	public XmlRepresentation(MediaType mediaType, long expectedSize) {
		super(mediaType, expectedSize);
		this.namespaces = null;
		this.namespaceAware = false;
	}

	/**
	 * Returns a document builder properly configured.
	 * 
	 * @return A document builder properly configured.
	 */
	protected DocumentBuilder getDocumentBuilder() throws IOException {
		DocumentBuilder result = null;

		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory
					.newInstance();
			dbf.setNamespaceAware(isNamespaceAware());
			dbf.setValidating(isValidating());
			dbf.setXIncludeAware(isXIncludeAware());

			result = dbf.newDocumentBuilder();
			result.setEntityResolver(getEntityResolver());
			result.setErrorHandler(getErrorHandler());
		} catch (ParserConfigurationException pce) {
			throw new IOException("Couldn't create the empty document: "
					+ pce.getMessage());
		}

		return result;
	}

	/**
	 * Return the possibly null current SAX {@link EntityResolver}.
	 * 
	 * @return The possibly null current SAX {@link EntityResolver}.
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	/**
	 * Return the possibly null current SAX {@link ErrorHandler}.
	 * 
	 * @return The possibly null current SAX {@link ErrorHandler}.
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Returns the map of namespaces.
	 * 
	 * @return The map of namespaces.
	 */
	private Map<String, String> getNamespaces() {
		if (this.namespaces == null) {
			this.namespaces = new HashMap<String, String>();
		}
		return this.namespaces;
	}

	/**
	 * {@inheritDoc
	 * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String}
	 */
	public String getNamespaceURI(String prefix) {
		return this.namespaces.get(prefix);
	}

	/**
	 * {@inheritDoc
	 * javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String}
	 */
	public String getPrefix(String namespaceURI) {
		String result = null;
		boolean found = false;

		for (Iterator<String> iterator = getNamespaces().keySet().iterator(); iterator
				.hasNext()
				&& !found;) {
			String key = iterator.next();
			if (getNamespaces().get(key).equals(namespaceURI)) {
				found = true;
				result = key;
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc
	 * javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String}
	 */
	public Iterator<String> getPrefixes(String namespaceURI) {
		final List<String> result = new ArrayList<String>();

		for (Iterator<String> iterator = getNamespaces().keySet().iterator(); iterator
				.hasNext();) {
			String key = iterator.next();
			if (getNamespaces().get(key).equals(namespaceURI)) {
				result.add(key);
			}
		}

		return Collections.unmodifiableList(result).iterator();
	}

	/**
	 * Indicates if processing is namespace aware.
	 * 
	 * @return True if processing is namespace aware.
	 */
	public boolean isNamespaceAware() {
		return this.namespaceAware;
	}

	/**
	 * Indicates the desire for validating this type of XML representations
	 * against an XML schema if one is referenced within the contents.
	 * 
	 * @return True if the schema-based validation is enabled.
	 */
	public boolean isValidating() {
		return validating;
	}

	/**
	 * Indicates the desire for processing <em>XInclude</em> if found in this
	 * type of XML representations.
	 * 
	 * @return The current value of the xIncludeAware flag.
	 */
	public boolean isXIncludeAware() {
		return xIncludeAware;
	}

	/**
	 * Puts a new mapping between a prefix and a namespace URI.
	 * 
	 * @param prefix
	 *            The namespace prefix.
	 * @param namespaceURI
	 *            The namespace URI.
	 */
	public void putNamespace(String prefix, String namespaceURI) {
		getNamespaces().put(prefix, namespaceURI);
	}

	/**
	 * Releases the namespaces map.
	 */
	@Override
	public void release() {
		if (this.namespaces != null) {
			this.namespaces.clear();
			this.namespaces = null;
		}
		super.release();
	}

	/**
	 * Set the {@link EntityResolver} to use when resolving external entity
	 * references encountered in this type of XML representations.
	 * 
	 * @param entityResolver
	 *            the {@link EntityResolver} to set.
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/**
	 * Set the {@link ErrorHandler} to use when signaling SAX event exceptions.
	 * 
	 * @param errorHandler
	 *            the {@link ErrorHandler} to set.
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * Indicates if processing is namespace aware.
	 * 
	 * @param namespaceAware
	 *            Indicates if processing is namespace aware.
	 */
	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}

	/**
	 * Indicates the desire for validating this type of XML representations
	 * against an XML schema if one is referenced within the contents.
	 * 
	 * @param validating
	 *            The new validation flag to set.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Indicates the desire for processing <em>XInclude</em> if found in this
	 * type of XML representations.
	 * 
	 * @param includeAware
	 *            The new value of the xIncludeAware flag.
	 */
	public void setXIncludeAware(boolean includeAware) {
		xIncludeAware = includeAware;
	}

}
