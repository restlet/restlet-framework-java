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

import java.io.File;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;

import freemarker.template.Configuration;

/**
 *
 */
public class DynamicApplication extends Application {

    /** Freemarker configuration object. */
    private Configuration fmc;

    /**
     * Constructor.
     */
    public DynamicApplication() {
        try {
            // Instantiate the shared configuration manager for Freemarker.
            final File templateDir = new File(
                    "D:\\alaska\\forge\\build\\swc\\nre\\trunk\\books\\apress\\manuscript\\sample");
            this.fmc = new Configuration();
            this.fmc.setDirectoryForTemplateLoading(templateDir);
        } catch (final Exception e) {
            getLogger().severe("Erreur config FreeMarker");
            e.printStackTrace();
        }
    }

    @Override
    public Restlet createRoot() {
        final Router router = new Router(getContext());
        router.attach("/transformer", TransformerResource.class);
        router.attach("/freemarker", FreemarkerResource.class);
        router.attach("/velocity", VelocityResource.class);

        return router;
    }

    /**
     * Returns the Freemarker configuration object.
     * 
     * @return the Freemarker configuration object.
     */
    public Configuration getFmc() {
        return this.fmc;
    }
}
