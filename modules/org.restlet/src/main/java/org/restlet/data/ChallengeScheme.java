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

package org.restlet.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Challenge scheme used to authenticate remote clients.
 * 
 * @author Jerome Louvel
 */
public final class ChallengeScheme {
    /** Custom scheme based on IP address or cookies or query parameters, etc. */
    public static final ChallengeScheme CUSTOM = new ChallengeScheme("CUSTOM",
            "Custom", "Custom authentication");

    /** Plain FTP scheme. */
    public static final ChallengeScheme FTP_PLAIN = new ChallengeScheme(
            "FTP_PLAIN", "PLAIN", "Plain FTP authentication");

    /** Amazon Query String HTTP scheme. */
    public static final ChallengeScheme HTTP_AWS_IAM = new ChallengeScheme(
            "HTTP_AWS_IAM", "AWS3", "Amazon IAM-based authentication");

    /** Amazon Query String HTTP scheme. */
    public static final ChallengeScheme HTTP_AWS_QUERY = new ChallengeScheme(
            "HTTP_AWS_QUERY", "AWS_QUERY", "Amazon Query String authentication");

    /** Amazon S3 HTTP scheme. */
    public static final ChallengeScheme HTTP_AWS_S3 = new ChallengeScheme(
            "HTTP_AWS_S3", "AWS", "Amazon S3 HTTP authentication");

    /**
     * Microsoft Azure Shared Key scheme.
     * 
     * @see <a
     *      href="http://msdn.microsoft.com/en-us/library/dd179428.aspx#Subheading2">MSDN
     *      page</a>
     */
    public static final ChallengeScheme HTTP_AZURE_SHAREDKEY = new ChallengeScheme(
            "HTTP_AZURE_SHAREDKEY", "SharedKey",
            "Microsoft Azure Shared Key authorization (authentication)");

    /**
     * Microsoft Azure Shared Key lite scheme.
     * 
     * @see <a
     *      href="http://msdn.microsoft.com/en-us/library/dd179428.aspx#Subheading2">MSDN
     *      page</a>
     */
    public static final ChallengeScheme HTTP_AZURE_SHAREDKEY_LITE = new ChallengeScheme(
            "HTTP_AZURE_SHAREDKEY_LITE", "SharedKeyLite",
            "Microsoft Azure Shared Key lite authorization (authentication)");

    /** Basic HTTP scheme. */
    public static final ChallengeScheme HTTP_BASIC = new ChallengeScheme(
            "HTTP_BASIC", "Basic", "Basic HTTP authentication");

    /** Cookie HTTP scheme. */
    public static final ChallengeScheme HTTP_COOKIE = new ChallengeScheme(
            "HTTP_Cookie", "Cookie", "Cookie HTTP authentication");

    /** Digest HTTP scheme. */
    public static final ChallengeScheme HTTP_DIGEST = new ChallengeScheme(
            "HTTP_DIGEST", "Digest", "Digest HTTP authentication");

    /** Microsoft NTML HTTP scheme. */
    public static final ChallengeScheme HTTP_NTLM = new ChallengeScheme(
            "HTTP_NTLM", "NTLM", "Microsoft NTLM HTTP authentication");

    /**
     * OAuth 1.0 HTTP scheme. Removed in later drafts and final OAuth 2.0
     * specification.
     */
    public static final ChallengeScheme HTTP_OAUTH = new ChallengeScheme(
            "HTTP_OAuth", "OAuth", "OAuth 1.0 authentication");

    /** OAuth Bearer HTTP scheme. */
    public static final ChallengeScheme HTTP_OAUTH_BEARER = new ChallengeScheme(
            "HTTP_Bearer", "Bearer", "OAuth 2.0 bearer token authentication");

    /** OAuth MAC HTTP scheme. */
    public static final ChallengeScheme HTTP_OAUTH_MAC = new ChallengeScheme(
            "HTTP_MAC", "Mac",
            "OAuth 2.0 message authentication code authentication");

    /** Basic POP scheme. Based on the USER/PASS commands. */
    public static final ChallengeScheme POP_BASIC = new ChallengeScheme(
            "POP_BASIC", "Basic",
            "Basic POP authentication (USER/PASS commands)");

    /** Digest POP scheme. Based on the APOP command. */
    public static final ChallengeScheme POP_DIGEST = new ChallengeScheme(
            "POP_DIGEST", "Digest", "Digest POP authentication (APOP command)");

    /** Private list of schemes for optimization purpose. */
    private static Map<String, ChallengeScheme> SCHEMES;

    /** Secure Data Connector scheme. */
    public static final ChallengeScheme SDC = new ChallengeScheme("SDC", "SDC",
            "Secure Data Connector authentication");

    /** Plain SMTP scheme. */
    public static final ChallengeScheme SMTP_PLAIN = new ChallengeScheme(
            "SMTP_PLAIN", "PLAIN", "Plain SMTP authentication");

    static {
        Map<String, ChallengeScheme> schemes = new HashMap<String, ChallengeScheme>();

        schemes.put(CUSTOM.getName().toLowerCase(), CUSTOM);
        schemes.put(FTP_PLAIN.getName().toLowerCase(), FTP_PLAIN);
        schemes.put(HTTP_AWS_IAM.getName().toLowerCase(), HTTP_AWS_S3);
        schemes.put(HTTP_AWS_QUERY.getName().toLowerCase(), HTTP_AWS_S3);
        schemes.put(HTTP_AWS_S3.getName().toLowerCase(), HTTP_AWS_S3);
        schemes.put(HTTP_AZURE_SHAREDKEY.getName().toLowerCase(),
                HTTP_AZURE_SHAREDKEY);
        schemes.put(HTTP_AZURE_SHAREDKEY_LITE.getName().toLowerCase(),
                HTTP_AZURE_SHAREDKEY_LITE);
        schemes.put(HTTP_BASIC.getName().toLowerCase(), HTTP_BASIC);
        schemes.put(HTTP_COOKIE.getName().toLowerCase(), HTTP_COOKIE);
        schemes.put(HTTP_DIGEST.getName().toLowerCase(), HTTP_DIGEST);
        schemes.put(HTTP_NTLM.getName().toLowerCase(), HTTP_NTLM);
        schemes.put(HTTP_OAUTH.getName().toLowerCase(), HTTP_OAUTH);
        schemes.put(HTTP_OAUTH_BEARER.getName().toLowerCase(), HTTP_OAUTH);
        schemes.put(HTTP_OAUTH_MAC.getName().toLowerCase(), HTTP_OAUTH);
        schemes.put(POP_BASIC.getName().toLowerCase(), POP_BASIC);
        schemes.put(POP_DIGEST.getName().toLowerCase(), POP_DIGEST);
        schemes.put(SDC.getName().toLowerCase(), SDC);
        schemes.put(SMTP_PLAIN.getName().toLowerCase(), SMTP_PLAIN);

        ChallengeScheme.SCHEMES = Collections.unmodifiableMap(schemes);
    }

    /**
     * Returns the challenge scheme associated to a scheme name. If an existing
     * constant exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The scheme name.
     * @return The associated challenge scheme.
     */
    public static ChallengeScheme valueOf(final String name) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "ChallengeScheme.valueOf(name) name must not be null");
        }

        ChallengeScheme result = SCHEMES.get(name.toLowerCase());

        if (result == null) {
            result = new ChallengeScheme(name, null, null);
        }

        return result;
    }

    /** The description. */
    private final String description;

    /** The name. */
    private volatile String name;

    /** The technical name. */
    private volatile String technicalName;

    /**
     * Constructor.
     * 
     * @param name
     *            The unique name.
     * @param technicalName
     *            The technical name.
     */
    public ChallengeScheme(final String name, final String technicalName) {
        this(name, technicalName, null);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The unique name.
     * @param technicalName
     *            The technical name.
     * @param description
     *            The description.
     */
    public ChallengeScheme(final String name, final String technicalName,
            final String description) {
        this.name = name;
        this.description = description;
        this.technicalName = technicalName;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        return (object instanceof ChallengeScheme)
                && ((ChallengeScheme) object).getName().equalsIgnoreCase(getName());
    }

    /**
     * Returns the description.
     * 
     * @return The description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the technical name (ex: BASIC).
     * 
     * @return The technical name (ex: BASIC).
     */
    public String getTechnicalName() {
        return this.technicalName;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }

    /**
     * Sets the technical name (ex: BASIC).
     * 
     * @param technicalName
     *            The technical name (ex: BASIC).
     */
    @SuppressWarnings("unused")
    private void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    @Override
    public String toString() {
        return getName();
    }

}
