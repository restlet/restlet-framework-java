/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.restlet.engine.converter.ConverterUtils.getVariants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.application.StatusInfo;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

/**
 * Service to handle error statuses. If an exception is thrown within your application or Restlet
 * code, it will be intercepted by this service if it is enabled.<br>
 * <br>
 * When an exception or an error is caught, the {@link #toStatus(Throwable, Request, Response)}
 * method is first invoked to obtain the status that you want to set on the response. If this method
 * isn't overridden or returns null, the {@link Status#SERVER_ERROR_INTERNAL} constant will be set
 * by default.<br>
 * <br>
 * Also, when the status of a response returned is an error status (see {@link Status#isError()},
 * the {@link #toRepresentation(Status, Request, Response)} method is then invoked to give your
 * service a chance to override the default error page.<br>
 * <br>
 * If you want to customize the default behavior, you need to create a subclass of StatusService
 * that overrides some or all of the methods mentioned above. Then, just create a instance of your
 * class and set it on your Component or Application via the setStatusService() methods.<br>
 * <br>
 * In case the response's entity has already been set, the status service does not generate an error
 * representation. You can turn off this default behavior by calling the {@link
 * #setOverwriting(boolean)} method.
 *
 * @author Jerome Louvel
 */
public class StatusService extends Service {

    /** The service used to select the preferred variant. */
    private volatile ConnegService connegService;

    /** The email address to contact in case of error. */
    private volatile String contactEmail;

    /** The service used to convert between status/throwable and representation. */
    private volatile ConverterService converterService;

    /** The home URI to propose in case of error. */
    private volatile Reference homeRef;

    /** The service used to select the preferred variant. */
    private volatile MetadataService metadataService;

    /** True if an existing entity should be overwritten. */
    private volatile boolean overwriting;

    /** Constructor. By default, it creates the necessary services. */
    public StatusService() {
        this(true);
    }

    /**
     * Constructor. By default, it creates the necessary services.
     *
     * @param enabled True if the service has been enabled.
     */
    public StatusService(boolean enabled) {
        this(enabled, new ConverterService(), new MetadataService(), new ConnegService());
    }

    /**
     * Constructor.
     *
     * @param enabled True if the service has been enabled.
     * @param converterService The service used to convert between status/throwable and
     *     representation.
     * @param metadataService The service used to select the preferred variant.
     * @param connegService The service used to select the preferred variant.
     */
    public StatusService(
            boolean enabled,
            ConverterService converterService,
            MetadataService metadataService,
            ConnegService connegService) {
        super(enabled);
        this.converterService = converterService;
        this.metadataService = metadataService;
        this.connegService = connegService;
        this.contactEmail = null;
        this.homeRef = new Reference("/");
        this.overwriting = false;
    }

    @Override
    public org.restlet.routing.Filter createInboundFilter(Context context) {
        return new org.restlet.engine.application.StatusFilter(context, this);
    }

    /**
     * Returns the service used to select the preferred variant.
     *
     * @return The service used to select the preferred variant.
     */
    public ConnegService getConnegService() {
        return connegService;
    }

    /**
     * Returns the email address to contact in case of error. This is typically used when creating
     * the status representations.
     *
     * @return The email address to contact in case of error.
     */
    public String getContactEmail() {
        return this.contactEmail;
    }

    /**
     * Returns the service used to convert between status/throwable and representation.
     *
     * @return The service used to convert between status/throwable and representation.
     */
    public ConverterService getConverterService() {
        return converterService;
    }

    /**
     * Returns the home URI to propose in case of error.
     *
     * @return The home URI to propose in case of error.
     */
    public Reference getHomeRef() {
        return this.homeRef;
    }

    /**
     * Returns the service used to select the preferred variant.
     *
     * @return The service used to select the preferred variant.
     */
    public MetadataService getMetadataService() {
        return metadataService;
    }

    /**
     * Indicates if an existing entity should be overwritten. False by default.
     *
     * @return True if an existing entity should be overwritten.
     */
    public boolean isOverwriting() {
        return this.overwriting;
    }

    /**
     * Sets the service used to select the preferred variant.
     *
     * @param connegService The service used to select the preferred variant.
     */
    public void setConnegService(ConnegService connegService) {
        this.connegService = connegService;
    }

    /**
     * Sets the email address to contact in case of error. This is typically used when creating the
     * status representations.
     *
     * @param contactEmail The email address to contact in case of error.
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * Sets the service used to convert between status/throwable and representation.
     *
     * @param converterService The service used to convert between status/throwable and
     *     representation.
     */
    public void setConverterService(ConverterService converterService) {
        this.converterService = converterService;
    }

    /**
     * Sets the home URI to propose in case of error.
     *
     * @param homeRef The home URI to propose in case of error.
     */
    public void setHomeRef(Reference homeRef) {
        this.homeRef = homeRef;
    }

    /**
     * Sets the service used to select the preferred variant.
     *
     * @param metadataService The service used to select the preferred variant.
     */
    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Indicates if an existing entity should be overwritten.
     *
     * @param overwriting True if an existing entity should be overwritten.
     */
    public void setOverwriting(boolean overwriting) {
        this.overwriting = overwriting;
    }

    /**
     * Returns a representation for the given status. To customize the default representation, this
     * method can be overridden. It returns a {@link org.restlet.data.Status} representation by
     * default or a {@link java.lang.Throwable} representation if the throwable is annotated with
     * {@link org.restlet.resource.Status}.
     *
     * @param status The status to represent.
     * @param request The request handled.
     * @param response The response updated.
     * @return The representation of the given status.
     */
    public Representation toRepresentation(Status status, Request request, Response response) {
        if (converterService == null || connegService == null || metadataService == null) {
            return null;
        }

        Throwable cause = status.getThrowable();

        if (canSerializeStatusException(cause)) {
            return toRepresentation(request, cause);
        }

        // Default representation match with the status properties
        final StatusInfo defaultStatusRepresentation =
                new StatusInfo(status, getContactEmail(), getHomeRef().toString());
        return toRepresentation(request, defaultStatusRepresentation);
    }

    /**
     * Serialize exception if any and if {@link org.restlet.resource.Status} annotation asks for it
     */
    private boolean canSerializeStatusException(final Throwable cause) {
        // Do content negotiation for status
        if (cause != null) {
            org.restlet.engine.resource.ThrowableAnnotationInfo tai =
                    org.restlet.engine.resource.AnnotationUtils.getInstance()
                            .getThrowableAnnotationInfo(cause.getClass());

            if (tai != null && tai.isSerializable()) {
                if (Application.getCurrent() != null && !Application.getCurrent().isDebugging()) {
                    // We clear the stack trace to prevent technical information leak
                    cause.setStackTrace(new StackTraceElement[] {});

                    if (cause.getCause() != null) {
                        Context.getCurrentLogger()
                                .log(
                                        Level.WARNING,
                                        "The cause of the exception should be null except in debug mode");
                    }
                }

                return true;
            }
        }
        return false;
    }

    /** Serializes the given object into a representation. */
    private Representation toRepresentation(
            final Request request, final Object representationObject) {
        try {
            List<VariantInfo> variants = getVariants(representationObject.getClass(), null);
            if (variants == null) {
                variants = new ArrayList<>();
            }

            Variant variant = connegService.getPreferredVariant(variants, request, metadataService);
            return converterService.toRepresentation(representationObject, variant);
        } catch (Exception e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            e,
                            () -> "Could not serialize class " + representationObject.getClass());
        }
        return null;
    }

    /**
     * Returns a representation for the given status.<br>
     * To customize the default representation, this method can be overridden. By default, it
     * invokes {@link #toRepresentation(Status, Request, Response)}
     *
     * @param status The status to represent.
     * @param resource The parent resource.
     * @return The representation of the given status.
     */
    public Representation toRepresentation(Status status, Resource resource) {
        return toRepresentation(status, resource.getRequest(), resource.getResponse());
    }

    /**
     * Returns a status for a given exception or error. By default, it unwraps the status of {@link
     * ResourceException}. For other exceptions or errors, it returns an {@link
     * Status#SERVER_ERROR_INTERNAL} status.<br>
     * <br>
     * To customize the default behavior, this method can be overridden.
     *
     * @param throwable The exception or error caught.
     * @param request The request handled.
     * @param response The response updated.
     * @return The representation of the given status.
     */
    public Status toStatus(Throwable throwable, Request request, Response response) {
        Status result;

        Status defaultStatus = Status.SERVER_ERROR_INTERNAL;
        Throwable t = throwable;

        // If throwable is a ResourceException, use its status and the cause.
        if (throwable instanceof ResourceException resourceException) {
            defaultStatus = resourceException.getStatus();

            if (throwable.getCause() != null && throwable.getCause() != throwable) {
                t = throwable.getCause();
            }
        }

        // look for Status annotation
        org.restlet.engine.resource.ThrowableAnnotationInfo tai =
                org.restlet.engine.resource.AnnotationUtils.getInstance()
                        .getThrowableAnnotationInfo(t.getClass());

        if (tai != null) {
            result = new Status(tai.getStatus(), t);
        } else {
            result = new Status(defaultStatus, t);
        }

        return result;
    }

    /**
     * Returns a status for a given exception or error. By default, it returns an {@link
     * Status#SERVER_ERROR_INTERNAL} status and logs a severe message.<br>
     * To customize the default behavior, this method can be overridden.
     *
     * @param throwable The exception or error caught.
     * @param resource The parent resource.
     * @return The representation of the given status.
     */
    public Status toStatus(Throwable throwable, Resource resource) {
        return toStatus(
                throwable,
                (resource == null) ? null : resource.getRequest(),
                (resource == null) ? null : resource.getResponse());
    }
}
