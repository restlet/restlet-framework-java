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

package org.restlet.ext.velocity;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Resolver;

/**
 * Filter response's entity and wrap it with a FreeMarker's template
 * representation.
 * 
 * @author Thierry Boileau (contact@noelios.com)
 */
public class TemplateFilter extends Filter {
    private class ResolverMap implements Map<String, Object> {
        private Resolver<String> resolver;

        public ResolverMap(Resolver<String> resolver) {
            super();
            this.resolver = resolver;
        }

        public void clear() {
        }

        public boolean containsKey(Object key) {
            return resolver.resolve((String) key) != null;
        }

        public boolean containsValue(Object value) {
            return false;
        }

        public Set<Entry<String, Object>> entrySet() {
            return null;
        }

        public String get(Object key) {
            return resolver.resolve((String) key);
        }

        public boolean isEmpty() {
            return false;
        }

        public Set<String> keySet() {
            return null;
        }

        public String put(String key, Object value) {
            return null;
        }

        public void putAll(Map<? extends String, ? extends Object> t) {
        }

        public String remove(Object key) {
            return null;
        }

        public int size() {
            return 0;
        }

        public Collection<Object> values() {
            return null;
        }

    }

    /**
     * Constructor.
     */
    public TemplateFilter() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     */
    public TemplateFilter(Context context) {
        super(context);
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param next
     *                The next Restlet.
     */
    public TemplateFilter(Context context, Restlet next) {
        super(context, next);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (response.isEntityAvailable()
                && response.getEntity().getEncodings().contains(
                        Encoding.VELOCITY)) {
            try {
                response.setEntity(new TemplateRepresentation(response
                        .getEntity(), new ResolverMap(Resolver.createResolver(
                        request, response))));
            } catch (ResourceNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseErrorException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
