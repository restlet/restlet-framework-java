/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.emf.internal;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Offers a generic HTML representation of an EMF object. It lists all its
 * properties and can even generate HTML links when the proper EMF eAnnotation
 * is detected.
 * 
 * This is useful to be able to automatically navigate a web API whose resource
 * representations are defined using EMF.
 * 
 * @author Jerome Louvel
 */
public class EmfHtmlWriter {

    public static final String ANNOTATION_URI = "http://www.restlet.org/schemas/2011/emf/html";

    private final EObject object;

    /**
     * Returns the EMF object to write.
     * 
     * @return The EMF object to write.
     */
    public EObject getObject() {
        return object;
    }

    /**
     * Constructor.
     * 
     * @param object
     */
    public EmfHtmlWriter(EObject object) {
        this.object = object;
    }

    /**
     * Writes the wrapped EMF object as an HTML document.
     * 
     * @param writer
     *            The writer to use.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void write(Writer writer) throws IOException {
        String title = null;
        EClass eClass = getObject().eClass();
        EAnnotation annotation = eClass.getEAnnotation(ANNOTATION_URI);

        if (annotation != null) {
            title = (String) annotation.getDetails().get("label");
        }

        title = (title == null) ? eClass.getName() : title;

        // Write the header
        writer.write("<html>\n");
        writer.write("<body style=\"font-family: sans-serif;\">\n");
        writer.write("<h2>" + title + "</h2>\n");
        writer.write("<table border=\"0\">\n");
        writer.write("<thead>\n");
        writer.write("<tr>");
        writer.write("<td><b>Property</b></td>\n");
        writer.write("<td><b>Value</b></td>\n");
        writer.write("</tr>\n");
        writer.write("</thead>\n");
        writer.write("<tbody>\n");

        // Write the object properties
        for (EObject content : eClass.eContents()) {
            if (content instanceof EStructuralFeature) {
                EStructuralFeature sf = (EStructuralFeature) content;
                String label = null;
                boolean hyperlink = false;
                annotation = sf.getEAnnotation(ANNOTATION_URI);

                if (annotation != null) {
                    label = (String) annotation.getDetails().get("label");
                    hyperlink = Boolean.parseBoolean(annotation.getDetails()
                            .get("linked"));
                }

                label = (label == null) ? sf.getName() : label;
                Object value = getObject().eGet(sf);

                if (value instanceof EList) {
                    EList<Object> items = (EList<Object>) value;

                    for (Object item : items) {
                        writeRow(writer, label, item.toString(), hyperlink);
                    }
                } else {
                    writeRow(writer, label,
                            (value == null) ? "null" : value.toString(),
                            hyperlink);
                }
            }
        }

        writer.write("</tbody>\n");
        writer.write("</table>\n");
        writer.write("</body>\n");
        writer.write("</html>\n");
        writer.flush();
    }

    private void writeRow(Writer writer, String name, String value,
            boolean hyperlink) throws IOException {
        writer.write("<tr>");

        // Write the property name
        writer.write("<td>");
        writer.write(name);
        writer.write("</td>\n");

        // Write the property value
        writer.write("<td>");

        if (hyperlink) {
            writer.write("<a href=\"" + value + "\">");
        }

        writer.write(value);

        if (hyperlink) {
            writer.write("</a>");
        }

        writer.write("</td>\n");

        writer.write("</tr>\n");
    }
}
