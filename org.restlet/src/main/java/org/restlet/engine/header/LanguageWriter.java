/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.util.List;
import org.restlet.data.Language;

/**
 * Language header writer.
 *
 * @author Jerome Louvel
 */
public class LanguageWriter extends MetadataWriter<Language> {

    /**
     * Writes a list of languages.
     *
     * @param languages The languages to write.
     * @return This writer.
     */
    public static String write(List<Language> languages) {
        return new LanguageWriter().append(languages).toString();
    }
}
