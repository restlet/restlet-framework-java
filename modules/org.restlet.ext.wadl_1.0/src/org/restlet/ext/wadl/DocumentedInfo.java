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
package org.restlet.ext.wadl;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass of WADL elements that supports dcumentation.
 * 
 */
public class DocumentedInfo {
	/** Doc elements used to document that element. */
	private List<DocumentationInfo> documentations;

	/**
	 * Constructor.
	 */
	public DocumentedInfo() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param documentation
	 *            A single DocumentationInfo element.
	 */
	public DocumentedInfo(DocumentationInfo documentation) {
		super();
		getDocumentations().add(documentation);
	}

	/**
	 * Constructor.
	 * 
	 * @param documentations
	 *            The list of DocumentationInfo elements.
	 */
	public DocumentedInfo(List<DocumentationInfo> documentations) {
		super();
		this.documentations = documentations;
	}

	/**
	 * Constructor.
	 * 
	 * @param documentation
	 *            A single DocumentationInfo element.
	 */
	public DocumentedInfo(String documentation) {
		this(new DocumentationInfo(documentation));
	}

	/**
	 * Returns the list of documentation elements.
	 * 
	 * @return The list of documentation elements.
	 */
	public List<DocumentationInfo> getDocumentations() {
		// Lazy initialization with double-check.
		List<DocumentationInfo> d = this.documentations;
		if (d == null) {
			synchronized (this) {
				d = this.documentations;
				if (d == null) {
					this.documentations = d = new ArrayList<DocumentationInfo>();
				}
			}
		}
		return d;
	}

	/**
	 * Set the list of documentation elements with a single element.
	 * 
	 * @param documentationInfo
	 *            A single documentation element.
	 */
	public void setDocumentation(DocumentationInfo documentationInfo) {
		getDocumentations().clear();
		getDocumentations().add(documentationInfo);
	}

	/**
	 * Set the list of documentation elements with a single element.
	 * 
	 * @param documentation
	 *            A single documentation element.
	 */
	public void setDocumentation(String documentation) {
		getDocumentations().clear();
		getDocumentations().add(new DocumentationInfo(documentation));
	}

	/**
	 * Sets the list of documentation elements.
	 * 
	 * @param doc
	 *            The list of documentation elements.
	 */
	public void setDocumentations(List<DocumentationInfo> doc) {
		this.documentations = doc;
	}

}
