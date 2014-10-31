/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.converter.ConverterUtils;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.engine.util.ThrowableSerializer;
import org.restlet.representation.Representation;
import org.restlet.representation.StatusRepresentation;
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
 * Application via the setStatusService() methods.
 * 
 * @see <a href="http://wiki.restlet.org/docs_2.2/202-restlet.html">User
 *      Guide</a>
 * @author Jerome Louvel
 */
public class StatusService extends Service {

    /** HTML Variant */
    private static final VariantInfo VARIANT_HTML = new VariantInfo(
            MediaType.TEXT_HTML);

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

    /**
     * Constructor. By default, it creates the necessary services.
     */
    public StatusService() {
        this(true, new ConverterService(), new MetadataService(),
                new ConnegService());
    }

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
     * @deprecated Use
     *             {@link #toRepresentation(Status, Throwable, Request, Response)}
     *             instead.
     */
    @Deprecated
    public Representation getRepresentation(Status status, Request request,
            Response response) {
        // [ifndef gwt] instruction
        return toRepresentation(status, null, request, response);
        // [ifdef gwt] instruction uncomment
        // return toRepresentation(status, null, request, response);
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
        return toStatus(throwable, request, response);
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
        return toStatus(throwable, resource);
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
     * @param throwable
     *            The exception or error caught. If null, use
     *            {@link org.restlet.data.Status#getThrowable()}.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    public Representation toRepresentation(Status status, Throwable throwable,
            Request request, Response response) {
        Representation result = null;

        // do content negotiation for status
        if (converterService != null && connegService != null
                && metadataService != null) {
            Object representationObject = null;

            // serialize exception if any and if {@link
            // org.restlet.resource.Status} annotation ask for it
            // [ifndef gwt]
            Throwable cause = throwable != null ? throwable : status
                    .getThrowable();
            if (cause != null) {
                org.restlet.engine.resource.StatusAnnotationInfo sai = org.restlet.engine.resource.AnnotationUtils
                        .getInstance()
                        .getStatusAnnotationInfo(cause.getClass());
                if (sai != null && sai.isSerializable()) {
                    try {
                        representationObject = ThrowableSerializer
                                .serializeToMap(cause);
                    } catch (Exception e) {
                        Context.getCurrentLogger().log(
                                Level.WARNING,
                                "Could not serialize throwable class "
                                        + cause.getClass(), e);
                    }
                }
            }
            // [enddef]

            // default representation match with the status properties
            if (representationObject == null) {
                representationObject = new StatusRepresentation(status);
            }

            List<VariantInfo> variants = ConverterUtils.getVariants(
                    representationObject.getClass(), null);
            // TODO This seems to be a workaround in order to prevent zealous
            // converters to cope with conversions whereas they are not supposed
            // to. Should be updated when introducing strict mode of content
            // negotiation.
            if (!variants.contains(VARIANT_HTML)) {
                variants.add(VARIANT_HTML);
            }
            Variant variant = connegService.getPreferredVariant(variants,
                    request, metadataService);
            try {
                result = converterService.toRepresentation(
                        representationObject, variant);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * Returns a representation for the given status.<br>
     * In order to customize the default representation, this method can be
     * overridden. By default it invokes
     * {@link #toRepresentation(Status, Throwable, Request, Response)}
     * 
     * @param status
     *            The status to represent.
     * @param throwable
     *            The exception or error caught.
     * @param resource
     *            The parent resource.
     * @return The representation of the given status.
     */
    public Representation toRepresentation(Status status, Throwable throwable,
            Resource resource) {
        // [ifndef gwt] instruction
        return toRepresentation(status, throwable, resource.getRequest(),
                resource.getResponse());
        // [ifdef gwt] instruction uncomment
        // return null;
    }

    // [ifdef gwt] method uncomment
    // /**
    // * Returns a representation for the given status. In order to customize
    // the
    // * default representation, this method can be overridden. It returns null
    // by
    // * default.
    // *
    // * @param status
    // * The status to represent.
    // * @param throwable
    // * The exception or error caught.
    // * @param request
    // * The request handled.
    // * @param response
    // * The response updated.
    // * @return The representation of the given status.
    // */
    // public Representation toRepresentation(Status status, Throwable
    // throwable,
    // Request request, Response response) {
    // return null;
    // }

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
        org.restlet.engine.resource.StatusAnnotationInfo sai = org.restlet.engine.resource.AnnotationUtils
                .getInstance().getStatusAnnotationInfo(t.getClass());

        if (sai != null) {
            result = new Status(sai.getStatus(), t);
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
     */
    public Status toStatus(Throwable throwable, Resource resource) {
        return toStatus(throwable,
                (resource == null) ? null : resource.getRequest(),
                (resource == null) ? null : resource.getResponse());
    }

    /**
     * 
     * @param status
     * @param representation
     * @return
     */
    public Throwable toThrowable(Status status, Representation representation) {
        Throwable result = null;

        return result;
    }

}
