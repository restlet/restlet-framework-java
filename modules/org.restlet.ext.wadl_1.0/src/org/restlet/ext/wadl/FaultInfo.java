/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.wadl;

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.util.Iterator;
import java.util.List;

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
     * Constructor.
     */
    public FaultInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public FaultInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public FaultInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public FaultInfo(String documentation) {
        super(documentation);
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    @Override
    public void writeElement(XmlWriter writer) throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();
        if ((getIdentifier() != null) && !getIdentifier().equals("")) {
            attributes.addAttribute("", "id", null, "xs:ID", getIdentifier());
        }
        if (getMediaType() != null) {
            attributes.addAttribute("", "mediaType", null, "xs:string",
                    getMediaType().toString());
        }
        if ((getProfiles() != null) && !getProfiles().isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            for (final Iterator<Reference> iterator = getProfiles().iterator(); iterator
                    .hasNext();) {
                final Reference reference = iterator.next();
                builder.append(reference.toString());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            attributes.addAttribute("", "profile", null, "xs:string", builder
                    .toString());
        }
        if ((getStatuses() != null) && !getStatuses().isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            for (final Iterator<Status> iterator = getStatuses().iterator(); iterator
                    .hasNext();) {
                final Status status = iterator.next();
                builder.append(status.getCode());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            attributes.addAttribute("", "status", null, "xs:string", builder
                    .toString());
        }
        if ((getXmlElement() != null) && !getXmlElement().equals("")) {
            attributes.addAttribute("", "element", null, "xs:QName",
                    getXmlElement());
        }

        if (getDocumentations().isEmpty() && getParameters().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "fault", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "fault", null, attributes);

            for (final DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }
            for (final ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "fault");
        }
    }

}
