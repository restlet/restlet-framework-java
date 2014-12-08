/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine;

/**
 * Enumeration of Restlet editions.
 * 
 * @author Jerome Louvel
 */
public enum Edition {

    /**
     * Android mobile OS, Google App Engine, Google Web Toolkit, JEE, JSE, OSGI.
     */
    ANDROID, GAE, GWT, JEE, JSE, OSGI;

    /** The current engine edition. */
    public static final Edition CURRENT = Edition.JSE;

    /**
     * Returns the full size name of the edition.
     * 
     * @return The full size of the edition.
     */
    public String getFullName() {
        switch (this) {
        case ANDROID:
            return "Android";
        case GAE:
            return "Google App Engine";
        case GWT:
            return "Google Web Toolkit";
        case JEE:
            return "Java Enterprise Edition";
        case JSE:
            return "Java Standard Edition";
        case OSGI:
            return "OSGi";
        }

        return null;
    }

    /**
     * Returns the medium size name of the edition.
     * 
     * @return The medium size name of the edition.
     */
    public String getMediumName() {
        switch (this) {
        case ANDROID:
            return "Android";
        case GAE:
            return "GAE";
        case GWT:
            return "GWT";
        case JEE:
            return "Java EE";
        case JSE:
            return "Java SE";
        case OSGI:
            return "OSGi";
        }

        return null;
    }

    /**
     * Returns the short size name of the edition.
     * 
     * @return The short size name of the edition.
     */
    public String getShortName() {
        switch (this) {
        case ANDROID:
            return "Android";
        case GAE:
            return "GAE";
        case GWT:
            return "GWT";
        case JEE:
            return "JEE";
        case JSE:
            return "JSE";
        case OSGI:
            return "OSGi";
        }

        return null;
    }
}
