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

package org.restlet.example.book.rest.ch3;

import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Amazon S3 object.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class S3Object extends S3Authorized {

    private S3Bucket bucket;

    private Variant metadata;

    private String name;

    public S3Object(S3Bucket bucket, String name) {
        this.bucket = bucket;
        this.name = name;
    }

    /**
     * Retrieves the metadata hash for this object, possibly fetchingit from S3.
     * 
     * @return The metadata hash for this object, possibly fetchingit from S3.
     */
    public Variant getMetadata() {
        if (this.metadata == null)
            this.metadata = authorizedHead(getUri()).getEntity();
        return this.metadata;
    }

    /**
     * Retrieves the value of this object, always fetching it (along with the
     * metadata) from S3.
     * 
     * @return The value of this object.
     */
    public Representation getValue() {
        return authorizedGet(getUri()).getEntity();
    }

    /**
     * Store this object on S3 with a given value.
     * 
     * @param value
     *            The value of the object to store.
     */
    public Status save(Representation value) {
        this.metadata = value;
        return authorizedPut(getUri(), value).getStatus();
    }

    /**
     * Deletes this bucket.
     */
    public Status delete() {
        return authorizedDelete(getUri()).getStatus();
    }

    public String getUri() {
        return getBucket().getUri() + "/" + Reference.encode(getName());
    }

    public S3Bucket getBucket() {
        return this.bucket;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
