/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.http.header;

import java.util.List;

import org.restlet.data.Tag;

/**
 * Tag header writer.
 * 
 * @author Jerome Louvel
 */
public class TagWriter extends HeaderWriter<Tag> {

    /**
     * Writes a list of tags.
     * 
     * @param tags
     *            The tags to write.
     * @return This writer.
     */
    public static String write(List<Tag> tags) {
        return new TagWriter().append(tags).toString();
    }

    /**
     * Writes a tag.
     * 
     * @param tag
     *            The tag to write.
     * @return This writer.
     */
    public static String write(Tag tag) {
        return tag.format();
    }

    @Override
    public HeaderWriter<Tag> append(Tag tag) {
        return append(write(tag));
    }

}
