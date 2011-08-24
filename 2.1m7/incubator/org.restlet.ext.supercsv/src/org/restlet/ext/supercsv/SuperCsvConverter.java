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

package org.restlet.ext.supercsv;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Converter between the CSV and Representation classes based on SuperCSV.
 * 
 * @author Jerome Louvel
 */
public class SuperCsvConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_CSV = new VariantInfo(
            MediaType.TEXT_CSV);

    /**
     * Creates the marshaling {@link SuperCsvRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link SuperCsvRepresentation}.
     */
    protected <T> SuperCsvRepresentation<T> create(MediaType mediaType, T source) {
        return new SuperCsvRepresentation<T>(mediaType, source);
    }

    /**
     * Creates the unmarshaling {@link SuperCsvRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @param objectClass
     *            The object class to instantiate.
     * @return The unmarshaling {@link SuperCsvRepresentation}.
     */
    protected <T> SuperCsvRepresentation<T> create(Representation source,
            Class<T> objectClass) {
        return new SuperCsvRepresentation<T>(source, objectClass);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_CSV.isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, SuperCsvRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null) {
            result = addVariant(result, VARIANT_CSV);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof SuperCsvRepresentation<?>) {
            result = 1.0F;
        } else {
            if (target == null) {
                result = 0.5F;
            } else if (VARIANT_CSV.isCompatible(target)) {
                result = 0.8F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        float result = -1.0F;

        if (source instanceof SuperCsvRepresentation<?>) {
            result = 1.0F;
        } else if ((target != null)
                && SuperCsvRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if (VARIANT_CSV.isCompatible(source)) {
            result = 0.8F;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        Object result = null;

        // The source for the SuperCSV conversion
        SuperCsvRepresentation<?> superCsvSource = null;

        if (source instanceof SuperCsvRepresentation) {
            superCsvSource = (SuperCsvRepresentation<?>) source;
        } else if (VARIANT_CSV.isCompatible(source)) {
            superCsvSource = create(source, target);
        }

        if (superCsvSource != null) {
            // Handle the conversion
            if ((target != null)
                    && SuperCsvRepresentation.class.isAssignableFrom(target)) {
                result = superCsvSource;
            } else {
                result = superCsvSource.getObject();
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) {
        Representation result = null;

        if (source instanceof SuperCsvRepresentation) {
            result = (SuperCsvRepresentation<?>) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.TEXT_CSV);
            }

            if (VARIANT_CSV.isCompatible(target)) {
                SuperCsvRepresentation<Object> superCsvRepresentation = create(
                        target.getMediaType(), source);
                result = superCsvRepresentation;
            }
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        updatePreferences(preferences, MediaType.TEXT_CSV, 1.0F);
    }

}
