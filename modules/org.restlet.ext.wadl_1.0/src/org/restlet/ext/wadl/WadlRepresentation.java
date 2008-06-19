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

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.XmlRepresentation;

/**
 * Root of a WADL description document.
 * 
 * @author Jerome Louvel
 */
public class WadlRepresentation extends XmlRepresentation {

    /** Web Application Description Language namespace. */
    public static final String APP_NAMESPACE = "http://research.sun.com/wadl/2006/10";

    private ApplicationInfo application;

    public WadlRepresentation() {
        super(MediaType.APPLICATION_WADL_XML);
    }

    public WadlRepresentation(ApplicationInfo application) {
        super(MediaType.APPLICATION_WADL_XML);
        this.application = application;
    }

    public WadlRepresentation(Representation xmlRepresentation) {
        super(MediaType.APPLICATION_WADL_XML);

        // TODO: parse the given document using SAX to produce an
        // ApplicationInfo instance.
    }

    @Override
    public Object evaluate(String expression, QName returnType)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public ApplicationInfo getApplication() {
        return application;
    }

    public void setApplication(ApplicationInfo application) {
        this.application = application;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        // Convert the attache ApplicationInfo instance into an equivalent WADL
        // XML document.
    }

}
