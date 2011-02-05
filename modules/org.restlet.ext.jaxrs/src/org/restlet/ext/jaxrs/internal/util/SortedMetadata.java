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

package org.restlet.ext.jaxrs.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;

/**
 * SortedMetadata contains given Metadata, sorted by it's quality, Metadata with
 * the highest quality first.
 * 
 * @author Stephan Koops
 * @param <T>
 *            the Metadata type the instance contains.
 */
public class SortedMetadata<T extends Metadata> implements Iterable<T> {

    @SuppressWarnings("hiding")
    private class IteratorIterator<T extends Metadata> implements Iterator<T> {
        private final Iterator<Iterable<T>> iterIter;

        private Iterator<T> iter;

        IteratorIterator(Iterator<Iterable<T>> iterIter) {
            this.iterIter = iterIter;
        }

        public boolean hasNext() {
            if ((this.iter != null) && this.iter.hasNext()) {
                return true;
            }
            while (this.iterIter.hasNext()) {
                final Iterable<T> iterable = this.iterIter.next();
                if (iterable != null) {
                    this.iter = iterable.iterator();
                    if (this.iter.hasNext()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public T next() {
            if (this.hasNext()) {
                return this.iter.next();
            }
            throw new NoSuchElementException();
        }

        /**
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @param respMediaType
     * @return the media type as singleton of SortedMetadata
     */
    public static SortedMetadata<MediaType> get(MediaType respMediaType) {
        return new SortedMetadata<MediaType>(
                Collections.singleton(new Preference<MediaType>(respMediaType)));
    }

    /**
     * Returns an empty SortedMetadata
     * 
     * @return an empty SortedMetadata
     */
    public static SortedMetadata<MediaType> getEmptyMediaTypes() {
        return new SortedMetadata<MediaType>(
                new ArrayList<Collection<MediaType>>());
    }

    /**
     * Creates a new {@link SortedMetadata} for {@link MediaType}s.
     * 
     * @param preferences
     * @return the given languages as SortedMetadata
     */
    public static SortedMetadata<Language> getForLanguages(
            Collection<Preference<Language>> preferences) {
        return new SortedMetadata<Language>(preferences);
    }

    /**
     * Creates a new {@link SortedMetadata} for {@link MediaType}s. If the given
     * Collection is empty, {@link MediaType#ALL} is returned.
     * 
     * @param preferences
     * @return the given media type as SortedMetadata
     */
    public static SortedMetadata<MediaType> getForMediaTypes(
            Collection<Preference<MediaType>> preferences) {
        if (preferences.isEmpty()) {
            return new SortedMetadata<MediaType>(
                    Collections
                            .singletonList((Collection<MediaType>) Collections
                                    .singletonList(MediaType.ALL)));
        }

        return new SortedMetadata<MediaType>(preferences);
    }

    /**
     * Creates a SortedMetadata collection with exactly the {@link MediaType} 
     * '*<!---->/*''
     * 
     * @return '*<!---->/*'' as SortedMediaType
     * @see MediaType#ALL
     */
    public static SortedMetadata<MediaType> getMediaTypeAll() {
        return new SortedMetadata<MediaType>(Collections.singletonList(Util
                .createColl(MediaType.ALL)));
    }

    /**
     * 
     * @param mediaType
     * @return the media type as singleton as SortedMetadata
     * @see Collections#singleton(Object)
     */
    public static SortedMetadata<MediaType> singleton(MediaType mediaType) {
        return new SortedMetadata<MediaType>(Collections.singletonList(Util
                .createColl(mediaType)));
    }

    private final List<Collection<T>> metadatas;

    /**
     * Creates a new SortedMetadata from the given Metadata.
     * 
     * @param preferences
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SortedMetadata(Collection<Preference<T>> preferences) {
        final SortedMap<Float, Collection<T>> map = new TreeMap<Float, Collection<T>>(
                Collections.reverseOrder());

        for (final Preference<T> preference : preferences) {
            final Float quality = preference.getQuality();
            Collection<T> metadatas = map.get(quality);
            if (metadatas == null) {
                metadatas = new ArrayList<T>(2);
                map.put(quality, metadatas);
            }
            metadatas.add(preference.getMetadata());
        }

        final Collection<Collection<T>> values = map.values();
        this.metadatas = Collections.unmodifiableList(new ArrayList(values));
    }

    /**
     * Creates a new SortedMetadata from the sorted Metadata.
     * 
     * @param metadatas
     */
    private SortedMetadata(List<Collection<T>> metadatas) {
        this.metadatas = metadatas;
    }

    /**
     * Checks, if this SortedMetadata is empty
     * 
     * @return true, if this sorted metadata is empty, or false if not.
     * @see Collection#isEmpty()
     */
    public boolean isEmpty() {
        return this.metadatas.isEmpty();
    }

    /**
     * Iterates over all the sorted Metadata.
     * 
     * @see java.lang.Iterable#iterator()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Iterator<T> iterator() {
        return new IteratorIterator(this.metadatas.iterator());
    }

    /**
     * Returns the list of collections as {@link Iterable} of {@link Iterable}s.
     * 
     * @return the list of collections as {@link Iterable} of {@link Iterable}s.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Iterable<Iterable<T>> listOfColls() {
        return (Iterable) this.metadatas;
    }

    @Override
    public String toString() {
        return this.metadatas.toString();
    }
}