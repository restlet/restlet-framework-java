/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

import java.io.IOException;

/**
 * Represents an appendable sequence of characters.
 *
 * @author Jerome Louvel
 */
public class AppendableRepresentation extends StringRepresentation implements Appendable {

	/** The appendable text. */
	private volatile StringBuilder appendableText;

	/**
	 * Constructor. The following metadata are used by default: "text/plain" media
	 * type, no language and the ISO-8859-1 character set.
	 */
	public AppendableRepresentation() {
		this(null);
	}

	/**
	 * Constructor. The following metadata are used by default: "text/plain" media
	 * type, no language and the ISO-8859-1 character set.
	 *
	 * @param text The string value.
	 */
	public AppendableRepresentation(CharSequence text) {
		super(text);
	}

	/**
	 * Constructor. The following metadata are used by default: "text/plain" media
	 * type, no language and the ISO-8859-1 character set.
	 *
	 * @param text     The string value.
	 * @param language The language.
	 */
	public AppendableRepresentation(CharSequence text, Language language) {
		super(text, language);
	}

	/**
	 * Constructor. The following metadata are used by default: no language and the
	 * ISO-8859-1 character set.
	 *
	 * @param text      The string value.
	 * @param mediaType The media type.
	 */
	public AppendableRepresentation(CharSequence text, MediaType mediaType) {
		super(text, mediaType);
	}

	/**
	 * Constructor. The following metadata are used by default: ISO-8859-1 character
	 * set.
	 *
	 * @param text      The string value.
	 * @param mediaType The media type.
	 * @param language  The language.
	 */
	public AppendableRepresentation(CharSequence text, MediaType mediaType, Language language) {
		super(text, mediaType, language);
	}

	/**
	 * Constructor.
	 *
	 * @param text         The string value.
	 * @param mediaType    The media type.
	 * @param language     The language.
	 * @param characterSet The character set.
	 */
	public AppendableRepresentation(CharSequence text, MediaType mediaType, Language language,
			CharacterSet characterSet) {
		super(text, mediaType, language, characterSet);
	}

	@Override
	public Appendable append(char c) throws IOException {
		if (this.appendableText == null) {
			this.appendableText = new StringBuilder().append(c);
		} else {
			this.appendableText.append(c);
		}

		return this;
	}

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		if (this.appendableText == null) {
			this.appendableText = new StringBuilder(csq);
		} else {
			this.appendableText.append(csq);
		}

		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		if (this.appendableText == null) {
			this.appendableText = new StringBuilder();
		}

		this.appendableText.append(csq, start, end);

		return this;
	}

	@Override
	public String getText() {
		return (this.appendableText == null) ? null : this.appendableText.toString();
	}

	@Override
	public void setText(CharSequence text) {
		if (text != null) {
			if (this.appendableText == null) {
				this.appendableText = new StringBuilder(text);
			} else {
				this.appendableText.delete(0, this.appendableText.length());
				this.appendableText.append(text);
			}
		} else {
			this.appendableText = null;
		}
	}
}
