/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.html;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.engine.header.ContentType;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.representation.OutputRepresentation;

/**
 * Multi-part HTML form that can mix textual data and binary data such as a file
 * upload field.
 * 
 * @author Jerome Louvel
 */
public class MultiPartForm extends OutputRepresentation {

    private final static String DEFAULT_BOUNDARY = "---Aa1Bb2Cc3---";

    public static MediaType getFormMediaType(String boundary) {
        Form params = new Form();
        params.add("boundary", boundary);
        MediaType result = new MediaType(
                MediaType.MULTIPART_FORM_DATA.getName(), params);
        return result;
    }

    private volatile String boundary;

    private final List<MultiPartData> data;

    public MultiPartForm() {
        this(DEFAULT_BOUNDARY);
    }

    public MultiPartForm(String boundary) {
        super(getFormMediaType(boundary));
        this.boundary = boundary;
        this.data = new CopyOnWriteArrayList<MultiPartData>();
    }

    public String getBoundary() {
        return boundary;
    }

    public List<MultiPartData> getData() {
        return data;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        for (MultiPartData data : getData()) {
            // Write the boundary line
            outputStream.write(("--" + getBoundary()).getBytes());
            HeaderUtils.writeCRLF(outputStream);

            // Write the optional content type header line
            if (MediaType.TEXT_PLAIN.equals(data.getMediaType())) {
                // Write the content disposition header line
                String line = "Content-Disposition: form-data; name=\""
                        + data.getName() + "\"";
                outputStream.write(line.getBytes());
                HeaderUtils.writeCRLF(outputStream);
            } else {
                // Write the content disposition header line
                String line = "Content-Disposition: form-data; name=\""
                        + data.getName() + "\"; filename=\""
                        + data.getFilename() + "\"";
                outputStream.write(line.getBytes());
                HeaderUtils.writeCRLF(outputStream);

                // Write the content type header line
                line = "Content-Type: "
                        + ContentType.writeHeader(data.getValue());
                outputStream.write(line.getBytes());
                HeaderUtils.writeCRLF(outputStream);
            }

            // Write the data content
            HeaderUtils.writeCRLF(outputStream);
            data.getValue().write(outputStream);
            HeaderUtils.writeCRLF(outputStream);
        }

        // Write the final boundary line
        outputStream.write(("--" + getBoundary() + "--").getBytes());
        HeaderUtils.writeCRLF(outputStream);
    }

}
