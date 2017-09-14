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

package org.restlet.service;

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
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

/**
 * Service to handle error statuses. If an exception is thrown within your
 * application or Restlet code, it will be intercepted by this service if it is
 * enabled.<br>
 * <br>
 * When an exception or an error is caught, the
 * {@link #getStatus(Throwable, Request, Response)} method is first invoked to
 * obtain the status that you want to set on the response. If this method isn't
 * overridden or returns null, the {@link Status#SERVER_ERROR_INTERNAL} constant
 * will be set by default.<br>
 * <br>
 * Also, when the status of a response returned is an error status (see
 * {@link Status#isError()}, the
 * {@link #getRepresentation(Status, Request, Response)} method is then invoked
 * to give your service a chance to override the default error page.<br>
 * <br>
 * If you want to customize the default behavior, you need to create a subclass
 * of StatusService that overrides some or all of the methods mentioned above.
 * Then, just create a instance of your class and set it on your Component or
 * Application via the setStatusService() methods.<br>
 * <br>
 * In case the response's entity has already been set, the status service does
 * not generate an error representation. You can turn off this default behavior
 * by calling the {@link #setOverwriting(boolean)} method.
 * 
 * @see <a href="http://wiki.restlet.org/docs_2.2/202-restlet.html">User
 *      Guide</a>
 * @author Jerome Louvel
 */
public class StatusService extends Service {

    // [ifndef gwt] member
    /** The service used to select the preferred variant. */
    private volatile ConnegService connegService;

    /** The email address to contact in case of error. */
    private volatile String contactEmail;

    // [ifndef gwt] member
    /** The service used to convert between status/throwable and representation. */
    private volatile ConverterService converterService;

    /** The home URI to propose in case of error. */
    private volatile Reference homeRef;

    // [ifndef gwt] member
    /** The service used to select the preferred variant. */
    private volatile MetadataService metadataService;

    /** True if an existing entity should be overwritten. */
    private volatile boolean overwriting;

    /**
     * Constructor. By default, it creates the necessary services.
     */
    public StatusService() {
        this(true);
    }

    /**
     * Constructor. By default, it creates the necessary services.
     * 
     * @param enabled
     *            True if the service has been enabled.
     * 
     */
    public StatusService(boolean enabled) {
        // [ifndef gwt] instruction
        this(enabled, new ConverterService(), new MetadataService(),
                new ConnegService());
        // [ifdef gwt] instruction uncomment
        // super(enabled);
        // this.homeRef = new Reference("/");
    }

    // [ifndef gwt] method
    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     * @param converterService
     *            The service used to convert between status/throwable and
     *            representation.
     * @param metadataService
     *            The service used to select the preferred variant.
     * @param connegService
     *            The service used to select the preferred variant.
     */
    public StatusService(boolean enabled, ConverterService converterService,
            MetadataService metadataService, ConnegService connegService) {
        super(enabled);
        this.converterService = converterService;
        this.metadataService = metadataService;
        this.connegService = connegService;
        this.contactEmail = null;
        this.homeRef = new Reference("/");
        this.overwriting = false;
    }

    // [ifndef gwt] method
    @Override
    public org.restlet.routing.Filter createInboundFilter(Context context) {
        return new org.restlet.engine.application.StatusFilter(context, this);
    }

    // [ifndef gwt] method
    /**
     * Returns the service used to select the preferred variant.
     * 
     * @return The service used to select the preferred variant.
     */
    public ConnegService getConnegService() {
        return connegService;
    }

    /**
     * Returns the email address to contact in case of error. This is typically
     * used when creating the status representations.
     * 
     * @return The email address to contact in case of error.
     */
    public String getContactEmail() {
        return this.contactEmail;
    }

    // [ifndef gwt] method
    /**
     * Returns the service used to convert between status/throwable and
     * representation.
     * 
     * @return The service used to convert between status/throwable and
     *         representation.
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

    // [ifndef gwt] method
    /**
     * Returns the service used to select the preferred variant.
     * 
     * @return The service used to select the preferred variant.
     */
    public MetadataService getMetadataService() {
        return metadataService;
    }

    /**
     * Returns a representation for the given status. In order to customize the
     * default representation, this method can be overridden. It returns null by
     * default.
     * 
     * @param status
     *            The status to represent.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     * @deprecated Use {@link #toRepresentation(Status, Request, Response)}
     *             instead.
     */
    @Deprecated
    public Representation getRepresentation(Status status, Request request,
            Response response) {
        Representation result = null;

        // [ifndef gwt]
        // Do content negotiation for status
        if (converterService != null && connegService != null
                && metadataService != null) {
            Object representationObject = null;

            // Serialize exception if any and if {@link
            // org.restlet.resource.Status} annotation asks for it
            Throwable cause = status.getThrowable();

            if (cause != null) {
                org.restlet.engine.resource.ThrowableAnnotationInfo tai = org.restlet.engine.resource.AnnotationUtils
                        .getInstance().getThrowableAnnotationInfo(
                                cause.getClass());

                if (tai != null && tai.isSerializable()) {
                    if (Application.getCurrent() != null
                            && !Application.getCurrent().isDebugging()) {
                        // We clear the stack trace to prevent technical
                        // information leak
                        cause.setStackTrace(new StackTraceElement[] {});

                        if (cause.getCause() != null) {
                            Context.getCurrentLogger()
                                    .log(Level.WARNING,
                                            "The cause of the exception should be null except in debug mode");
                        }
                    }

                    representationObject = cause;
                }
            }

            try {
                // Default representation match with the status properties
                if (representationObject == null) {
                    representationObject = new StatusInfo(status,
                            getContactEmail(), getHomeRef().toString());
                }

                List<org.restlet.engine.resource.VariantInfo> variants = org.restlet.engine.converter.ConverterUtils
                        .getVariants(representationObject.getClass(), null);
                if (variants == null) {
                    variants = new ArrayList<>();
                }

                Variant variant = connegService.getPreferredVariant(variants,
                        request, metadataService);
                result = converterService.toRepresentation(
                        representationObject, variant);
            } catch (Exception e) {
                Context.getCurrentLogger().log(
                        Level.WARNING,
                        "Could not serialize throwable class "
                                + ((cause == null) ? null : cause.getClass()),
                        e);
            }
        }
        // [enddef]
        return result;
    }

    /**
     * Returns a status for a given exception or error. By default it unwraps
     * the status of {@link ResourceException}. For other exceptions or errors,
     * it returns an {@link Status#SERVER_ERROR_INTERNAL} status.<br>
     * <br>
     * In order to customize the default behavior, this method can be
     * overridden.
     * 
     * @param throwable
     *            The exception or error caught.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     * @deprecated Use {@link #toStatus(Throwable, Request, Response)} instead.
     */
    @Deprecated
    public Status getStatus(Throwable throwable, Request request,
            Response response) {
        Status result;

        Status defaultStatus = Status.SERVER_ERROR_INTERNAL;
        Throwable t = throwable;

        // If throwable is a ResourceException, use its status and the cause.
        if (throwable instanceof ResourceException) {
            defaultStatus = ((ResourceException) throwable).getStatus();

            if (throwable.getCause() != null
                    && throwable.getCause() != throwable) {
                t = throwable.getCause();
            }
        }

        // [ifndef gwt]
        // look for Status annotation
        org.restlet.engine.resource.ThrowableAnnotationInfo tai = org.restlet.engine.resource.AnnotationUtils
                .getInstance().getThrowableAnnotationInfo(t.getClass());

        if (tai != null) {
            result = new Status(tai.getStatus(), t);
        } else {
            result = new Status(defaultStatus, t);
        }
        // [enddef]
        // [ifdef gwt] instruction uncomment
        // result = new Status(defaultStatus, t);

        return result;
    }

    /**
     * Returns a status for a given exception or error. By default it returns an
     * {@link Status#SERVER_ERROR_INTERNAL} status and logs a severe message.<br>
     * In order to customize the default behavior, this method can be
     * overridden.
     * 
     * @param throwable
     *            The exception or error caught.
     * @param resource
     *            The parent resource.
     * @return The representation of the given status.
     * @deprecated Use {@link #toStatus(Throwable, Resource)} instead.
     */
    @Deprecated
    public Status getStatus(Throwable throwable, Resource resource) {
        return toStatus(throwable,
                (resource == null) ? null : resource.getRequest(),
                (resource == null) ? null : resource.getResponse());
    }

    /**
     * Indicates if an existing entity should be overwritten. False by default.
     * 
     * @return True if an existing entity should be overwritten.
     */
    public boolean isOverwriting() {
        return this.overwriting;
    }

    // [ifndef gwt] method
    /**
     * Sets the service used to select the preferred variant.
     * 
     * @param connegService
     *            The service used to select the preferred variant.
     */
    public void setConnegService(ConnegService connegService) {
        this.connegService = connegService;
    }

    /**
     * Sets the email address to contact in case of error. This is typically
     * used when creating the status representations.
     * 
     * @param contactEmail
     *            The email address to contact in case of error.
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    // [ifndef gwt] method
    /**
     * Sets the service used to convert between status/throwable and
     * representation.
     * 
     * @param converterService
     *            The service used to convert between status/throwable and
     *            representation.
     */
    public void setConverterService(ConverterService converterService) {
        this.converterService = converterService;
    }

    /**
     * Sets the home URI to propose in case of error.
     * 
     * @param homeRef
     *            The home URI to propose in case of error.
     */
    public void setHomeRef(Reference homeRef) {
        this.homeRef = homeRef;
    }

    // [ifndef gwt] method
    /**
     * Sets the service used to select the preferred variant.
     * 
     * @param metadataService
     *            The service used to select the preferred variant.
     */
    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Indicates if an existing entity should be overwritten.
     * 
     * @param overwriting
     *            True if an existing entity should be overwritten.
     */
    public void setOverwriting(boolean overwriting) {
        this.overwriting = overwriting;
    }

    /**
     * Returns a representation for the given status. In order to customize the
     * default representation, this method can be overridden. It returns a
     * {@link org.restlet.data.Status} representation by default or a
     * {@link java.lang.Throwable} representation if the throwable is annotated
     * with {@link org.restlet.resource.Status}.
     * 
     * @param status
     *            The status to represent.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    public Representation toRepresentation(Status status, Request request,
            Response response) {
        return getRepresentation(status, request, response);
    }

    /**
     * Returns a representation for the given status.<br>
     * In order to customize the default representation, this method can be
     * overridden. By default it invokes
     * {@link #toRepresentation(Status, Request, Response)}
     * 
     * @param status
     *            The status to represent.
     * @param resource
     *            The parent resource.
     * @return The representation of the given status.
     */
    public Representation toRepresentation(Status status, Resource resource) {
        return toRepresentation(status, resource.getRequest(),
                resource.getResponse());
    }

    /**
     * Returns a status for a given exception or error. By default it unwraps
     * the status of {@link ResourceException}. For other exceptions or errors,
     * it returns an {@link Status#SERVER_ERROR_INTERNAL} status.<br>
     * <br>
     * In order to customize the default behavior, this method can be
     * overridden.
     * 
     * @param throwable
     *            The exception or error caught.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    public Status toStatus(Throwable throwable, Request request,
            Response response) {
        return getStatus(throwable, request, response);
    }

    /**
     * Returns a status for a given exception or error. By default it returns an
     * {@link Status#SERVER_ERROR_INTERNAL} status and logs a severe message.<br>
     * In order to customize the default behavior, this method can be
     * overridden.
     * 
     * @param throwable
     *            The exception or error caught.
     * @param resource
     *            The parent resource.
     * @return The representation of the given status.
     */
    public Status toStatus(Throwable throwable, Resource resource) {
        return getStatus(throwable, resource);
    }
}
