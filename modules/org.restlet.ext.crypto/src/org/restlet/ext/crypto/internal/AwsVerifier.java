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

package org.restlet.ext.crypto.internal;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.util.DateUtils;
import org.restlet.security.LocalVerifier;
import org.restlet.security.SecretVerifier;
import org.restlet.security.User;
import org.restlet.util.Series;

/**
 * Wrapped verifier that can verify HTTP requests utilizing the Amazon S3
 * authentication scheme. Verifies the user by computing the request signature
 * using the local secret and comparing it to the signature provided in the
 * request.
 * <p>
 * Per the Amazon S3 specification the {@code Date} header is required. If the
 * {@code Date} header is missing or the request is older than the allowed time
 * limit, specified by the {@code maxRequestAge} property, the request fails
 * verification.
 * 
 * @author Jean-Philippe Steinmetz <caskater47@gmail.com>
 * @see <a
 *      href="http://docs.amazonwebservices.com/AmazonS3/latest/RESTAuthentication.html">
 *      Authenticating REST Requests</a>
 */
public class AwsVerifier extends SecretVerifier {
    /**
     * Default maximum request age (15 minutes)
     */
    private static final long DEFAULT_MAX_REQUEST_AGE = 15 * 60 * 1000L;

    /**
     * The maximum age of a request, in milliseconds, before it is considered
     * stale.
     */
    private long maxRequestAge;

    /** The local secret verifier. */
    private LocalVerifier wrappedVerifier;

    /**
     * Creates a new HttpAwsS3Verifier instance.
     * 
     * @param wrappedVerifier
     *            The wrapped verifier containing local identifier/secret
     *            couples
     */
    public AwsVerifier(LocalVerifier wrappedVerifier) {
        this(wrappedVerifier, DEFAULT_MAX_REQUEST_AGE);
    }

    /**
     * Creates a new HttpAwsS3Verifier instance.
     * 
     * @param wrappedVerifier
     *            The wrapped verifier containing local identifier/secret
     *            couples
     * @param maxRequestAge
     *            The maximum age of a request, in milliseconds, before it is
     *            considered stale
     */
    public AwsVerifier(LocalVerifier wrappedVerifier, long maxRequestAge) {
        super();
        setMaxRequestAge(maxRequestAge);
        setWrappedVerifier(wrappedVerifier);
    }

    /**
     * Returns the user identifier portion of an Amazon S3 compatible
     * {@code Authorization} header.
     * <p>
     * An Amazon S3 compatible {@code Authorization} header has the following
     * pattern.<br/>
     * {@code Authorization: AWS id:signature}
     */
    @Override
    protected String getIdentifier(Request request, Response response) {
        if (request.getChallengeResponse() == null
                || request.getChallengeResponse().getRawValue() == null)
            return null;

        String[] parts = request.getChallengeResponse().getRawValue()
                .split(":");

        if (parts != null && parts.length == 2)
            return parts[0];
        else
            return null;
    }

    /**
     * Returns the local secret associated to a given identifier.
     * 
     * @param identifier
     *            The identifier to lookup.
     * @return The secret associated to the identifier or null.
     */
    public char[] getLocalSecret(String identifier) {
        char[] result = null;
        result = getWrappedVerifier().getLocalSecret(identifier);
        return result;
    }

    /**
     * Returns the maximum age of a request, in milliseconds, before it is
     * considered stale.
     * <p>
     * A negative or zero value indicates no age restriction. The default value
     * is 15 minutes.
     */
    public long getMaxRequestAge() {
        return this.maxRequestAge;
    }

    /**
     * Returns the signature portion of an Amazon S3 compatible
     * {@code Authorization} header.
     * <p>
     * An Amazon S3 compatible {@code Authorization} header has the following
     * pattern.<br/>
     * {@code Authorization: AWS id:signature}
     */
    @Override
    protected char[] getSecret(Request request, Response response) {
        if (request.getChallengeResponse() == null
                || request.getChallengeResponse().getRawValue() == null)
            return null;

        String[] parts = request.getChallengeResponse().getRawValue()
                .split(":");

        if (parts != null && parts.length == 2)
            return parts[1].toCharArray();
        else
            return null;
    }

    /**
     * Returns the wrapped local secret verifier.
     * 
     * @return The local secret verifier.
     */
    public LocalVerifier getWrappedVerifier() {
        return wrappedVerifier;
    }

    /**
     * Sets the maximum age of a request, in milliseconds, before it is
     * considered stale.
     * <p>
     * A negative or zero value indicates no age restriction. The default value
     * is 15 minutes.
     */
    public void setMaxRequestAge(long value) {
        if (value < 0)
            value = 0;
        this.maxRequestAge = value;
    }

    /**
     * Sets the wrapped local secret verifier.
     * 
     * @param wrappedVerifier
     *            The local secret verifier.
     */
    public void setWrappedVerifier(LocalVerifier wrappedVerifier) {
        this.wrappedVerifier = wrappedVerifier;
    }

    @Override
    public int verify(Request request, Response response) {
        if (request.getChallengeResponse() == null)
            return RESULT_MISSING;

        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) request.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
        String userId = getIdentifier(request, response);

        if (userId == null || (userId.length() == 0))
            return RESULT_MISSING;

        // A date header is always required
        if (headers.getFirstValue(HeaderConstants.HEADER_DATE, true) == null)
            return RESULT_INVALID;

        // Make sure the date is not stale
        if (getMaxRequestAge() > 0) {
            Long date = DateUtils.parse(
                    headers.getFirstValue(HeaderConstants.HEADER_DATE, true))
                    .getTime();
            Long now = System.currentTimeMillis();
            if (now - date > getMaxRequestAge())
                return RESULT_STALE;
        }

        char[] userSecret = getLocalSecret(userId);
        char[] signature = getSecret(request, response);
        String sigToCompare = AwsUtils.getS3Signature(request, userSecret);

        if (!compare(signature, sigToCompare.toCharArray()))
            return RESULT_INVALID;

        request.getClientInfo().setUser(new User(userId));

        return RESULT_VALID;
    }

    /**
     * This function is not implemented because the authorization scheme
     * requires direct access to the request. See
     * {@link #verify(Request, Response)}.
     */
    @Override
    public int verify(String identifier, char[] secret)
            throws IllegalArgumentException {
        throw new RuntimeException("Method not implemented");
    }
}
