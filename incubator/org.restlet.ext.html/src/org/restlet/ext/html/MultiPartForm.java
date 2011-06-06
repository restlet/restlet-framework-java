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
            outputStream.write(getBoundary().getBytes());
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
        outputStream.write((getBoundary() + "--").getBytes());
        HeaderUtils.writeCRLF(outputStream);
   }

}
