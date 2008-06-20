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

import java.util.Iterator;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes a class of closely related resources.
 * 
 * @author Jerome Louvel
 */
public class ResourceInfo {

	private List<ResourceInfo> childResources;

	private List<DocumentationInfo> documentations;

	private String identifier;

	private List<MethodInfo> methods;

	private List<ParameterInfo> parameters;

	private String path;

	private MediaType queryType;

	private List<Reference> type;

	public List<ResourceInfo> getChildResources() {
		return childResources;
	}

	public List<DocumentationInfo> getDocumentations() {
		return documentations;
	}

	public String getIdentifier() {
		return identifier;
	}

	public List<MethodInfo> getMethods() {
		return methods;
	}

	public List<ParameterInfo> getParameters() {
		return parameters;
	}

	public String getPath() {
		return path;
	}

	public MediaType getQueryType() {
		return queryType;
	}

	public List<Reference> getType() {
		return type;
	}

	public void setChildResources(List<ResourceInfo> resources) {
		this.childResources = resources;
	}

	public void setDocumentations(List<DocumentationInfo> doc) {
		this.documentations = doc;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setMethods(List<MethodInfo> methods) {
		this.methods = methods;
	}

	public void setParameters(List<ParameterInfo> parameters) {
		this.parameters = parameters;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setQueryType(MediaType queryType) {
		this.queryType = queryType;
	}

	public void setType(List<Reference> type) {
		this.type = type;
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

		if (getPath() != null && !getPath().equals("")) {
			attributes.addAttribute("", "path", null, "xs:string", getPath());
		}

		if (getQueryType() != null) {
			attributes.addAttribute("", "queryType", null, "xs:string",
					getQueryType().getMainType());
		}
		if (getType() != null && !getType().isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (Iterator<Reference> iterator = getType().iterator(); iterator
					.hasNext();) {
				Reference reference = iterator.next();
				builder.append(reference.toString());
				if (iterator.hasNext()) {
					builder.append(" ");
				}
			}
			attributes.addAttribute("", "type", null, "xs:string", builder
					.toString());
		}

		writer.startElement("", "resource", null, attributes);

		if (getChildResources() != null) {
			for (ResourceInfo resourceInfo : getChildResources()) {
				resourceInfo.writeElement(writer);
			}
		}
		if (getDocumentations() != null) {
			for (DocumentationInfo documentationInfo : getDocumentations()) {
				documentationInfo.writeElement(writer);
			}
		}
		if (getMethods() != null) {
			for (MethodInfo methodInfo : getMethods()) {
				methodInfo.writeElement(writer);
			}
		}
		if (getParameters() != null) {
			for (ParameterInfo parameterInfo : getParameters()) {
				parameterInfo.writeElement(writer);
			}
		}
		
		writer.endElement("", "resource");
	}

}
