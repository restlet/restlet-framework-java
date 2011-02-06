/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.atom.internal;

import java.util.ArrayList;

import org.restlet.data.Reference;
import org.restlet.ext.atom.Categories;
import org.restlet.ext.atom.Category;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Content reader for categories.
 * 
 * @author Jerome Louvel
 */
public class CategoriesContentReader extends DefaultHandler {

    private Categories categories = null;

    /**
     * Constructor.
     * 
     * @param categories
     *            The parent categories.
     */
    public CategoriesContentReader(Categories categories) {
        this.categories = categories;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        if (uri.equalsIgnoreCase(Service.APP_NAMESPACE)) {
            if (localName.equalsIgnoreCase("categories")) {
                String attr = attrs.getValue("", "fixed");
                this.categories.setFixed((attr == null) ? false : Boolean
                        .parseBoolean(attr));
                attr = attrs.getValue("", "scheme");
                this.categories.setScheme((attr == null) ? null
                        : new Reference(attr));
            }
        } else if (uri.equalsIgnoreCase(Feed.ATOM_NAMESPACE)) {
            if (localName.equalsIgnoreCase("category")) {
                Category category = new Category();

                if (this.categories.getEntries() == null) {
                    this.categories.setEntries(new ArrayList<Category>());
                }
                this.categories.getEntries().add(category);

                String attr = attrs.getValue("", "term");
                category.setTerm((attr == null) ? null : attr);
                attr = attrs.getValue("", "label");
                category.setLabel((attr == null) ? null : attr);
                attr = attrs.getValue("", "scheme");
                category.setScheme((attr == null) ? null : new Reference(attr));

                if (category.getScheme() == null) {
                    category.setScheme(this.categories.getScheme());
                }
            }
        }
    }
}