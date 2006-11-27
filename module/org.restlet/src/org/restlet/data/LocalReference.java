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

import java.io.File;
import java.io.IOException;

/**
 * Reference to a local resource. It has helper methods for the three following
 * schemes: CLAP, FILE and JAR.<br/> <br/> CLAP (ClassLoader Access Protocol)
 * is a custom scheme to access to representations via classloaders. Exemple
 * URI: "clap://thread/org/restlet/Restlet.class".<br/> <br/> JAR is a common
 * scheme to access to representations inside Java ARchives. Exemple URI:
 * "jar:http://www.foo.com/bar/baz.jar!/COM/foo/Quux.class".<br/> <br/> FILE is
 * a standard scheme to access to representations stored in the file system
 * (locally most of the time). Example URI: "file:///D/root/index.html".
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class LocalReference extends Reference {
	/**
	 * The resources will be resolved from the classloader associated with the
	 * local class. Examples: clap://class/rootPkg/subPkg/myClass.class or
	 * clap://class/rootPkg/file.html
	 * 
	 * @see java.lang.Class#getClassLoader()
	 */
	public static final int CLAP_CLASS = 2;

	/**
	 * The resources will be resolved from the system's classloader. Examples:
	 * clap://system/rootPkg/subPkg/myClass.class or
	 * clap://system/rootPkg/file.html
	 * 
	 * @see java.lang.ClassLoader#getSystemClassLoader()
	 */
	public static final int CLAP_SYSTEM = 3;

	/**
	 * The resources will be resolved from the current thread's classloader.
	 * Examples: clap://thread/rootPkg/subPkg/myClass.class or
	 * clap://thread/rootPkg/file.html
	 * 
	 * @see java.lang.Thread#getContextClassLoader()
	 */
	public static final int CLAP_THREAD = 4;

	/**
	 * Constructor.
	 * 
	 * @param authorityType
	 *            The authority type for the resource path.
	 * @param path
	 *            The resource path.
	 */
	public static LocalReference createClapReference(int authorityType,
			String path) {
		return new LocalReference("clap://" + getAuthorityName(authorityType)
				+ path);
	}

	/**
	 * Constructor.
	 * 
	 * @param file
	 *            The file whose path must be used.
	 * @throws IOException
	 */
	public static LocalReference createFileReference(File file)
			throws IOException {
		return createFileReference(file.getCanonicalPath());
	}

	/**
	 * Constructor.
	 * 
	 * @param filePath
	 *            The local file path.
	 */
	public static LocalReference createFileReference(String filePath) {
		return createFileReference("", filePath);
	}

	/**
	 * Constructor.
	 * 
	 * @param hostName
	 *            The authority (can be a host name or the special "localhost"
	 *            or an empty value).
	 * @param filePath
	 *            The file path.
	 */
	public static LocalReference createFileReference(String hostName,
			String filePath) {
		return new LocalReference("file://" + hostName + "/"
				+ normalizePath(filePath));
	}

	/**
	 * Constructor.
	 * 
	 * @param jarFile
	 *            The JAR file reference.
	 * @param entryPath
	 *            The entry path inside the JAR file.
	 */
	public static LocalReference createJarReference(Reference jarFile,
			String entryPath) {
		return new LocalReference("jar:" + jarFile.toString() + "!/"
				+ entryPath);
	}

	/**
	 * Returns an authority name.
	 * 
	 * @param authority
	 *            The authority.
	 * @return The name.
	 */
	public static String getAuthorityName(int authority) {
		String result = null;

		switch (authority) {
		case CLAP_CLASS:
			result = "class";
			break;
		case CLAP_SYSTEM:
			result = "system";
			break;
		case CLAP_THREAD:
			result = "thread";
			break;
		}

		return result;
	}

	/**
	 * Localize a path by converting all the separator characters to the
	 * system-dependant separator character.
	 * 
	 * @param path
	 *            The path to localize.
	 * @return The localized path.
	 */
	public static String localizePath(String path) {
		StringBuilder result = new StringBuilder();
		char nextChar;
		for (int i = 0; i < path.length(); i++) {
			nextChar = path.charAt(i);
			if ((nextChar == '/') || (nextChar == '\\')) {
				// Convert the URI separator to the system dependent path
				// separator
				result.append(File.separatorChar);
			} else {
				result.append(nextChar);
			}
		}

		return result.toString();
	}

	/**
	 * Normalize a path by converting all the system-dependant separator
	 * characters to the standard '/' separator character.
	 * 
	 * @param path
	 *            The path to normalize.
	 * @return The normalize path.
	 */
	public static String normalizePath(String path) {
		StringBuilder result = new StringBuilder();
		char nextChar;
		for (int i = 0; i < path.length(); i++) {
			nextChar = path.charAt(i);
			if ((nextChar == '\\')) {
				// Convert the Windows style path separator to the standard path
				// separator
				result.append('/');
			} else {
				result.append(nextChar);
			}
		}

		return result.toString();
	}

	/**
	 * Constructor.
	 * 
	 * @param localRef
	 *            The local reference.
	 */
	public LocalReference(Reference localRef) {
		super(localRef.toString());
	}

	/**
	 * Constructor.
	 * 
	 * @param localUri
	 *            The local URI.
	 */
	public LocalReference(String localUri) {
		super(localUri);
	}

	/**
	 * Returns the type of authority.
	 * 
	 * @return The type of authority.
	 */
	public int getClapAuthorityType() {
		int result = 0;

		if (getSchemeProtocol().equals(Protocol.CLAP)) {
			String authority = getAuthority();

			if (authority != null) {
				if (authority.equalsIgnoreCase(getAuthorityName(CLAP_CLASS))) {
					result = CLAP_CLASS;
				} else if (authority
						.equalsIgnoreCase(getAuthorityName(CLAP_SYSTEM))) {
					result = CLAP_SYSTEM;
				} else if (authority
						.equalsIgnoreCase(getAuthorityName(CLAP_THREAD))) {
					result = CLAP_THREAD;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the local file corresponding to the reference. Only URIs referring
	 * to the "localhost" or to an empty authority are supported.
	 * 
	 * @return The local file corresponding to the reference.
	 */
	public File getFile() {
		File result = null;

		if (getSchemeProtocol().equals(Protocol.FILE)) {
			String hostName = getAuthority();

			if ((hostName == null) || hostName.equals("")
					|| hostName.equalsIgnoreCase("localhost")) {
				String filePath = getPath();
				result = new File(filePath);
			} else {
				throw new RuntimeException(
						"Can't resolve files on remote host machines");
			}
		}

		return result;
	}

	/**
	 * Returns the JAR entry path.
	 * 
	 * @return The JAR entry path.
	 */
	public String getJarEntryPath() {
		String result = null;

		if (getSchemeProtocol().equals(Protocol.JAR)) {
			String ssp = getSchemeSpecificPart();

			if (ssp != null) {
				int separatorIndex = ssp.indexOf("!/");

				if (separatorIndex != -1) {
					result = ssp.substring(separatorIndex + 2);
				}
			}
		}

		return result;
	}

	/**
	 * Returns the JAR file reference.
	 * 
	 * @return The JAR file reference.
	 */
	public Reference getJarFileRef() {
		Reference result = null;

		if (getSchemeProtocol().equals(Protocol.JAR)) {
			String ssp = getSchemeSpecificPart();

			if (ssp != null) {
				int separatorIndex = ssp.indexOf("!/");

				if (separatorIndex != -1) {
					result = new Reference(ssp.substring(0, separatorIndex));
				}
			}
		}

		return result;
	}

}
