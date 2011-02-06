/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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
import org.restlet.resource.UniformResource;

/**
 * Application service converting between representation and regular Java
 * objects. The conversion can work in both directions.<br>
 * <br>
 * By default, the following conversions are supported. Additional ones can be
 * plugged into the engine.
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
     * Returns the list of variants that can be converted from a given object
     * class.
     * 
     * @param source
     *            The source class.
     * @param target
     *            The expected representation metadata.
     * @return The list of variants that can be converted.
     */
    public List<? extends Variant> getVariants(Class<?> source, Variant target) {
        return ConverterUtils.getVariants(source, target);
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
            UniformResource resource) throws IOException {
        T result = null;

        if ((source != null) && source.isAvailable() && (source.getSize() != 0)) {
            ConverterHelper ch = ConverterUtils.getBestHelper(source, target,
                    resource);

            if (ch != null) {
                Context.getCurrentLogger().fine(
                        "The following converter was selected for the "
                                + source + " representation: " + ch);

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
                Context.getCurrentLogger().warning(
                        "Unable to find a converter for this representation : "
                                + source);
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
     */
    public Representation toRepresentation(Object source) {
        return toRepresentation(source, null, null);
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
     */
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) {
        Representation result = null;
        ConverterHelper ch = ConverterUtils.getBestHelper(source, target,
                resource);

        if (ch != null) {
            try {
                Context.getCurrentLogger().fine(
                        "The following converter was selected for the "
                                + source + " object: " + ch);

                if (target == null) {
                    List<VariantInfo> variants = ch.getVariants(source
                            .getClass());

                    if ((variants != null) && !variants.isEmpty()) {
                        if (resource != null) {
                            target = resource.getClientInfo()
                                    .getPreferredVariant(variants,
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
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to convert object to a representation", e);
            }
        } else {
            Context.getCurrentLogger().warning(
                    "Unable to find a converter for this object : " + source);
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
