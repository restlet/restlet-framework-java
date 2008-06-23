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
import java.util.Iterator;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describres a variant representation for a target resource.
 * 
 * @author Jerome Louvel
 */
public class RepresentationInfo {

	private List<DocumentationInfo> documentations;

	private String identifier;

	private MediaType mediaType;

	private List<ParameterInfo> parameters;

	private List<Reference> profiles;

	private List<Status> statuses;

	private String xmlElement;

	public List<DocumentationInfo> getDocumentations() {
		// Lazy initialization with double-check.
		List<DocumentationInfo> d = this.documentations;
		if (d == null) {
			synchronized (this) {
				d = this.documentations;
				if (d == null)
					this.documentations = d = new ArrayList<DocumentationInfo>();
			}
		}
		return d;
	}

	public String getIdentifier() {
		return identifier;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public List<ParameterInfo> getParameters() {
		// Lazy initialization with double-check.
		List<ParameterInfo> p = this.parameters;
		if (p == null) {
			synchronized (this) {
				p = this.parameters;
				if (p == null)
					this.parameters = p = new ArrayList<ParameterInfo>();
			}
		}
		return p;
	}

	public List<Reference> getProfiles() {
		// Lazy initialization with double-check.
		List<Reference> p = this.profiles;
		if (p == null) {
			synchronized (this) {
				p = this.profiles;
				if (p == null)
					this.profiles = p = new ArrayList<Reference>();
			}
		}
		return p;
	}

	public List<Status> getStatuses() {
		// Lazy initialization with double-check.
		List<Status> s = this.statuses;
		if (s == null) {
			synchronized (this) {
				s = this.statuses;
				if (s == null)
					this.statuses = s = new ArrayList<Status>();
			}
		}
		return s;
	}

	public String getXmlElement() {
		return xmlElement;
	}

	public void setDocumentations(List<DocumentationInfo> doc) {
		this.documentations = doc;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public void setParameters(List<ParameterInfo> parameters) {
		this.parameters = parameters;
	}

	public void setProfiles(List<Reference> profiles) {
		this.profiles = profiles;
	}

	public void setStatuses(List<Status> statuses) {
		this.statuses = statuses;
	}

	public void setXmlElement(String xmlElement) {
		this.xmlElement = xmlElement;
	}

	/**
	 * Writes the current object as an XML element using the given SAX writer.
	 * 
	 * @param writer
	 *            The SAX writer.
	 * @throws SAXException
	 */
	public void writeElement(XmlWriter writer) throws SAXException {
		AttributesImpl attributes = new AttributesImpl();
		if (getIdentifier() != null && !getIdentifier().equals("")) {
			attributes.addAttribute("", "id", null, "xs:ID", getIdentifier());
		}
		if (getMediaType() != null) {
			attributes.addAttribute("", "mediaType", null, "xs:string",
					getMediaType().toString());
		}
		if (getProfiles() != null && !getProfiles().isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (Iterator<Reference> iterator = getProfiles().iterator(); iterator
					.hasNext();) {
				Reference reference = iterator.next();
				builder.append(reference.toString());
				if (iterator.hasNext()) {
					builder.append(" ");
				}
			}
			attributes.addAttribute("", "profile", null, "xs:string", builder
					.toString());
		}
		if (getStatuses() != null && !getStatuses().isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (Iterator<Status> iterator = getStatuses().iterator(); iterator
					.hasNext();) {
				Status status = iterator.next();
				builder.append(status.getCode());
				if (iterator.hasNext()) {
					builder.append(" ");
				}
			}
			attributes.addAttribute("", "status", null, "xs:string", builder
					.toString());
		}
		if (getXmlElement() != null && !getXmlElement().equals("")) {
			attributes.addAttribute("", "element", null, "xs:QName",
					getXmlElement());
		}

		writer.startElement("", "representation_type", null, attributes);

		if (getDocumentations() != null) {
			for (DocumentationInfo documentationInfo : getDocumentations()) {
				documentationInfo.writeElement(writer);
			}
		}
		if (getParameters() != null) {
			for (ParameterInfo parameterInfo : getParameters()) {
				parameterInfo.writeElement(writer);
			}
		}

		writer.endElement("", "representation_type");
	}

}
