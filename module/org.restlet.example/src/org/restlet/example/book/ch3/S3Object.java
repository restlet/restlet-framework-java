/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.example.book.ch3;

import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
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
