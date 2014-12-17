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

import java.io.IOException;
import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.application.StatusInfo;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.service.StatusService;

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
     * @see StatusService#toRepresentation(Status, Request, Response)
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
     * Returns a representation for the given status.<br>
     * In order to customize the default representation, this method can be
     * overridden.
     * 
     * @param status
     *            The status info to represent.
     * @return The representation of the given status.
     */
    protected Representation toHtml(StatusInfo status) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("   <title>Status page</title>\n");
        sb.append("</head>\n");
        sb.append("<body style=\"font-family: sans-serif;\">\n");

        sb.append("<p style=\"font-size: 1.2em;font-weight: bold;margin: 1em 0px;\">");
        sb.append(StringUtils.htmlEscape(getStatusLabel(status)));
        sb.append("</p>\n");
        if (status.getDescription() != null) {
            sb.append("<p>");
            sb.append(StringUtils.htmlEscape(status.getDescription()));
            sb.append("</p>\n");
        }

        sb.append("<p>You can get technical details <a href=\"");
        sb.append(status.getUri());
        sb.append("\">here</a>.<br>\n");

        if (status.getContactEmail() != null) {
            sb.append("For further assistance, you can contact the <a href=\"mailto:");
            sb.append(status.getContactEmail());
            sb.append("\">administrator</a>.<br>\n");
        }

        if (status.getHomeRef() != null) {
            sb.append("Please continue your visit at our <a href=\"");
            sb.append(status.getHomeRef());
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