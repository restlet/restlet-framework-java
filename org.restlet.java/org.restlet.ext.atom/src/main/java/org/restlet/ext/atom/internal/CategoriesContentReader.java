/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
 * @deprecated Will be removed in next major release.
 */
@Deprecated
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
