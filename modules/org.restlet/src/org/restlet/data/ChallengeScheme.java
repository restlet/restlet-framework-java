/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

/**
 * Challenge scheme used to authenticate remote clients.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ChallengeScheme extends Metadata {
    /** Custom scheme based on IP address or cookies or query params, etc. */
    public static final ChallengeScheme CUSTOM = new ChallengeScheme("CUSTOM",
            "Custom", "Custom authentication");

    /** Amazon Web Services HTTP scheme. */
    public static final ChallengeScheme HTTP_AWS = new ChallengeScheme(
            "HTTP_AWS", "AWS", "Amazon Web Services HTTP authentication");

    /** Basic HTTP scheme. */
    public static final ChallengeScheme HTTP_BASIC = new ChallengeScheme(
            "HTTP_BASIC", "Basic", "Basic HTTP authentication");

    /** Digest HTTP scheme. */
    public static final ChallengeScheme HTTP_DIGEST = new ChallengeScheme(
            "HTTP_DIGEST", "Digest", "Digest HTTP authentication");

    /** Microsoft NTML HTTP scheme. */
    public static final ChallengeScheme HTTP_NTLM = new ChallengeScheme(
            "HTTP_NTLM", "NTLM", "Microsoft NTLM HTTP authentication");

    /** Plain SMTP scheme. */
    public static final ChallengeScheme SMTP_PLAIN = new ChallengeScheme(
            "SMTP_PLAIN", "PLAIN", "Plain SMTP authentication");

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

        if (name != null) {
            if (name.equalsIgnoreCase(CUSTOM.getName())) {
                result = CUSTOM;
            } else if (name.equalsIgnoreCase(HTTP_AWS.getName())) {
                result = HTTP_AWS;
            } else if (name.equalsIgnoreCase(HTTP_BASIC.getName())) {
                result = HTTP_BASIC;
            } else if (name.equalsIgnoreCase(HTTP_DIGEST.getName())) {
                result = HTTP_DIGEST;
            } else if (name.equalsIgnoreCase(HTTP_NTLM.getName())) {
                result = HTTP_NTLM;
            } else if (name.equalsIgnoreCase(SMTP_PLAIN.getName())) {
                result = SMTP_PLAIN;
            } else {
                result = new ChallengeScheme(name, null, null);
            }
        }

        return result;
    }

    /** The technical name. */
    private String technicalName;

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
    public ChallengeScheme(final String name, final String technicalName, final String description) {
        super(name, description);
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
}
