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

package org.restlet.example.ext.fileupload;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class FileUploadServerResource extends ServerResource {

    @Post
    public Representation accept(Representation entity) throws Exception {
        Representation result = null;
        if (entity != null
                && MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(),
                        true)) {
            // 1/ Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1000240);

            // 2/ Create a new file upload handler based on the Restlet
            // FileUpload extension that will parse Restlet requests and
            // generates FileItems.
            RestletFileUpload upload = new RestletFileUpload(factory);

            // 3/ Request is parsed by the handler which generates a
            // list of FileItems
            FileItemIterator fileIterator = upload.getItemIterator(entity);

            // Process only the uploaded item called "fileToUpload"
            // and return back
            boolean found = false;
            while (fileIterator.hasNext() && !found) {
                FileItemStream fi = fileIterator.next();

                if (fi.getFieldName().equals("fileToUpload")) {
                    // For the matter of sample code, it filters the multo
                    // part form according to the field name.
                    found = true;
                    // consume the stream immediately, otherwise the stream
                    // will be closed.
                    StringBuilder sb = new StringBuilder("media type: ");
                    sb.append(fi.getContentType()).append("\n");
                    sb.append("file name : ");
                    sb.append(fi.getName()).append("\n");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fi.openStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    sb.append("\n");
                    result = new StringRepresentation(sb.toString(),
                            MediaType.TEXT_PLAIN);
                }
            }
        } else {
            // POST request with no entity.
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        return result;
    }

}
