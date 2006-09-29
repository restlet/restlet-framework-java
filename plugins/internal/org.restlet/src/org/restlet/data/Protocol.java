/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

/**
 * Protocol used by client and server connectors. Connectors enable the communication between components
 * by implementing standard protocols.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Protocol extends Metadata
{
	/** AJP 1.3 protocol to communicate with Apache HTTP server or Microsoft IIS. */
	public static final Protocol AJP = new Protocol("ajp", "AJP",
			"Apache Jakarta Protocol", 8009);

	/** Context access protocol base on Java classloaders, Web application context loaders. */
	public static final Protocol CONTEXT = new Protocol("context", "CONTEXT",
			"Context Access Protocol", -1);

	/** Local file system access protocol. */
	public static final Protocol FILE = new Protocol("file", "FILE",
			"Local File System Protocol", -1);

	/** FTP protocol. */
	public static final Protocol FTP = new Protocol("ftp", "FTP",
			"File Transfer Protocol", 21);

	/** HTTP protocol. */
	public static final Protocol HTTP = new Protocol("http", "HTTP",
			"HyperText Transport Protocol", 80);

	/** HTTPS protocol (via SSL socket). */
	public static final Protocol HTTPS = new Protocol("https", "HTTPS",
			"HyperText Transport Protocol (Secure)", 443);

	/** JDBC protocol. */
	public static final Protocol JDBC = new Protocol("jdbc", "JDBC",
			"Java DataBase Connectivity", -1);

	/** SMTP protocol. */
	public static final Protocol SMTP = new Protocol("smtp", "SMTP",
			"Simple Mail Transfer Protocol", 25);

	/** SMTP with STARTTLS protocol (started with a plain socket). */
	public static final Protocol SMTP_STARTTLS = new Protocol("smtp", "SMTP_STARTTLS",
			"Simple Mail Transfer Protocol (starting a TLS encryption)", 25);

	/** SMTPS protocol (via SSL/TLS socket). */
	public static final Protocol SMTPS = new Protocol("smtps", "SMTPS",
			"Simple Mail Transfer Protocol (Secure)", 465);

	/** The scheme name. */
	private String schemeName;

	/** The default port if known or -1. */
	private int defaultPort;

	/**
	 * Constructor.
	 * @param schemeName The scheme name.
	 */
	public Protocol(String schemeName)
	{
		this(schemeName, schemeName.toUpperCase(), schemeName.toUpperCase() + " Protocol",
				-1);
	}

	/**
	 * Constructor.
	 * @param schemeName The scheme name.
	 * @param name The unique name.
	 * @param description The description.
	 * @param defaultPort The default port.
	 */
	public Protocol(String schemeName, String name, String description, int defaultPort)
	{
		super(name, description);
		this.schemeName = schemeName;
		this.defaultPort = defaultPort;
	}

	/**
	 * Returns the URI scheme name. 
	 * @return The URI scheme name.
	 */
	public String getSchemeName()
	{
		return this.schemeName;
	}

	/**
	 * Returns the default port number.
	 * @return The default port number.
	 */
	public int getDefaultPort()
	{
		return this.defaultPort;
	}

	/**
	 * Indicates if the protocol is equal to a given one.
	 * @param object The object to compare to.
	 * @return True if the protocol is equal to a given one.
	 */
	public boolean equals(Object object)
	{
		return (object instanceof Protocol)
				&& getName().equalsIgnoreCase(((Protocol) object).getName());
	}

	/**
	 * Creates the protocol associated to a URI scheme name. If an existing constant exists then it is returned,
	 * otherwise a new instance is created.
	 * @param schemeName The scheme name.
	 * @return The associated protocol.
	 */
	public static Protocol valueOf(String schemeName)
	{
		Protocol result = null;

		if (schemeName != null)
		{
			if (schemeName.equalsIgnoreCase(AJP.getSchemeName()))
				result = AJP;
			else if (schemeName.equalsIgnoreCase(CONTEXT.getSchemeName()))
				result = CONTEXT;
			else if (schemeName.equalsIgnoreCase(FILE.getSchemeName()))
				result = FILE;
			else if (schemeName.equalsIgnoreCase(HTTP.getSchemeName()))
				result = HTTP;
			else if (schemeName.equalsIgnoreCase(HTTPS.getSchemeName()))
				result = HTTPS;
			else if (schemeName.equalsIgnoreCase(JDBC.getSchemeName()))
				result = JDBC;
			else if (schemeName.equalsIgnoreCase(SMTP.getSchemeName()))
				result = SMTP;
			else if (schemeName.equalsIgnoreCase(SMTPS.getSchemeName()))
				result = SMTPS;
			else
				result = new Protocol(schemeName);
		}

		return result;
	}
}
