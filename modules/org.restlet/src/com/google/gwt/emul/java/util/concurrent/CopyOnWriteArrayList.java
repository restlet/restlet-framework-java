package com.google.gwt.emul.java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Emulate the CopyOnWriteArrayList class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 */
public class CopyOnWriteArrayList<E> implements List<E> {

    private List<E> list;

    public CopyOnWriteArrayList() {
        super();
        list = new ArrayList<E>();
    }

    public boolean add(E o) {
        return list.add(o);
    }

    public void add(int index, E element) {
        list.add(index, element);
    }

    public boolean addAll(Collection<? extends E> c) {
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, c);
    }

    public void clear() {
        list.clear();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public E get(int index) {
        return list.get(index);
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Iterator<E> iterator() {
        return list.iterator();
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public E remove(int index) {
        return list.remove(index);
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    public E set(int index, E element) {
        return list.set(index, element);
    }

    public int size() {
        return list.size();
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

}
