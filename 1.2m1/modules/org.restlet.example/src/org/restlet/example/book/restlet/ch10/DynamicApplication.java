/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
        } catch (Exception e) {
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
