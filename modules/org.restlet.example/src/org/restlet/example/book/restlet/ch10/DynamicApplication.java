package org.restlet.example.book.restlet.ch10;

import java.io.File;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;

import freemarker.template.Configuration;

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
