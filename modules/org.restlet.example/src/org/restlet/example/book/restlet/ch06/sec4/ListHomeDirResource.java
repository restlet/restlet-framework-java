/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.example.book.restlet.ch06.sec4;

import java.io.File;
import java.io.FilenameFilter;
import java.security.PrivilegedAction;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.ext.jaas.JaasUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * @author Bruno Harbulot (bruno/distributedmatter.net)
 * 
 */
public class ListHomeDirResource extends ServerResource {
    @Get("txt")
    public Representation echoPrincipals() throws ResourceException {
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

        StringBuilder sb = JaasUtils.doAsPriviledged(getRequest()
                .getClientInfo(), action);

        Representation rep = new StringRepresentation(sb, MediaType.TEXT_PLAIN,
                Language.ALL, CharacterSet.UTF_8);
        return rep;
    }
}
