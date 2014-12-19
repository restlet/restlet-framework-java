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

package org.restlet.ext.thymeleaf;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;
import org.restlet.util.Resolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.thymeleaf.util.Validate;

/**
 * Thymeleaf template representation. Useful for dynamic string-based
 * representations.
 * 
 * @see <a href="http://www.thymeleaf.org/">Thymeleaf home page</a>
 * @author Grzegorz Godlewski
 */
public class TemplateRepresentation extends WriterRepresentation {

    /**
     * Context that leverages an instance of {@link Resolver}.
     * 
     * @author Grzegorz Godlewski
     */
    private static class ResolverContext implements IContext {
        /** The Locale. */
        private Locale locale;

        /** The Resolver instance. */
        private Resolver<Object> resolver;

        /**
         * Constructor.
         * 
         * @param locale
         *            The Locale.
         * @param resolver
         *            The Resolver instance.
         */
        public ResolverContext(Locale locale, Resolver<Object> resolver) {
            this.locale = locale;
            this.resolver = resolver;
        }

        public final void addContextExecutionInfo(final String templateName) {
            Validate.notEmpty(templateName,
                    "Template name cannot be null or empty");
        }

        public Locale getLocale() {
            return locale;
        }

        @SuppressWarnings("serial")
        public VariablesMap<String, Object> getVariables() {
            return new VariablesMap<String, Object>() {
                @Override
                public boolean containsKey(Object key) {
                    return resolver.resolve(key.toString()) != null;
                }

                @Override
                public Object get(Object key) {
                    return resolver.resolve(key.toString());
                }
            };
        }

    }

    /**
     * Returns a new instance of {@link TemplateEngine} based by default on a
     * {@link TemplateResolver} returned by calling
     * {@link #createTemplateResolver()}.
     * 
     * @return A new instance of {@link TemplateEngine}
     */
    public static TemplateEngine createTemplateEngine() {
        return createTemplateEngine(createTemplateResolver());
    }

    /**
     * Returns a new instance of {@link TemplateEngine} based by default on a
     * {@link TemplateResolver} returned by calling
     * {@link #createTemplateResolver()}.
     * 
     * @return A new instance of {@link TemplateEngine}
     */
    public static TemplateEngine createTemplateEngine(ITemplateResolver resolver) {
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    /**
     * Returns a new instance of {@link ITemplateResolver} with default
     * configuration (XHTML template model, templates located inside
     * "/WEB-INF/templates/", suffixed by ".html".
     * 
     * @return A new instance of {@link ITemplateResolver}.
     */
    public static ITemplateResolver createTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // XHTML is the default mode, but we will set it anyway for better
        // understanding of code
        templateResolver.setTemplateMode("XHTML");
        // This will convert "home" to "/WEB-INF/templates/home.html"
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        // Set template cache TTL to 1 hour. If not set, entries would live in
        // cache until expelled by LRU
        templateResolver.setCacheTTLMs(3600000L);

        return templateResolver;
    }

    /** The template's data model. */
    protected volatile IContext context;

    /** The Thymeleaf engine. */
    private volatile TemplateEngine engine;

    /** The template locale */
    private final Locale locale;

    /** The template name. */
    private volatile String templateName;

    /**
     * Constructor.
     * 
     * @param templateName
     *            The Thymeleaf template's name. The actual template is
     *            retrieved using the Thymeleaf configuration.
     * @param locale
     *            The locale of the template.
     * @param dataModel
     *            The Thymeleaf template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName, Locale locale,
            Map<String, Object> dataModel, MediaType mediaType) {
        this(templateName, createTemplateEngine(), locale, mediaType);
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *            The Thymeleaf template's name. The full path is resolved by
     *            the configuration.
     * @param locale
     *            The locale of the template.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName, Locale locale,
            MediaType mediaType) {
        this(templateName, locale, new ConcurrentHashMap<String, Object>(),
                mediaType);
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *            The Thymeleaf template's name. The actual template is
     *            retrieved using the Thymeleaf configuration.
     * @param engine
     *            The template engine.
     * @param locale
     *            The locale of the template.
     * @param dataModel
     *            The Thymeleaf template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName, TemplateEngine engine,
            Locale locale, Map<String, Object> dataModel, MediaType mediaType) {
        super(mediaType);
        this.locale = locale;
        this.engine = engine;
        this.templateName = templateName;
        setDataModel(dataModel);
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *            The Thymeleaf template's name. The full path is resolved by
     *            the configuration.
     * @param locale
     *            The locale of the template
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName, TemplateEngine engine,
            Locale locale, MediaType mediaType) {
        this(templateName, engine, locale,
                new ConcurrentHashMap<String, Object>(), mediaType);
    }

    /**
     * Constructor based on a Thymeleaf 'encoded' representation.
     * 
     * @param templateRepresentation
     *            The representation to 'decode'.
     * @param locale
     *            The locale of the template.
     * @param mediaType
     *            The representation's media type.
     * @throws IOException
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public TemplateRepresentation(
            TemplateRepresentation templateRepresentation, Locale locale,
            MediaType mediaType) throws IOException {
        this(templateRepresentation, createTemplateEngine(), locale, mediaType);
    }

    /**
     * Constructor based on a Thymeleaf 'encoded' representation.
     * 
     * @param templateRepresentation
     *            The representation to 'decode'.
     * @param engine
     *            The template engine.
     * @param locale
     *            The locale of the template.
     * @param mediaType
     *            The representation's media type.
     * @throws IOException
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public TemplateRepresentation(
            TemplateRepresentation templateRepresentation,
            TemplateEngine engine, Locale locale, MediaType mediaType)
            throws IOException {
        super(mediaType);
        this.locale = locale;
        this.engine = engine;
        this.templateName = templateRepresentation.getTemplateName();
    }

    /**
     * Returns the representation's locale.
     * 
     * @return The representation's locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the template's name.
     * 
     * @return The template's name.
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Sets the Thymeleaf context.
     * 
     * @param context
     *            The Thymeleaf context
     */
    protected void setContext(IContext context) {
        this.context = context;
    }

    /**
     * Sets the template's data model.
     * 
     * @param dataModel
     *            The template's data model.
     */
    public void setDataModel(Map<String, Object> dataModel) {
        Context ctx = new Context(locale);
        ctx.setVariables(dataModel);
        setContext(ctx);
    }

    /**
     * Sets the template's data model from a request/response pair. This default
     * implementation uses a Resolver.
     * 
     * @see Resolver
     * @see Resolver#createResolver(Request, Response)
     * 
     * @param request
     *            The request where data are located.
     * @param response
     *            The response where data are located.
     */
    public void setDataModel(Request request, Response response) {
        Form form = new Form(request.getEntity());
        Context ctx = new Context(locale);
        ctx.setVariables(form.getValuesMap());
        setContext(ctx);
    }

    /**
     * Sets the template's data model from a resolver.
     * 
     * @param resolver
     *            The resolver.
     */
    public void setDataModel(Resolver<Object> resolver) {
        setContext(new ResolverContext(locale, resolver));
    }

    /**
     * Sets the template's name.
     * 
     * @param templateName
     *            The template's name.
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Writes the datum as a stream of characters.
     * 
     * @param writer
     *            The writer to use when writing.
     */
    @Override
    public void write(Writer writer) throws IOException {
        try {
            // Load the template
            // Process the template
            engine.process(templateName, context, writer);

        } catch (Exception e) {
            final org.restlet.Context context = org.restlet.Context
                    .getCurrent();

            if (context != null) {
                context.getLogger().log(Level.WARNING,
                        "Unable to process the template", e);
            }

            e.printStackTrace();

            throw new IOException("Template processing error. "
                    + e.getMessage());
        }
    }
}
