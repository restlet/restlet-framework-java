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

import org.restlet.engine.util.StringUtils;

/**
 * Protocol used by client and server connectors. Connectors enable the
 * communication between components by implementing standard protocols.
 * 
 * @author Jerome Louvel
 */
public final class Protocol {

    /** Indicates that the port number is undefined. */
    public static final int UNKNOWN_PORT = -1;

    /**
     * AJP 1.3 protocol to communicate with Apache HTTP server or Microsoft IIS.
     */
    public static final Protocol AJP = new Protocol("ajp", "AJP",
            "Apache Jakarta Protocol", 8009);

    /** All protocols wildcard. */
    public static final Protocol ALL = new Protocol("all", "ALL",
            "Wildcard for all protocols", UNKNOWN_PORT);

    /**
     * CLAP (ClassLoader Access Protocol) is a custom scheme to access to
     * representations via classloaders. Example URI:
     * "clap://thread/org/restlet/Restlet.class".<br>
     * <br>
     * In order to work, CLAP requires a client connector provided by the core
     * Restlet engine.
     * 
     * @see org.restlet.data.LocalReference
     */
    public static final Protocol CLAP = new Protocol("clap", "CLAP",
            "Class Loader Access Protocol", UNKNOWN_PORT, true);

    /**
     * FILE is a standard scheme to access to representations stored in the file
     * system (locally most of the time). Example URI:
     * "file:///D/root/index.html".<br>
     * <br>
     * In order to work, FILE requires a client connector provided by the core
     * Restlet engine.
     * 
     * @see org.restlet.data.LocalReference
     */
    public static final Protocol FILE = new Protocol("file", "FILE",
            "Local File System Protocol", UNKNOWN_PORT, true);

    /** FTP protocol. */
    public static final Protocol FTP = new Protocol("ftp", "FTP",
            "File Transfer Protocol", 21);

    /** HTTP protocol. */
    public static final Protocol HTTP = new Protocol("http", "HTTP",
            "HyperText Transport Protocol", 80, "1.1");

    /** HTTPS protocol (via SSL socket). */
    public static final Protocol HTTPS = new Protocol("https", "HTTPS", "HTTP",
            "HyperText Transport Protocol (Secure)", 443, true, "1.1");

    /**
     * JAR (Java ARchive) is a common scheme to access to representations inside
     * archive files. Example URI:
     * "jar:http://www.foo.com/bar/baz.jar!/COM/foo/Quux.class".
     * 
     * @see org.restlet.data.LocalReference#createJarReference(Reference,
     *      String)
     */
    public static final Protocol JAR = new Protocol("jar", "JAR",
            "Java ARchive", UNKNOWN_PORT, true);

    /** JDBC protocol. */
    public static final Protocol JDBC = new Protocol("jdbc", "JDBC",
            "Java DataBase Connectivity", UNKNOWN_PORT);

    // [ifdef osgi] member
    /**
     * OBAP (OSGi Bundle Access Protocol) is a custom scheme to access to
     * representations via bundles referenced using their symbolic name. Example
     * URI: "obap://org.restlet/org/restlet/Restlet.class".<br>
     * <br>
     * In order to work, OBAP requires a client connector provided by the
     * extension for the OSGi platform.
     */
    public static final Protocol OBAP = new Protocol("obap", "OBAP",
            "OSGi Bundle Access Protocol", Protocol.UNKNOWN_PORT, true);

    /** POP protocol. */
    public static final Protocol POP = new Protocol("pop", "POP",
            "Post Office Protocol", 110);

    /** POPS protocol (via SSL/TLS socket).. */
    public static final Protocol POPS = new Protocol("pops", "POPS",
            "Post Office Protocol (Secure)", 995, true);

    /**
     * RIAP (Restlet Internal Access Protocol) is a custom scheme to access
     * representations via internal calls to virtual hosts/components. Example
     * URIs: "riap://component/myAppPath/myResource" and
     * "riap://application/myResource".<br>
     * <br>
     * In order to work, RIAP doesn't requires any client connector and is
     * automatically supported by the Restlet engine.
     * 
     * @see org.restlet.data.LocalReference
     */
    public static final Protocol RIAP = new Protocol("riap", "RIAP",
            "Restlet Internal Access Protocol", UNKNOWN_PORT, true);

    /**
     * SDC (Secure Data Connector) protocol. <br>
     * <br>
     * SDC is natively available on the Google App Engine platform and via a
     * special Restlet extension on other platforms.
     */
    public static final Protocol SDC = new Protocol("sdc", "SDC",
            "Secure Data Connector Protocol", UNKNOWN_PORT, true);

    /** SIP protocol. */
    public static final Protocol SIP = new Protocol("sip", "SIP",
            "Session Initiation Protocol", 5060, "2.0");

    /** SIPS protocol (via SSL socket). */
    public static final Protocol SIPS = new Protocol("sips", "SIPS", "SIP",
            "Session Initiation Protocol (Secure)", 5061, true, "2.0");

    /** SMTP protocol. */
    public static final Protocol SMTP = new Protocol("smtp", "SMTP",
            "Simple Mail Transfer Protocol", 25);

    /** SMTPS protocol (via SSL/TLS socket). */
    public static final Protocol SMTPS = new Protocol("smtps", "SMTPS",
            "Simple Mail Transfer Protocol (Secure)", 465, true);

    /** Local Web Archive access protocol. */
    public static final Protocol WAR = new Protocol("war", "WAR",
            "Web Archive Access Protocol", UNKNOWN_PORT, true);

    /**
     * ZIP is a special scheme to access to representations inside Zip archive
     * files. Example URI: "zip:file:///tmp/test.zip!/test.txt".
     * 
     * @see org.restlet.data.LocalReference#createZipReference(Reference,
     *      String)
     */
    public static final Protocol ZIP = new Protocol("zip", "ZIP",
            "Zip Archive Access Protocol", UNKNOWN_PORT, true);

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
            if (name.equalsIgnoreCase(AJP.getSchemeName())) {
                result = AJP;
            } else if (name.equalsIgnoreCase(CLAP.getSchemeName())) {
                result = CLAP;
            } else if (name.equalsIgnoreCase(FILE.getSchemeName())) {
                result = FILE;
            } else if (name.equalsIgnoreCase(FTP.getSchemeName())) {
                result = FTP;
            } else if (name.equalsIgnoreCase(HTTP.getSchemeName())) {
                result = HTTP;
            } else if (name.equalsIgnoreCase(HTTPS.getSchemeName())) {
                result = HTTPS;
            } else if (name.equalsIgnoreCase(JAR.getSchemeName())) {
                result = JAR;
            } else if (name.equalsIgnoreCase(JDBC.getSchemeName())) {
                result = JDBC;
            } else if (name.equalsIgnoreCase(POP.getSchemeName())) {
                result = POP;
            } else if (name.equalsIgnoreCase(POPS.getSchemeName())) {
                result = POPS;
            } else if (name.equalsIgnoreCase(RIAP.getSchemeName())) {
                result = RIAP;
            } else if (name.equalsIgnoreCase(SMTP.getSchemeName())) {
                result = SMTP;
            } else if (name.equalsIgnoreCase(SMTPS.getSchemeName())) {
                result = SMTPS;
            } else if (name.equalsIgnoreCase(SIP.getSchemeName())) {
                result = SIP;
            } else if (name.equalsIgnoreCase(SIPS.getSchemeName())) {
                result = SIPS;
            } else if (name.equalsIgnoreCase(WAR.getSchemeName())) {
                result = WAR;
            } else if (name.equalsIgnoreCase(ZIP.getSchemeName())) {
                result = ZIP;
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
    private volatile String schemeName;

    /** The technical name that appears on the wire. */
    private final String technicalName;

    /** The version. */
    private volatile String version;

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
     * Indicates if the protocol guarantees the confidentially of the messages
     * exchanged, for example via a SSL-secured connection.
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
