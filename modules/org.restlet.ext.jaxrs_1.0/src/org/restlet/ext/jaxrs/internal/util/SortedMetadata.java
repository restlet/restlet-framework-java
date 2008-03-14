/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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

import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;

/**
 * SortedMetadata contains given Metadata, sorted by it's quality, Metadata with
 * the highest quality first.
 * 
 * @author Stephan Koops
 * @param <T> the Metadata type the instance contains.
 */
public class SortedMetadata<T extends Metadata> implements Iterable<T> {

    private List<Collection<T>> metadatas;

    /**
     * Creates a new SortedMetadata from the given Metadata.
     * 
     * @param preferences
     */
    @SuppressWarnings("unchecked")
    public SortedMetadata(Collection<Preference<T>> preferences) {
        if (preferences.isEmpty()) {
            this.metadatas = Collections
                    .singletonList((Collection<T>) Collections
                            .singletonList(MediaType.ALL));
            return;
        }
        SortedMap<Float, Collection<T>> map = new TreeMap<Float, Collection<T>>(
                Collections.reverseOrder());
        for (Preference<T> preference : preferences) {
            Float quality = preference.getQuality();
            Collection<T> metadatas = map.get(quality);
            if (metadatas == null) {
                metadatas = new ArrayList<T>(2);
                map.put(quality, metadatas);
            }
            metadatas.add(preference.getMetadata());
        }
        this.metadatas = new ArrayList<Collection<T>>(map.values());
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
     * Iterates over all given Metadata.
     * 
     * @see java.lang.Iterable#iterator()
     */
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return new IteratorIterator(metadatas.iterator());
    }

    @SuppressWarnings("hiding")
    private class IteratorIterator<T extends Metadata> implements Iterator<T> {
        private Iterator<Iterable<T>> iterIter;

        private Iterator<T> iter;

        IteratorIterator(Iterator<Iterable<T>> iterIter) {
            this.iterIter = iterIter;
        }

        public boolean hasNext() {
            if (iter != null && iter.hasNext())
                return true;
            while (iterIter.hasNext()) {
                Iterable<T> iterable = iterIter.next();
                if (iterable != null) {
                    iter = iterable.iterator();
                    if (iter.hasNext())
                        return true;
                }
            }
            return false;
        }

        public T next() {
            if (this.hasNext())
                return iter.next();
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
     * Returns the list of collections as {@link Iterable} of {@link Iterable}s.
     * 
     * @return the list of collections as {@link Iterable} of {@link Iterable}s.
     */
    @SuppressWarnings("unchecked")
    public Iterable<Iterable<T>> listOfColls() {
        return (Iterable) this.metadatas;
    }

    /**
     * Checks, if this SortedMetadata is empty
     * 
     * @return
     * @see Collection#isEmpty()
     */
    public boolean isEmpty() {
        return this.metadatas.isEmpty();
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
     * Creates a SortedMetadata collection with exactly the {@link MediaType} '*<!---->/*''
     * 
     * @return
     * @see MediaType#ALL
     */
    public static SortedMetadata<MediaType> getMediaTypeAll() {
        return new SortedMetadata<MediaType>(Collections.singletonList(Util
                .createColl(MediaType.ALL)));
    }

    /**
     * 
     * @param mediaType
     * @return
     * @see Collections#singleton(Object)
     */
    public static SortedMetadata<MediaType> singleton(MediaType mediaType) {
        return new SortedMetadata<MediaType>(Collections.singletonList(Util
                .createColl(mediaType)));
    }

    @Override
    public String toString() {
        return this.metadatas.toString();
    }
}