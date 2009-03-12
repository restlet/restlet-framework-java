/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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
 
 package org.restlet.ext.rdf.internal;

import java.io.IOException;

public abstract class LexicalUnit {

    private RdfN3ContentHandler contentHandler;

    private Context context;

    private LexicalUnit parent;

    private boolean parsed;

    private String value;

    public LexicalUnit(String value) {
        super();
        setValue(value);
    }

    public LexicalUnit(RdfN3ContentHandler contentHandler, Context context) {
        super();
        this.contentHandler = contentHandler;
        this.context = context;
    }

    public LexicalUnit(RdfN3ContentHandler contentHandler, LexicalUnit parent,
            Context context) {
        this(contentHandler, context);
        this.parent = parent;
    }

    public RdfN3ContentHandler getContentHandler() {
        return contentHandler;
    }

    public Context getContext() {
        return context;
    }

    public LexicalUnit getParent() {
        return parent;
    }

    public String getValue() {
        return value;
    }

    public boolean isParsed() {
        return parsed;
    }

    public abstract void parse() throws IOException;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setParent(LexicalUnit parent) {
        this.parent = parent;
    }

    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    public void setValue(String value) {
        this.value = value;
    }

}