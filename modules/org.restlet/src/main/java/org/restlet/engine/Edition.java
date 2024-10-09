/**
 * Copyright 2005-2024 Qlik
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
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine;

/**
 * Enumeration of Restlet editions.
 *
 * @author Jerome Louvel
 */
public enum Edition {

	/**
	 * Android mobile OS, JEE, JSE, OSGI.
	 */
	ANDROID("Android", "Android", "Android"),
	JEE("Java Enterprise Edition", "Java EE", "JEE"),
	JSE("Java Standard Edition", "Java SE", "JSE"),
	OSGI("OSGi", "OSGi", "OSGi");

	/** The current engine edition. */
	public static Edition CURRENT = Edition.JSE;

	private final String fullName;
	private final String mediumName;
	private final String shortName;

	Edition(final String fullName, final String mediumName, final String shortName) {
		this.fullName = fullName;
		this.mediumName = mediumName;
		this.shortName = shortName;
	}

	/**
	 * Returns the full size name of the edition.
	 *
	 * @return The full size of the edition.
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Returns the medium size name of the edition.
	 *
	 * @return The medium size name of the edition.
	 */
	public String getMediumName() {
		return mediumName;
	}

	/**
	 * Returns the short size name of the edition.
	 *
	 * @return The short size name of the edition.
	 */
	public String getShortName() {
		return shortName;
	}
	
	/**
	 * Returns true if this edition is the current one.
	 *
	 * @return True if this edition is the current one.
	 */
	public boolean isCurrentEdition() {
		return this == CURRENT;
	}
	
	/**
	 * Returns true if this edition is not the current one.
	 *
	 * @return True if this edition is not the current one.
	 */
	public boolean isNotCurrentEdition() {
		return this != CURRENT;
	}

	public static boolean isCurrentEditionOneOf(Edition... editions) {
		boolean result = false;

		if (editions != null) {
			for (int i = 0; i < editions.length && !result; i++) {
				result = editions[i].isCurrentEdition();
			}
		}

		return result;
	}

	/**
	 * Set this edition as the current one.
	 */
	public void setCurrentEdition() {
		CURRENT = this;
	}


}
