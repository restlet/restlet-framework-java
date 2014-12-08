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

import java.io.File;

/**
 * Reference to a local resource. It has helper methods for the three following
 * schemes: {@link Protocol#CLAP}, {@link Protocol#FILE}, {@link Protocol#JAR}
 * and {@link Protocol#RIAP}.
 * 
 * @author Jerome Louvel
 */
public final class LocalReference extends Reference {
    /**
     * The resources will be resolved from the classloader associated with the
     * local class. This is the same as the {@link #CLAP_CLASS} authority.
     * Examples: clap:///rootPkg/subPkg/myClass.class or
     * clap:///rootPkg/file.html
     * 
     * @see java.lang.Class#getClassLoader()
     */
    public static final int CLAP_DEFAULT = 0;

    /**
     * The resources will be resolved from the classloader associated with the
     * local class. This is the default CLAP authority. Examples:
     * clap://class/rootPkg/subPkg/myClass.class or
     * clap://class/rootPkg/file.html or clap:///rootPkg/file.html
     * 
     * @see java.lang.Class#getClassLoader()
     */
    public static final int CLAP_CLASS = 1;

    /**
     * The resources will be resolved from the system's classloader. Examples:
     * clap://system/rootPkg/subPkg/myClass.class or
     * clap://system/rootPkg/file.html
     * 
     * @see java.lang.ClassLoader#getSystemClassLoader()
     */
    public static final int CLAP_SYSTEM = 2;

    /**
     * The resources will be resolved from the current thread's classloader.
     * Examples: clap://thread/rootPkg/subPkg/myClass.class or
     * clap://thread/rootPkg/file.html
     * 
     * @see java.lang.Thread#getContextClassLoader()
     */
    public static final int CLAP_THREAD = 3;

    /**
     * The resources will be resolved from the current application's root
     * Restlet. Example riap://application/myPath/myResource
     */
    public static final int RIAP_APPLICATION = 4;

    /**
     * The resources will be resolved from the current component's internal
     * (private) router. Example riap://component/myAppPath/myResource
     */
    public static final int RIAP_COMPONENT = 5;

    /**
     * The resources will be resolved from the current component's virtual host.
     * Example riap://host/myAppPath/myResource
     */
    public static final int RIAP_HOST = 6;

    /**
     * Constructor.
     * 
     * @param pkg
     *            The package to identify.
     */
    public static LocalReference createClapReference(Package pkg) {
        return createClapReference(CLAP_DEFAULT, pkg);
    }

    /**
     * Constructor for CLAP URIs to a given package.
     * 
     * @param authorityType
     *            The authority type for the resource path.
     * @param pkg
     *            The package to identify.
     */
    public static LocalReference createClapReference(int authorityType,
            Package pkg) {
        String pkgPath = pkg.getName().replaceAll("\\.", "/");
        return new LocalReference("clap://" + getAuthorityName(authorityType)
                + "/" + pkgPath);
    }

    /**
     * Constructor.
     * 
     * @param path
     *            The resource path.
     */
    public static LocalReference createClapReference(String path) {
        return createClapReference(CLAP_DEFAULT, path);
    }

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
     * @return The new local reference.
     * @see #createFileReference(String)
     */
    public static LocalReference createFileReference(File file) {
        return createFileReference(file.getAbsolutePath());
    }

    /**
     * Constructor.
     * 
     * @param filePath
     *            The local file path.
     * @see #createFileReference(String, String)
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
        return new LocalReference("jar:" + jarFile.getTargetRef().toString()
                + "!/" + entryPath);
    }

    /**
     * Constructor.
     * 
     * @param authorityType
     *            The authority type for the resource path.
     * @param path
     *            The resource path.
     */
    public static LocalReference createRiapReference(int authorityType,
            String path) {
        return new LocalReference("riap://" + getAuthorityName(authorityType)
                + path);
    }

    /**
     * Constructor.
     * 
     * @param zipFile
     *            The Zip file reference.
     * @param entryPath
     *            The entry path inside the Zip file.
     */
    public static LocalReference createZipReference(Reference zipFile,
            String entryPath) {
        return new LocalReference("zip:" + zipFile.getTargetRef().toString()
                + "!/" + entryPath);
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
        case CLAP_DEFAULT:
            result = "";
            break;
        case CLAP_CLASS:
            result = "class";
            break;
        case CLAP_SYSTEM:
            result = "system";
            break;
        case CLAP_THREAD:
            result = "thread";
            break;
        case RIAP_APPLICATION:
            result = "application";
            break;
        case RIAP_COMPONENT:
            result = "component";
            break;
        case RIAP_HOST:
            result = "host";
            break;
        }

        return result;
    }

    /**
     * Localize a path by converting all the separator characters to the
     * system-dependent separator character.
     * 
     * @param path
     *            The path to localize.
     * @return The localized path.
     */
    public static String localizePath(String path) {
        final StringBuilder result = new StringBuilder();
        char nextChar;
        for (int i = 0; i < path.length(); i++) {
            nextChar = path.charAt(i);
            if (nextChar == '/') {
                // Convert the URI separator to
                // the system dependent path separator
                result.append(File.separatorChar);
            } else {
                result.append(nextChar);
            }
        }

        return result.toString();
    }

    /**
     * Normalize a path by converting all the system-dependent separator
     * characters to the standard '/' separator character.
     * 
     * @param path
     *            The path to normalize.
     * @return The normalize path.
     */
    public static String normalizePath(String path) {
        final StringBuilder result = new StringBuilder();
        char nextChar;
        for (int i = 0; i < path.length(); i++) {
            nextChar = path.charAt(i);
            if ((nextChar == File.separatorChar)) {
                // Convert the Windows style path separator
                // to the standard path separator
                result.append('/');
            } else if (!isUnreserved(nextChar)) {
                result.append(Reference.encode("" + nextChar));
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
        super(localRef.getTargetRef().toString());
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

        if (Protocol.CLAP.equals(getSchemeProtocol())) {
            final String authority = getAuthority();

            if (authority != null) {
                if (authority.equalsIgnoreCase(getAuthorityName(CLAP_CLASS))) {
                    result = CLAP_CLASS;
                } else if (authority
                        .equalsIgnoreCase(getAuthorityName(CLAP_SYSTEM))) {
                    result = CLAP_SYSTEM;
                } else if (authority
                        .equalsIgnoreCase(getAuthorityName(CLAP_THREAD))) {
                    result = CLAP_THREAD;
                } else {
                    result = CLAP_DEFAULT;
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

        if (Protocol.FILE.equals(getSchemeProtocol())) {
            final String hostName = getAuthority();

            if ((hostName == null) || hostName.equals("")
                    || hostName.equalsIgnoreCase("localhost")) {
                final String filePath = Reference.decode(getPath());
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

        if (Protocol.JAR.equals(getSchemeProtocol())) {
            final String ssp = getSchemeSpecificPart();

            if (ssp != null) {
                final int separatorIndex = ssp.indexOf("!/");

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

        if (Protocol.JAR.equals(getSchemeProtocol())) {
            final String ssp = getSchemeSpecificPart();

            if (ssp != null) {
                final int separatorIndex = ssp.indexOf("!/");

                if (separatorIndex != -1) {
                    result = new Reference(ssp.substring(0, separatorIndex));
                }
            }
        }

        return result;
    }

    /**
     * Returns the type of authority.
     * 
     * @return The type of authority.
     */
    public int getRiapAuthorityType() {
        int result = 0;

        if (Protocol.RIAP.equals(getSchemeProtocol())) {
            final String authority = getAuthority();

            if (authority != null) {
                if (authority
                        .equalsIgnoreCase(getAuthorityName(RIAP_APPLICATION))) {
                    result = RIAP_APPLICATION;
                } else if (authority
                        .equalsIgnoreCase(getAuthorityName(RIAP_COMPONENT))) {
                    result = RIAP_COMPONENT;
                } else if (authority
                        .equalsIgnoreCase(getAuthorityName(RIAP_HOST))) {
                    result = RIAP_HOST;
                }
            }
        }

        return result;
    }

}
