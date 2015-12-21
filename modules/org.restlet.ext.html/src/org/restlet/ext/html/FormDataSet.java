/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.html;

import java.io.IOException;
import java.io.OutputStream;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.engine.header.ContentType;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.html.internal.FormUtils;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

/**
 * HTML form supporting either URL encoding or multipart encoding.
 * 
 * @author Jerome Louvel
 */
public class FormDataSet extends OutputRepresentation {

    /** The default boundary separating multipart entries. */
    private final static String DEFAULT_BOUNDARY = "---Aa1Bb2Cc3---";

    /**
     * Creates the media type of a multipart form which must include the used
     * boundary.
     * 
     * @param boundary
     *            The multipart boundary.
     * @return The multipart media type.
     */
    private static MediaType createMultipartMediaType(String boundary) {
        Series<Parameter> params = new Series<Parameter>(Parameter.class);
        params.add("boundary", boundary);
        MediaType result = new MediaType(
                MediaType.MULTIPART_FORM_DATA.getName(), params);
        return result;
    }

    /** The modifiable series of data entries. */
    private final Series<FormData> entries;

    /** Indicates if the form is multipart encoded. */
    private volatile boolean multipart;

    /** The boundary separating multipart entries. */
    private volatile String multipartBoundary;

    /**
     * Default constructor, creates a single part form.
     */
    public FormDataSet() {
        this(null, false, DEFAULT_BOUNDARY);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's mediaType.
     * @param multipart
     *            Indicates if the form is multipart encoded.
     * @param multipartBoundary
     *            The boundary separating multipart entries.
     */
    private FormDataSet(MediaType mediaType, boolean multipart,
            String multipartBoundary) {
        super(mediaType);
        this.entries = new Series<FormData>(FormData.class);
        this.multipartBoundary = multipartBoundary;
        this.multipart = multipart;
    }

    /**
     * Constructor.
     * 
     * @param formRepresentation
     *            The representation to parse.
     */
    public FormDataSet(Representation formRepresentation) {
        this();

        if ((formRepresentation != null)
                && MediaType.APPLICATION_WWW_FORM.equals(formRepresentation
                        .getMediaType())) {
            FormUtils.parse(this.entries, formRepresentation);
        }
    }

    /**
     * Creates a multipart form.
     * 
     * @param multipartBoundary
     *            The boundary separating multipart entries.
     */
    public FormDataSet(String multipartBoundary) {
        this(createMultipartMediaType(multipartBoundary), true,
                multipartBoundary);
    }

    /**
     * Adds a new form data entry.
     * 
     * @param name
     *            The entry name.
     * @param value
     *            The entry value.
     * @return The entry created and added to {@link #getEntries()}.
     */
    public FormData add(String name, String value) {
        FormData result = getEntries().createEntry(name, value);
        getEntries().add(result);
        return result;
    }

    /**
     * Encodes the form using the standard HTML form encoding mechanism and the
     * UTF-8 character set.
     * 
     * @return The encoded form.
     * @throws IOException
     */
    public String encode() throws IOException {
        return encode(false);
    }

    /**
     * Encodes the form using the standard URI encoding mechanism and the UTF-8
     * character set.
     * 
     * @param queryString
     *            True if the target is a query string.
     * @return The encoded form.
     * @throws IOException
     */
    public String encode(boolean queryString) throws IOException {
        return encode('&', queryString);
    }

    /**
     * URL encodes the form.
     * 
     * @param separator
     *            The separator character to append between parameters.
     * @param queryString
     *            True if the target is a query string.
     * @return The encoded form.
     * @throws IOException
     */
    public String encode(char separator, boolean queryString)
            throws IOException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < getEntries().size(); i++) {
            if (i > 0) {
                sb.append(separator);
            }

            getEntries().get(i).encode(sb, queryString);
        }

        return sb.toString();
    }

    /**
     * Returns the modifiable series of form entries.
     * 
     * @return The modifiable series of form entries.
     */
    public Series<FormData> getEntries() {
        return entries;
    }

    /**
     * Formats the form as a matrix path string. Uses UTF-8 as the character set
     * for encoding non-ASCII characters.
     * 
     * @return The form as a matrix string.
     * @see <a href="http://www.w3.org/DesignIssues/MatrixURIs.html">Matrix URIs
     *      by Tim Berners Lee</a>
     */
    public String getMatrixString() {
        try {
            return encode(';', true);
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Returns the boundary separating multipart entries.
     * 
     * @return The boundary separating multipart entries.
     */
    public String getMultipartBoundary() {
        return multipartBoundary;
    }

    /**
     * Formats the form as a query string. Uses UTF-8 as the character set for
     * encoding non-ASCII characters.
     * 
     * @return The form as a query string.
     */
    public String getQueryString() {
        try {
            return encode(true);
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Indicates if the form is multipart encoded.
     * 
     * @return True if the form is multipart encoded.
     */
    public boolean isMultipart() {
        return this.multipart;
    }

    /**
     * Indicates if the form is multipart encoded.
     * 
     * @param multipart
     *            True if the form is multipart encoded.
     */
    public void setMultipart(boolean multipart) {
        this.multipart = multipart;
        if (this.multipart && getMultipartBoundary() == null) {
            this.multipartBoundary = DEFAULT_BOUNDARY;
        }
        setMediaType(createMultipartMediaType(getMultipartBoundary()));
    }

    /**
     * Sets the boundary separating multipart entries.
     * 
     * @param boundary
     *            The boundary separating multipart entries.
     */
    public void setMultipartBoundary(String boundary) {
        if (boundary != null) {
            this.multipartBoundary = boundary;
            setMultipart(true);
        } else {
            this.multipartBoundary = DEFAULT_BOUNDARY;
        }
        setMediaType(createMultipartMediaType(getMultipartBoundary()));
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (isMultipart()) {
            for (FormData data : getEntries()) {
                // Write the boundary line
                outputStream.write(("--" + getMultipartBoundary()).getBytes());
                HeaderUtils.writeCRLF(outputStream);

                if (StringUtils.isNullOrEmpty(data.getFilename())
                        && MediaType.TEXT_PLAIN.equals(data.getMediaType())) {
                    // Write the content disposition header line, as a simple
                    // form field
                    String line = "Content-Disposition: form-data; name=\""
                            + data.getName() + "\"";
                    outputStream.write(line.getBytes());
                    HeaderUtils.writeCRLF(outputStream);
                } else {
                    // Write the content disposition header line as file
                    String line = "Content-Disposition: form-data; name=\""
                            + data.getName() + "\"; filename=\""
                            + data.getFilename() + "\"";
                    outputStream.write(line.getBytes());
                    HeaderUtils.writeCRLF(outputStream);

                    // Write the content type header line
                    line = "Content-Type: "
                            + ContentType.writeHeader(data
                                    .getValueRepresentation());
                    outputStream.write(line.getBytes());
                    HeaderUtils.writeCRLF(outputStream);
                }

                // Write the data content
                HeaderUtils.writeCRLF(outputStream);
                data.getValueRepresentation().write(outputStream);
                HeaderUtils.writeCRLF(outputStream);
            }

            // Write the final boundary line
            outputStream.write(("--" + getMultipartBoundary() + "--")
                    .getBytes());
            HeaderUtils.writeCRLF(outputStream);
        } else {
            Representation formRep = new StringRepresentation(getQueryString(),
                    MediaType.APPLICATION_WWW_FORM, null, CharacterSet.UTF_8);
            formRep.write(outputStream);
        }
    }
}
