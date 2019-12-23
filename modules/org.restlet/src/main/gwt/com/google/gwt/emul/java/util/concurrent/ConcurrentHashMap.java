/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package com.google.gwt.emul.java.util.concurrent;

import java.util.TreeMap;

/**
 * Emulate the ConcurrentHashMap class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 */
public class ConcurrentHashMap<K, V> extends TreeMap<K, V> implements
        ConcurrentMap<K, V> {

    /** */
    private static final long serialVersionUID = 1L;

    public V putIfAbsent(K key, V value) {
        if (!containsKey(key)) {
            return put(key, value);
        } else {
            return get(key);
        }

    }

    public boolean remove(Object key, Object value) {
        boolean result = false;
        if (containsKey(key) && get(key).equals(value)) {
            remove(key);
            result = true;
        }
        return result;
    }

    public V replace(K key, V value) {
        V result = null;

        if (containsKey(key)) {
            result = put(key, value);
        }

        return result;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        boolean result = false;

        if (containsKey(key) && get(key).equals(oldValue)) {
            put(key, newValue);
            result = true;
        }

        return result;
    }

}
