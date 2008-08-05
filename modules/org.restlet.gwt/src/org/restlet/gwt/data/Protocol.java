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

package org.restlet.gwt.data;

/**
 * Protocol used by client and server connectors. Connectors enable the
 * communication between components by implementing standard protocols.
 * 
 * @author Jerome Louvel
 */
public final class Protocol extends Metadata {

    /** Indicates that the port number is undefined. */
    public static final int UNKNOWN_PORT = -1;

    /** All protocols wildcard. */
    public static final Protocol ALL = new Protocol("all", "ALL",
            "Wildcard for all protocols", UNKNOWN_PORT);

    /**
     * FILE is a standard scheme to access to representations stored in the file
     * system (locally most of the time). Example URI:
     * "file:///D/root/index.html".<br>
     * <br>
     * In order to work, FILE requires a client connector provided by the core
     * Restlet engine.
     */
    public static final Protocol FILE = new Protocol("file", "FILE",
            "Local File System Protocol", UNKNOWN_PORT);

    /** FTP protocol. */
    public static final Protocol FTP = new Protocol("ftp", "FTP",
            "File Transfer Protocol", 21);

    /** HTTP protocol. */
    public static final Protocol HTTP = new Protocol("http", "HTTP",
            "HyperText Transport Protocol", 80);

    /** HTTPS protocol (via SSL socket). */
    public static final Protocol HTTPS = new Protocol("https", "HTTPS",
            "HyperText Transport Protocol (Secure)", 443);

    /**
     * Creates the protocol associated to a URI scheme name. If an existing
     * constant exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The scheme name.
     * @return The associated protocol.
     */
    public static Protocol valueOf(final String name) {
        Protocol result = null;

        if ((name != null) && !name.equals("")) {
            if (name.equalsIgnoreCase(FILE.getSchemeName())) {
                result = FILE;
            } else if (name.equalsIgnoreCase(FTP.getSchemeName())) {
                result = FTP;
            } else if (name.equalsIgnoreCase(HTTP.getSchemeName())) {
                result = HTTP;
            } else if (name.equalsIgnoreCase(HTTPS.getSchemeName())) {
                result = HTTPS;
            } else {
                result = new Protocol(name);
            }
        }

        return result;
    }

    /** The default port if known or -1. */
    private volatile int defaultPort;

    /** The scheme name. */
    private volatile String schemeName;

    /**
     * Constructor.
     * 
     * @param schemeName
     *            The scheme name.
     */
    public Protocol(final String schemeName) {
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
    public Protocol(final String schemeName, final String name,
            final String description, int defaultPort) {
        super(name, description);
        this.schemeName = schemeName;
        this.defaultPort = defaultPort;
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
     * Returns the URI scheme name.
     * 
     * @return The URI scheme name.
     */
    public String getSchemeName() {
        return this.schemeName;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }
}
