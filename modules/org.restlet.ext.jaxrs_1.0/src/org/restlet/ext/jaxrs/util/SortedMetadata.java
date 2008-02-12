package org.restlet.ext.jaxrs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.restlet.data.MediaType;
import org.restlet.data.Metadata;

/**
 * TODO javadoc SortedMetadata
 * 
 * @author Stephan Koops
 * @param <T>
 */
public class SortedMetadata<T extends Metadata> implements Iterable<T> {

    private List<Collection<T>> metadatas;

    /**
     * 
     * @param metadatas
     */
    public SortedMetadata(List<Collection<T>> metadatas) {
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
}