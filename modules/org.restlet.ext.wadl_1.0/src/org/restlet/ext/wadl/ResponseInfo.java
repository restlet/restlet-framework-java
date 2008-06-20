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

import java.util.List;

import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Describes the properties of a response associated to a parent method.
 * 
 * @author Jerome Louvel
 */
public class ResponseInfo {

	private List<DocumentationInfo> documentations;

	private List<FaultInfo> faults;

	private List<ParameterInfo> parameters;

	private List<RepresentationInfo> representations;

	public List<DocumentationInfo> getDocumentations() {
		return documentations;
	}

	public List<FaultInfo> getFaults() {
		return faults;
	}

	public List<ParameterInfo> getParameters() {
		return parameters;
	}

	public List<RepresentationInfo> getRepresentations() {
		return representations;
	}

	public void setDocumentations(List<DocumentationInfo> doc) {
		this.documentations = doc;
	}

	public void setFaults(List<FaultInfo> faults) {
		this.faults = faults;
	}

	public void setParameters(List<ParameterInfo> parameters) {
		this.parameters = parameters;
	}

	public void setRepresentations(List<RepresentationInfo> representations) {
		this.representations = representations;
	}

	/**
	 * Writes the current object as an XML element using the given SAX writer.
	 * 
	 * @param writer
	 *            The SAX writer.
	 * @throws SAXException
	 */
	public void writeElement(XmlWriter writer) throws SAXException {
		writer.startElement("", "response");
		if (getDocumentations() != null) {
			for (DocumentationInfo documentationInfo : getDocumentations()) {
				documentationInfo.writeElement(writer);
			}
		}
		if (getFaults() != null) {
			for (FaultInfo faultInfo : getFaults()) {
				faultInfo.writeElement(writer);
			}
		}
		if (getParameters() != null) {
			for (ParameterInfo parameterInfo : getParameters()) {
				parameterInfo.writeElement(writer);
			}
		}
		if (getRepresentations() != null) {
			for (RepresentationInfo representationInfo : getRepresentations()) {
				representationInfo.writeElement(writer);
			}
		}
		writer.endElement("", "response");

	}

}
