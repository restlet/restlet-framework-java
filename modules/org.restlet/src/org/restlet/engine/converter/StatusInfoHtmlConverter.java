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

package org.restlet.engine.converter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.application.StatusInfo;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import java.io.IOException;
import java.util.List;

/**
 * Converter for the {@link StatusInfo} class.
 * 
 * @author Manuel Boillod
 */
public class StatusInfoHtmlConverter extends ConverterHelper {

    /** Variant with media type application/xhtml+xml. */
    private static final VariantInfo VARIANT_APPLICATION_XHTML = new VariantInfo(
            MediaType.APPLICATION_XHTML);

    /** Variant with media type text/html. */
    private static final VariantInfo VARIANT_TEXT_HTML = new VariantInfo(
            MediaType.TEXT_HTML);

    /** The email address of the administrator to contact in case of error. */
    private String contactEmail;

    /** The home URI to propose in case of error. */
    private String homeRef;

    /**
     * Returns the email address of the administrator to contact in case of
     * error.
     *
     * @return The email address.
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * Returns the home URI to propose in case of error.
     *
     * @return The home URI.
     */
    public String getHomeRef() {
        return homeRef;
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (isCompatible(source)) {
            result = addObjectClass(result, StatusInfo.class);
        }

        return result;
    }

    /**
     * Returns the status information to display in the default representation.
     * By default it returns the status's reason phrase.
     * 
     * @param status
     *            The status.
     * @return The status information.
     * @see #toHtml(org.restlet.engine.application.StatusInfo)
     */
    protected String getStatusLabel(StatusInfo status) {
        return (status.getReasonPhrase() != null) ? status.getReasonPhrase()
                : "No information available for this result status";
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) throws IOException {
        List<VariantInfo> result = null;

        if (source != null && StatusInfo.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_TEXT_HTML);
            result = addVariant(result, VARIANT_APPLICATION_XHTML);
        }

        return result;
    }

    /**
     * Indicates if the given variant is compatible with the media types
     * supported by this converter.
     *
     * @param variant
     *            The variant.
     * @return True if the given variant is compatible with the media types
     *         supported by this converter.
     */
    protected boolean isCompatible(Variant variant) {
        return (variant != null)
                && (VARIANT_TEXT_HTML.isCompatible(variant) || VARIANT_APPLICATION_XHTML
                        .isCompatible(variant));
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof StatusInfo && isCompatible(target)) {
            result = 1.0F;
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        return -1.0F;
    }

    /**
     * Sets the email address of the administrator to contact in case of error.
     *
     * @param email
     *            The email address.
     */
    public void setContactEmail(String email) {
        this.contactEmail = email;
    }

    /**
     * Sets the home URI to propose in case of error.
     *
     * @param homeRef
     *            The home URI.
     */
    public void setHomeRef(String homeRef) {
        this.homeRef = homeRef;
    }

    /**
     * Returns a representation for the given status.<br>
     * In order to customize the default representation, this method can be
     * overridden.
     * 
     * @param statusInfo
     *            The status info to represent.
     * @return The representation of the given status.
     */
    protected Representation toHtml(StatusInfo statusInfo) {
        Status status = Status.valueOf(statusInfo.getCode());
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("   <title>Status page</title>\n");
        sb.append("</head>\n");
        sb.append("<body style=\"font-family: sans-serif;\">\n");

        sb.append("<p style=\"font-size: 1.2em;font-weight: bold;margin: 1em 0px;\">");
        sb.append(StringUtils.htmlEscape(getStatusLabel(statusInfo)));
        sb.append("</p>\n");
        if (statusInfo.getDescription() != null) {
            sb.append("<p>");
            sb.append(StringUtils.htmlEscape(statusInfo.getDescription()));
            sb.append("</p>\n");
        }

        sb.append("<p>You can get technical details <a href=\"");
        sb.append(status.getUri());
        sb.append("\">here</a>.<br>\n");

        if (getContactEmail() != null) {
            sb.append("For further assistance, you can contact the <a href=\"mailto:");
            sb.append(getContactEmail());
            sb.append("\">administrator</a>.<br>\n");
        }

        if (getHomeRef() != null) {
            sb.append("Please continue your visit at our <a href=\"");
            sb.append(getHomeRef());
            sb.append("\">home page</a>.\n");
        }

        sb.append("</p>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        return null;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) throws IOException {
        Representation result = null;

        if (source != null
                && StatusInfo.class.isAssignableFrom(source.getClass())) {
            StatusInfo si = (StatusInfo) source;
            result = toHtml(si);
        }

        return result;
    }
}