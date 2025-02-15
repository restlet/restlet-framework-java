/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.data;

import org.restlet.client.engine.util.StringUtils;

/**
 * Protocol used by client and server connectors. Connectors enable the
 * communication between components by implementing standard protocols.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
public final class Protocol {

    /** Indicates that the port number is undefined. */
    public static final int UNKNOWN_PORT = -1;

    /** All protocols' wildcard. */
    public static final Protocol ALL = new Protocol("all", "ALL",
            "Wildcard for all protocols", UNKNOWN_PORT);

    /** FTP protocol. */
    public static final Protocol FTP = new Protocol("ftp", "FTP",
            "File Transfer Protocol", 21);

    /** HTTP protocol. */
    public static final Protocol HTTP = new Protocol("http", "HTTP",
            "HyperText Transport Protocol", 80, "1.1");

    /** HTTPS protocol (via SSL socket). */
    public static final Protocol HTTPS = new Protocol("https", "HTTPS", "HTTP",
            "HyperText Transport Protocol (Secure)", 443, true, "1.1");

    /** POP protocol. */
    @Deprecated
    public static final Protocol POP = new Protocol("pop", "POP",
            "Post Office Protocol", 110);

    /** POPS protocol (via SSL/TLS socket).. */
    @Deprecated
    public static final Protocol POPS = new Protocol("pops", "POPS",
            "Post Office Protocol (Secure)", 995, true);

    /**
     * SDC (Secure Data Connector) protocol. <br>
     * <br>
     * SDC is natively available on the Google App Engine platform and via a
     * special Restlet extension on other platforms.
     */
    @Deprecated
    public static final Protocol SDC = new Protocol("sdc", "SDC",
            "Secure Data Connector Protocol", UNKNOWN_PORT, true);

    /** SIP protocol. */
    @Deprecated
    public static final Protocol SIP = new Protocol("sip", "SIP",
            "Session Initiation Protocol", 5060, "2.0");

    /** SIPS protocol (via SSL socket). */
    @Deprecated
    public static final Protocol SIPS = new Protocol("sips", "SIPS", "SIP",
            "Session Initiation Protocol (Secure)", 5061, true, "2.0");

    /** SMTP protocol. */
    @Deprecated
    public static final Protocol SMTP = new Protocol("smtp", "SMTP",
            "Simple Mail Transfer Protocol", 25);

    /** SMTPS protocol (via SSL/TLS socket). */
    @Deprecated
    public static final Protocol SMTPS = new Protocol("smtps", "SMTPS",
            "Simple Mail Transfer Protocol (Secure)", 465, true);

    /**
     * Creates the protocol associated to a URI scheme name. If an existing
     * constant exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The scheme name.
     * @return The associated protocol.
     */
    public static Protocol valueOf(String name) {
        Protocol result = null;

        if (!StringUtils.isNullOrEmpty(name)) {
            if (name.equalsIgnoreCase(FTP.getSchemeName())) {
                result = FTP;
            } else if (name.equalsIgnoreCase(HTTP.getSchemeName())) {
                result = HTTP;
            } else if (name.equalsIgnoreCase(HTTPS.getSchemeName())) {
                result = HTTPS;
            } else if (name.equalsIgnoreCase(POP.getSchemeName())) {
                result = POP;
            } else if (name.equalsIgnoreCase(POPS.getSchemeName())) {
                result = POPS;
            } else if (name.equalsIgnoreCase(SMTP.getSchemeName())) {
                result = SMTP;
            } else if (name.equalsIgnoreCase(SMTPS.getSchemeName())) {
                result = SMTPS;
            } else if (name.equalsIgnoreCase(SIP.getSchemeName())) {
                result = SIP;
            } else if (name.equalsIgnoreCase(SIPS.getSchemeName())) {
                result = SIPS;
            } else {
                result = new Protocol(name);
            }
        }

        return result;
    }

    /**
     * Creates the protocol associated to a URI scheme name. If an existing
     * constant exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The scheme name.
     * @param version
     *            The version number.
     * @return The associated protocol.
     */
    public static Protocol valueOf(String name, String version) {
        Protocol result = valueOf(name);

        if (!version.equals(result.getVersion())) {
            result = new Protocol(result.getSchemeName(), result.getName(),
                    result.getTechnicalName(), result.getDescription(),
                    result.getDefaultPort(), result.isConfidential(), version);
        }

        return result;
    }

    /** The confidentiality. */
    private final boolean confidential;

    /** The default port if known or -1. */
    private final int defaultPort;

    /** The description. */
    private final String description;

    /** The name. */
    private final String name;

    /** The scheme name. */
    private final String schemeName;

    /** The technical name that appears on the wire. */
    private final String technicalName;

    /** The version. */
    private final String version;

    /**
     * Constructor.
     * 
     * @param schemeName
     *            The scheme name.
     */
    public Protocol(String schemeName) {
        this(schemeName, schemeName.toUpperCase(), schemeName.toUpperCase()
                + " Protocol", UNKNOWN_PORT);
    }

    /**
     * Constructor.
     * 
     * @param schemeName
     *            The scheme name.
     * @param name
     *            The unique name.
     * @param description
     *            The description.
     * @param defaultPort
     *            The default port.
     */
    public Protocol(String schemeName, String name, String description,
            int defaultPort) {
        this(schemeName, name, description, defaultPort, false);
    }

    /**
     * Constructor.
     * 
     * @param schemeName
     *            The scheme name.
     * @param name
     *            The unique name.
     * @param description
     *            The description.
     * @param defaultPort
     *            The default port.
     * @param confidential
     *            The confidentiality.
     */
    public Protocol(String schemeName, String name, String description,
            int defaultPort, boolean confidential) {
        this(schemeName, name, description, defaultPort, confidential, null);
    }

    /**
     * Constructor.
     * 
     * @param schemeName
     *            The scheme name.
     * @param name
     *            The unique name.
     * @param description
     *            The description.
     * @param defaultPort
     *            The default port.
     * @param confidential
     *            The confidentiality.
     * @param version
     *            The version.
     */
    public Protocol(String schemeName, String name, String description,
            int defaultPort, boolean confidential, String version) {
        this(schemeName, name, name, description, defaultPort, confidential,
                version);
    }

    /**
     * Constructor.
     * 
     * @param schemeName
     *            The scheme name.
     * @param name
     *            The unique name.
     * @param description
     *            The description.
     * @param defaultPort
     *            The default port.
     * @param version
     *            The version.
     */
    public Protocol(String schemeName, String name, String description,
            int defaultPort, String version) {
        this(schemeName, name, description, defaultPort, false, version);
    }

    /**
     * Constructor.
     * 
     * @param schemeName
     *            The scheme name.
     * @param name
     *            The unique name.
     * @param technicalName
     *            The technical name that appears on the wire.
     * @param description
     *            The description.
     * @param defaultPort
     *            The default port.
     * @param confidential
     *            The confidentiality.
     * @param version
     *            The version.
     */
    public Protocol(String schemeName, String name, String technicalName,
            String description, int defaultPort, boolean confidential,
            String version) {
        this.name = name;
        this.description = description;
        this.schemeName = schemeName;
        this.technicalName = technicalName;
        this.defaultPort = defaultPort;
        this.confidential = confidential;
        this.version = version;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        return (object instanceof Protocol)
                && getName().equalsIgnoreCase(((Protocol) object).getName());
    }

    /**
     * Returns the default port number.
     * 
     * @return The default port number.
     */
    public int getDefaultPort() {
        return this.defaultPort;
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
     * Returns the URI scheme name.
     * 
     * @return The URI scheme name.
     */
    public String getSchemeName() {
        return this.schemeName;
    }

    /**
     * Returns the technical name that appears on the wire.
     * 
     * @return The technical name that appears on the wire.
     */
    public String getTechnicalName() {
        return technicalName;
    }

    /**
     * Returns the version.
     * 
     * @return The version.
     */
    public String getVersion() {
        return version;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }

    /**
     * Indicates if the protocol guarantees the confidentiality of the messages
     * exchanged, for example, via an SSL-secured connection.
     * 
     * @return True if the protocol is confidential.
     */
    public boolean isConfidential() {
        return this.confidential;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    @Override
    public String toString() {
        return getName() + ((getVersion() == null) ? "" : "/" + getVersion());
    }

}
