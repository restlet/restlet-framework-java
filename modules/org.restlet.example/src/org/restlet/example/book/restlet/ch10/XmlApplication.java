/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.example.book.restlet.ch10;

import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.LocalReference;

/**
 *
 */
public class XmlApplication extends Application {

    @Override
    public Restlet createRoot() {
        final Router router = new Router(getContext());
        router.attach("/dom", DomResource.class);
        final Directory dir = new Directory(
                getContext(),
                LocalReference
                        .createFileReference("D:\\workspace\\restlet-1.1\\BouquinApress\\src\\chapter7"));
        router.attach("/xml", dir);
        return router;
    }
}
