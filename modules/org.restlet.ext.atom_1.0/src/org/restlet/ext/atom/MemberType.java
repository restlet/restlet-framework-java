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

package org.restlet.ext.atom;

import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Collection member types.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public enum MemberType {
    ENTRY, MEDIA;

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @param namespace
     *            The element namespace URI.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer, String namespace)
            throws SAXException {
        writer.startElement(namespace, "memberType");
        writer.endElement(namespace, "memberType");
    }

}
