/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.CacheDirective;

import java.util.List;

/**
 * Cache directive header writer.
 * 
 * @author Thierry Boileau
 */
public class CacheDirectiveWriter extends HeaderWriter<CacheDirective> {

	/**
	 * Writes a list of cache directives with a comma separator.
	 * 
	 * @param directives The list of cache directives.
	 * @return The formatted list of cache directives.
	 */
	public static String write(List<CacheDirective> directives) {
		return new CacheDirectiveWriter().append(directives).toString();
	}

	@Override
	public CacheDirectiveWriter append(CacheDirective directive) {
		if (hasFieldNames(directive)) {
			// RFC 9111 section 5.2.2.4 and 5.2.2.7: the field names are a
			// quoted-string, even when there is a single name that would be a
			// valid token.
			append(directive.getName());
			append("=");
			appendQuotedString(directive.getValue());
		} else {
			appendExtension(directive);
		}

		return this;
	}

	/**
	 * Indicates if the directive is a "no-cache" or "private" directive qualified
	 * by field names.
	 * 
	 * @param directive The directive to test.
	 * @return True if the directive has a non-empty list of field names.
	 */
	private static boolean hasFieldNames(CacheDirective directive) {
		return (directive != null) && (directive.getValue() != null) && !directive.getValue().isEmpty()
				&& (HeaderConstants.CACHE_NO_CACHE.equalsIgnoreCase(directive.getName())
						|| HeaderConstants.CACHE_PRIVATE.equalsIgnoreCase(directive.getName()));
	}

}
