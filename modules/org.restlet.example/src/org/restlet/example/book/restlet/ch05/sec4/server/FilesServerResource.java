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

package org.restlet.example.book.restlet.ch05.sec4.server;

import java.io.File;
import java.io.FilenameFilter;
import java.security.AccessControlException;
import java.security.PrivilegedAction;

import org.restlet.data.Status;
import org.restlet.ext.jaas.JaasUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Using JVM security manager.
 * 
 * @author Bruno Harbulot (bruno/distributedmatter.net)
 */
public class FilesServerResource extends ServerResource {

    @Get("txt")
    public Representation retrieve() throws ResourceException {
        StringBuilder result = null;

        // The action requiring the CFO role to run
        PrivilegedAction<StringBuilder> action = new PrivilegedAction<StringBuilder>() {
            public StringBuilder run() {
                File dir = new File(System.getProperty("user.home"));
                String[] filenames = dir.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return !name.startsWith(".");
                    }
                });

                StringBuilder sb = new StringBuilder(
                        "Files in the home directory: \n\n");
                for (String filename : filenames) {
                    sb.append(filename);
                    sb.append("\n");
                }
                return sb;
            }
        };

        // Invoking the privileged action only if CFO role granted to
        // authenticated user
        try {
            result = JaasUtils.doAsPriviledged(getRequest().getClientInfo(),
                    action);
        } catch (AccessControlException ace) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        }

        // Returning home dir files listing
        return (result == null) ? null : new StringRepresentation(result);
    }
}
