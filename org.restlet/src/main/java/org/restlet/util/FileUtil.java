package org.restlet.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Set;

public class FileUtil {

    public static Path createTemporaryFileSafely(final String prefix, final String suffix) throws IOException {
        boolean isPosix = FileSystems.getDefault()
                .supportedFileAttributeViews()
                .contains("posix");

        if (isPosix) {
            // Atomic creation with permissions on Linux/Unix/macOS
            FileAttribute<Set<PosixFilePermission>> attr =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rw-------"));
            return Files.createTempFile(prefix, suffix, attr);
        } else {
            // Windows: create first, then lock down ACL
            final Path tempFile = Files.createTempFile(prefix, suffix);
            try {
                AclFileAttributeView aclView = Files.getFileAttributeView(
                        tempFile, AclFileAttributeView.class);
                UserPrincipal owner = aclView.getOwner();

                AclEntry entry = AclEntry.newBuilder()
                        .setType(AclEntryType.ALLOW)
                        .setPrincipal(owner)
                        .setPermissions(
                                AclEntryPermission.READ_DATA,
                                AclEntryPermission.WRITE_DATA,
                                AclEntryPermission.APPEND_DATA,
                                AclEntryPermission.READ_ATTRIBUTES,
                                AclEntryPermission.WRITE_ATTRIBUTES,
                                AclEntryPermission.READ_ACL,
                                AclEntryPermission.WRITE_ACL, // allows future ACL changes
                                AclEntryPermission.SYNCHRONIZE // required on Windows
                        )
                        .build();

                aclView.setAcl(List.of(entry)); // replace entire ACL
            } catch (IOException e) {
                Files.deleteIfExists(tempFile);
                throw e;
            }
            return tempFile;
        }
    }
}
