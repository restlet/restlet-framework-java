package com.google.gwt.emul.java.util.concurrent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Emulate the CopyOnWriteArraySet class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 */
public class CopyOnWriteArraySet<E> implements Set<E> {

    private Set<E> set;

    public CopyOnWriteArraySet() {
        super();
        this.set = new HashSet<E>();
    }

    public boolean add(E o) {
        return set.add(o);
    }

    public boolean addAll(Collection<? extends E> c) {
        return set.addAll(c);
    }

    public void clear() {
        set.clear();
    }

    public boolean contains(Object o) {
        return set.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public Iterator<E> iterator() {
        return set.iterator();
    }

    public boolean remove(Object o) {
        return set.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    public int size() {
        return set.size();
    }

    public Object[] toArray() {
        return set.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

}
