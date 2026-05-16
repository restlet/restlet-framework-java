/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.engine.connector.ClientHelper;

/**
 * Connector to the local resources accessible via file system, class loaders, and similar
 * mechanisms. Here is the list of parameters that are supported. They should be set in the Client's
 * context before it is started:
 *
 * <table>
 * <caption>list of supported parameters</caption>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>timeToLive</td>
 * <td>int</td>
 * <td>600</td>
 * <td>Time to live for a representation before it expires (in seconds). If you
 * set the value to '0', the representation will never expire.</td>
 * </tr>
 * <tr>
 * <td>defaultLanguage</td>
 * <td>String</td>
 * <td></td>
 * <td>When no metadata service is available (simple client connector with no
 * parent application), falls back on this default language. To indicate that no
 * default language should be set, "" can be used.</td>
 * </tr>
 * </table>
 *
 * @see org.restlet.data.LocalReference
 * @author Jerome Louvel
 * @author Thierry Boileau
 */
public abstract class LocalClientHelper extends ClientHelper {

    private static final String PRIVATE_TEMP_DIRECTORY_NAME = ".restlet/tmp";

    private static final FileAttribute<Set<PosixFilePermission>> OWNER_ALL_DIRECTORY_PERMISSIONS =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));

    private static final FileAttribute<Set<PosixFilePermission>> OWNER_READ_WRITE_FILE_PERMISSIONS =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));

    /**
     * Constructor. Note that the common list of metadata associations based on extensions is added,
     * see the addCommonExtensions() method.
     *
     * @param client The client to help.
     */
    protected LocalClientHelper(Client client) {
        super(client);
    }

    /**
     * Returns the default language. When no metadata service is available (simple client connector
     * with no parent application), it falls back on this default language.
     *
     * @return The default language.
     */
    public String getDefaultLanguage() {
        return getHelpedParameters().getFirstValue("defaultLanguage", "");
    }

    /**
     * Returns the time to live for a file representation before it expires (in seconds).
     *
     * @return The time to live for a file representation before it expires (in seconds).
     */
    public int getTimeToLive() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("timeToLive", "600"));
    }

    /**
     * Handles a call. Note that this implementation will systematically normalize and URI-decode
     * the resource reference.
     *
     * @param request The request to handle.
     * @param response The response to modify.
     */
    @Override
    public final void handle(Request request, Response response) {
        // Ensure that all ".." and "." are normalized into the path
        // to prevent unauthorized access to user directories.
        request.getResourceRef().normalize();

        // As the path may be percent-encoded, it has to be percent-decoded.
        // Then, all generated URIs must be encoded.
        String path = request.getResourceRef().getPath();
        String decodedPath = Reference.decode(path);

        if (decodedPath != null) {
            // Continue the local handling
            handleLocal(request, response, decodedPath);
        } else {
            getLogger()
                    .warning(
                            "Unable to get the path of this local URI: "
                                    + request.getResourceRef());
        }
    }

    protected static File createPrivateTempFile(String prefix, String suffix) throws IOException {
        Path temporaryDirectory = getPrivateTempDirectory();
        FileStore fileStore = Files.getFileStore(temporaryDirectory);
        Path temporaryFile;

        if (fileStore.supportsFileAttributeView("posix")) {
            temporaryFile =
                    Files.createTempFile(
                            temporaryDirectory, prefix, suffix, OWNER_READ_WRITE_FILE_PERMISSIONS);
        } else {
            temporaryFile = Files.createTempFile(temporaryDirectory, prefix, suffix);
            File temporaryFileAsFile = temporaryFile.toFile();
            setOwnerOnlyAccess(temporaryFileAsFile, false);
        }

        return temporaryFile.toFile();
    }

    private static Path getPrivateTempDirectory() throws IOException {
        Path homeDirectory = getPrivateTempHomeRoot();
        Path privateTempDirectory = homeDirectory.resolve(PRIVATE_TEMP_DIRECTORY_NAME);

        if (Files.exists(privateTempDirectory)) {
            if (Files.getFileStore(privateTempDirectory).supportsFileAttributeView("posix")) {
                Files.setPosixFilePermissions(
                        privateTempDirectory, OWNER_ALL_DIRECTORY_PERMISSIONS.value());
            }
            return privateTempDirectory;
        }

        if (Files.getFileStore(homeDirectory).supportsFileAttributeView("posix")) {
            return Files.createDirectories(privateTempDirectory, OWNER_ALL_DIRECTORY_PERMISSIONS);
        }

        return Files.createDirectories(privateTempDirectory);
    }

    private static Path getPrivateTempHomeRoot() {
        String userHome = System.getProperty("user.home");

        if (userHome != null && !userHome.isEmpty()) {
            return Paths.get(userHome);
        }

        return Paths.get(".").toAbsolutePath().normalize();
    }

    private static void setOwnerOnlyAccess(File file, boolean executable) throws IOException {
        ensurePermissionChange(file.setReadable(true, true), file, "enable owner read access");
        ensurePermissionChange(file.setWritable(true, true), file, "enable owner write access");

        if (executable) {
            ensurePermissionChange(
                    file.setExecutable(true, true), file, "enable owner execute access");
        }
    }

    private static void ensurePermissionChange(boolean updated, File file, String action)
            throws IOException {
        if (!updated) {
            throw new IOException("Unable to " + action + " for " + file.getAbsolutePath());
        }
    }

    /**
     * Handles a local call.
     *
     * @param request The request to handle.
     * @param response The response to modify.
     * @param decodedPath The decoded local path.
     */
    protected abstract void handleLocal(Request request, Response response, String decodedPath);
}
