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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.converter.ConverterUtils;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Application service converting between representation and regular Java
 * objects. The conversion can work in both directions. Actual converters can be
 * plugged into the engine to support this service.<br>
 * <br>
 * Root object classes used for conversion shouldn't be generic classes
 * otherwise important contextual type information will be missing at runtime
 * due to Java type erasure mechanism. If needed, create a fully resolved
 * subclasses and/or a container classes.
 * 
 * @author Jerome Louvel
 */
public class ConverterService extends Service {

    /**
     * Constructor.
     */
    public ConverterService() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public ConverterService(boolean enabled) {
        super(enabled);
    }

    /**
     * Applies a patch representation to an initial representation in order to
     * obtain a modified one. The patch must have a recognized media type in
     * order for the {@link ConverterService} to be able to process it.
     * 
     * @param initial
     *            The initial representation on which the patch must be applied.
     * @param patch
     *            The patch representation to apply.
     * @return The modified representation.
     * @throws IOException
     */
    public Representation applyPatch(Representation initial,
            Representation patch) throws IOException {

        return null;
    }

    /**
     * Creates a patch representation by calculating a diff between initial and
     * modified representations.
     * 
     * @param initial
     *            The initial representation.
     * @param modified
     *            The modified representation.
     * @return The patch representation able to convert the initial
     *         representation into the modified representation.
     * @throws IOException
     */
    public Representation createPatch(Representation initial,
            Representation modified) throws IOException {

        return null;
    }

    /**
     * Returns the list of object classes that can be converted from a given
     * variant.
     * 
     * @param source
     *            The source variant.
     * @return The list of object class that can be converted.
     */
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;
        List<Class<?>> helperObjectClasses = null;

        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {
            helperObjectClasses = ch.getObjectClasses(source);

            if (helperObjectClasses != null) {
                if (result == null) {
                    result = new ArrayList<Class<?>>();
                }

                result.addAll(helperObjectClasses);
            }
        }

        return result;
    }

    /**
     * Returns the list of patch media types available for the given
     * representation types.
     * 
     * @param representationType
     *            The representation media type or null for all supported patch
     *            types.
     * @return The list of patch media types available.
     */
    public List<MediaType> getPatchTypes(MediaType representationType) {
        return null;
    }

    /**
     * Returns the list of variants that can be converted from a given object
     * class.
     * 
     * @param source
     *            The source class.
     * @param target
     *            The expected representation metadata.
     * @return The list of variants that can be converted.
     * @throws IOException
     */
    public List<? extends Variant> getVariants(Class<?> source, Variant target)
            throws IOException {
        return ConverterUtils.getVariants(source, target);
    }

    /**
     * Reverts a patch representation from a modified representation in order to
     * obtain the initial one. The patch must have a recognized media type in
     * order for the {@link ConverterService} to be able to process it.
     * 
     * @param modified
     *            The modified representation from which the patch must be
     *            reverted.
     * @param patch
     *            The patch representation to revert.
     * @return The initial representation.
     * @throws IOException
     */
    public Representation revertPatch(Representation modified,
            Representation patch) throws IOException {

        return null;
    }

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param source
     *            The source representation to convert.
     * @return The converted Java object.
     * @throws IOException
     */
    public Object toObject(Representation source) throws IOException {
        return toObject(source, null, null);
    }

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param source
     *            The source representation to convert.
     * @param target
     *            The target class of the Java object.
     * @param resource
     *            The parent resource.
     * @return The converted Java object.
     * @throws IOException
     */
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        T result = null;
        boolean loggable = (resource == null) ? true : resource.isLoggable();

        if ((source != null) && source.isAvailable() && (source.getSize() != 0)) {
            ConverterHelper ch = ConverterUtils.getBestHelper(source, target,
                    resource);

            if (ch != null) {
                if (loggable
                        && Context.getCurrentLogger().isLoggable(Level.FINE)) {
                    Context.getCurrentLogger().fine(
                            "The following converter was selected for the "
                                    + source + " representation: " + ch);
                }

                result = ch.toObject(source, target, resource);

                if (result instanceof Representation) {
                    Representation resultRepresentation = (Representation) result;

                    // Copy the variant metadata
                    resultRepresentation.setCharacterSet(source
                            .getCharacterSet());
                    resultRepresentation.setMediaType(source.getMediaType());
                    resultRepresentation.getEncodings().addAll(
                            source.getEncodings());
                    resultRepresentation.getLanguages().addAll(
                            source.getLanguages());
                }
            } else {
                if (loggable) {
                    Context.getCurrentLogger().warning(
                            "Unable to find a converter for this representation : "
                                    + source);
                }
            }
        }

        return result;
    }

    /**
     * Converts a regular Java object into a Representation. The converter will
     * use the preferred variant of the selected converter.
     * 
     * @param source
     *            The source object to convert.
     * @return The converted representation.
     * @throws IOException
     */
    public Representation toRepresentation(Object source) throws IOException {
        return toRepresentation(source, null, null);
    }

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param source
     *            The source object to convert.
     * @param target
     *            The target representation media type.
     * @return The converted representation.
     * @throws IOException
     */
    public Representation toRepresentation(Object source, MediaType target)
            throws IOException {
        return toRepresentation(source, new Variant(target));
    }

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param source
     *            The source object to convert.
     * @param target
     *            The target representation variant.
     * @return The converted representation.
     * @throws IOException
     */
    public Representation toRepresentation(Object source, Variant target)
            throws IOException {
        return toRepresentation(source, target, null);
    }

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param source
     *            The source object to convert.
     * @param target
     *            The target representation variant.
     * @param resource
     *            The parent resource.
     * @return The converted representation.
     * @throws IOException
     */
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) throws IOException {
        Representation result = null;
        boolean loggable = (resource == null) ? true : resource.isLoggable();
        ConverterHelper ch = ConverterUtils.getBestHelper(source, target,
                resource);

        if (ch != null) {
            if (loggable && Context.getCurrentLogger().isLoggable(Level.FINE)) {
                Context.getCurrentLogger().fine(
                        "Converter selected for "
                                + source.getClass().getSimpleName() + ": "
                                + ch.getClass().getSimpleName());
            }

            if (target == null) {
                List<VariantInfo> variants = ch.getVariants(source.getClass());

                if ((variants != null) && !variants.isEmpty()) {
                    if (resource != null) {
                        target = resource.getConnegService()
                                .getPreferredVariant(variants,
                                        resource.getRequest(),
                                        resource.getMetadataService());
                    } else {
                        target = variants.get(0);
                    }
                } else {
                    target = new Variant();
                }
            }

            result = ch.toRepresentation(source, target, resource);

            if (result != null) {
                // Copy the variant metadata if necessary
                if (result.getCharacterSet() == null) {
                    result.setCharacterSet(target.getCharacterSet());
                }

                if ((result.getMediaType() == null)
                        || !result.getMediaType().isConcrete()) {
                    if ((target.getMediaType() != null)
                            && target.getMediaType().isConcrete()) {
                        result.setMediaType(target.getMediaType());
                    } else if (resource != null) {
                        result.setMediaType(resource.getMetadataService()
                                .getDefaultMediaType());
                    } else {
                        result.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
                    }
                }

                if (result.getEncodings().isEmpty()) {
                    result.getEncodings().addAll(target.getEncodings());
                }

                if (result.getLanguages().isEmpty()) {
                    result.getLanguages().addAll(target.getLanguages());
                }
            }
        } else {
            if (loggable) {
                Context.getCurrentLogger().warning(
                        "Unable to find a converter for this object : "
                                + source);
            }
        }

        return result;
    }

    /**
     * Updates the media type preferences with available conversion capabilities
     * for the given entity class.
     * 
     * @param preferences
     *            The media type preferences.
     * @param entity
     *            The entity class to convert.
     */
    public void updatePreferences(List<Preference<MediaType>> preferences,
            Class<?> entity) {
        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {
            ch.updatePreferences(preferences, entity);
        }
    }
}
