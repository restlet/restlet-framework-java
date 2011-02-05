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

package org.restlet.data;

/**
 * Challenge scheme used to authenticate remote clients.
 * 
 * @author Jerome Louvel
 */
public final class ChallengeScheme {
    /** Custom scheme based on IP address or cookies or query parameters, etc. */
    public static final ChallengeScheme CUSTOM = new ChallengeScheme("CUSTOM",
            "Custom", "Custom authentication");

    /** Amazon S3 HTTP scheme. */
    public static final ChallengeScheme HTTP_AWS_S3 = new ChallengeScheme(
            "HTTP_AWS", "AWS", "Amazon S3 HTTP authentication");

    /** Basic HTTP scheme. */
    public static final ChallengeScheme HTTP_BASIC = new ChallengeScheme(
            "HTTP_BASIC", "Basic", "Basic HTTP authentication");

    /** Cookie HTTP scheme. */
    public static final ChallengeScheme HTTP_COOKIE = new ChallengeScheme(
            "HTTP_Cookie", "Cookie", "Cookie HTTP authentication");

    /** Digest HTTP scheme. */
    public static final ChallengeScheme HTTP_DIGEST = new ChallengeScheme(
            "HTTP_DIGEST", "Digest", "Digest HTTP authentication");

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

    /** Microsoft NTML HTTP scheme. */
    public static final ChallengeScheme HTTP_NTLM = new ChallengeScheme(
            "HTTP_NTLM", "NTLM", "Microsoft NTLM HTTP authentication");

    /** OAuth HTTP scheme. */
    public static final ChallengeScheme HTTP_OAUTH = new ChallengeScheme(
            "HTTP_OAuth", "OAuth", "Open protocol for API authentication");

    /** Basic POP scheme. Based on the USER/PASS commands. */
    public static final ChallengeScheme POP_BASIC = new ChallengeScheme(
            "POP_BASIC", "Basic",
            "Basic POP authentication (USER/PASS commands)");

    /** Digest POP scheme. Based on the APOP command. */
    public static final ChallengeScheme POP_DIGEST = new ChallengeScheme(
            "POP_DIGEST", "Digest", "Digest POP authentication (APOP command)");

    /** Plain SMTP scheme. */
    public static final ChallengeScheme SMTP_PLAIN = new ChallengeScheme(
            "SMTP_PLAIN", "PLAIN", "Plain SMTP authentication");

    /** Plain FTP scheme. */
    public static final ChallengeScheme FTP_PLAIN = new ChallengeScheme(
            "FTP_PLAIN", "PLAIN", "Plain FTP authentication");

    /**
     * Returns the challenge scheme associated to a scheme name. If an existing
     * constant exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The scheme name.
     * @return The associated challenge scheme.
     */
    public static ChallengeScheme valueOf(final String name) {
        ChallengeScheme result = null;

        if ((name != null) && !name.equals("")) {
            if (name.equalsIgnoreCase(CUSTOM.getName())) {
                result = CUSTOM;
            } else if (name.equalsIgnoreCase(HTTP_AWS_S3.getName())) {
                result = HTTP_AWS_S3;
            } else if (name.equalsIgnoreCase(HTTP_BASIC.getName())) {
                result = HTTP_BASIC;
            } else if (name.equalsIgnoreCase(HTTP_COOKIE.getName())) {
                result = HTTP_COOKIE;
            } else if (name.equalsIgnoreCase(HTTP_DIGEST.getName())) {
                result = HTTP_DIGEST;
            } else if (name.equalsIgnoreCase(HTTP_AZURE_SHAREDKEY.getName())) {
                result = HTTP_AZURE_SHAREDKEY;
            } else if (name.equalsIgnoreCase(HTTP_AZURE_SHAREDKEY_LITE
                    .getName())) {
                result = HTTP_AZURE_SHAREDKEY_LITE;
            } else if (name.equalsIgnoreCase(HTTP_NTLM.getName())) {
                result = HTTP_NTLM;
            } else if (name.equalsIgnoreCase(HTTP_OAUTH.getName())) {
                result = HTTP_OAUTH;
            } else if (name.equalsIgnoreCase(POP_BASIC.getName())) {
                result = POP_BASIC;
            } else if (name.equalsIgnoreCase(POP_DIGEST.getName())) {
                result = POP_DIGEST;
            } else if (name.equalsIgnoreCase(SMTP_PLAIN.getName())) {
                result = SMTP_PLAIN;
            } else {
                result = new ChallengeScheme(name, null, null);
            }
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
                && ((ChallengeScheme) object).getName().equalsIgnoreCase(
                        getName());
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
