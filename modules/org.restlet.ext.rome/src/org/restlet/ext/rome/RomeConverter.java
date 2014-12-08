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

package org.restlet.ext.rome;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Converter of SyndFeed objects into Representation instances.
 * 
 * @author Thierry Boileau
 * @deprecated Not actively developed anymore.
 */
@Deprecated
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
    public float score(Object source, Variant target, Resource resource) {
        if (source instanceof SyndFeed) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        float result = -1.0F;

        if ((source != null) && (SyndFeed.class.isAssignableFrom(target))) {
            result = 1.0F;
        }

        return result;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        SyndFeedRepresentation syndFeedSource = null;
        if (source instanceof SyndFeedRepresentation) {
            syndFeedSource = (SyndFeedRepresentation) source;
        } else {
            syndFeedSource = new SyndFeedRepresentation(source);
        }

        T result = null;
        if ((target != null) && SyndFeed.class.isAssignableFrom(target)) {
            result = target.cast(syndFeedSource.getFeed());
        }

        return result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) throws IOException {
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
