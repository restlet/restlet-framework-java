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

package org.restlet.ext.rome;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Converter of SyndFeed objects into Representation instances.
 * 
 * @author Thierry Boileau
 * 
 */
public class RomeConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_APPLICATION_ATOM = new VariantInfo(
            MediaType.APPLICATION_ATOM);

    private static final VariantInfo VARIANT_APPLICATION_RSS = new VariantInfo(
            MediaType.APPLICATION_RSS);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_APPLICATION_ATOM.isCompatible(source)
                || VARIANT_APPLICATION_RSS.isCompatible(source)) {
            result = addObjectClass(result, SyndFeed.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (SyndFeed.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_APPLICATION_ATOM);
            result = addVariant(result, VARIANT_APPLICATION_RSS);
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            UniformResource resource) {
        float result = -1.0F;

        if ((source != null) && (SyndFeed.class.isAssignableFrom(target))) {
            result = 1.0F;
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        if (source instanceof SyndFeed) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        Object result = null;

        if (SyndFeed.class.isAssignableFrom(target)) {
            if (source instanceof SyndFeedRepresentation) {
                result = ((SyndFeedRepresentation) source).getFeed();
            } else {
                result = new SyndFeedRepresentation(source).getFeed();
            }
        }

        return target.cast(result);
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) throws IOException {
        if (source instanceof SyndFeed) {
            SyndFeed feed = (SyndFeed) source;

            if (feed.getFeedType() == null) {
                if (VARIANT_APPLICATION_RSS.isCompatible(target)) {
                    feed.setFeedType("rss_2.0");
                } else {
                    feed.setFeedType("atom_1.0");
                }
            }

            return new SyndFeedRepresentation(feed);
        }

        return null;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (SyndFeed.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_ATOM, 1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_RSS, 1.0F);
        }
    }

}
