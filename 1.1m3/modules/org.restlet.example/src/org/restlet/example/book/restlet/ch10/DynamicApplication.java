package org.restlet.example.book.restlet.ch10;

import java.io.File;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

import freemarker.template.Configuration;

public class DynamicApplication extends Application {

    /** Freemarker configuration object. */
    private Configuration fmc;

    public DynamicApplication(Context parentContext) {
        super(parentContext);
        try {
            // Instantiate the shared configuration manager for Freemarker.
            File templateDir = new File(
                    "D:\\alaska\\forge\\build\\swc\\nre\\trunk\\books\\apress\\manuscript\\sample");
            fmc = new Configuration();
            fmc.setDirectoryForTemplateLoading(templateDir);
        } catch (Exception e) {
            getLogger().severe("Erreur config FreeMarker");
            e.printStackTrace();
        }
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());
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
        return fmc;
    }
}
