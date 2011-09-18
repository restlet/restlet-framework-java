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

package org.restlet.ext.crypto.internal;

import java.util.Date;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.util.DateUtils;
import org.restlet.representation.Representation;

/**
 * Implements the HTTP authentication for the Amazon SimpleDB service.
 * 
 * @author Jerome Louvel
 */
public class AwsSdbHelper extends AuthenticatorHelper {

    /**
     * Constructor.
     */
    public AwsSdbHelper() {
        super(ChallengeScheme.HTTP_AWS_SDB, true, false);
    }

    @Override
    public Representation updateEntity(Representation entity,
            ChallengeResponse challengeResponse, Request request) {
        Representation result = entity;

        if (MediaType.APPLICATION_WWW_FORM.equals(entity.getMediaType())) {
            Form form = new Form(entity);
            form.add("AWSAccessKeyId", new String(request
                    .getChallengeResponse().getSecret()));
            form.add("SignatureMethod", "HmacSHA256");
            form.add("SignatureVersion", "2");
            form.add("Version", "2009-04-15");
            String df = DateUtils.format(new Date(),
                    DateUtils.FORMAT_ISO_8601.get(0));
            form.add("Timestamp", df);

            // Compute then add the signature parameter
            String signature = AwsUtils.getSdbSignature(request.getMethod(),
                    request.getResourceRef(), form, request
                            .getChallengeResponse().getSecret());
            form.add("Signature", signature);
            result = form.getWebRepresentation();
        }

        return result;
    }
}
