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

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.util.Iterator;

import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes an error condition for response descriptions.
 * 
 * @author Jerome Louvel
 */
public class FaultInfo extends RepresentationInfo {

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @throws SAXException
     */
    @Override
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

        if (getDocumentations().isEmpty() && getParameters().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "fault", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "fault", null, attributes);

            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }
            for (ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "fault");
        }
    }

}
