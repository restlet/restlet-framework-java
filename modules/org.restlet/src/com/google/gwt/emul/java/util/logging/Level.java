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

package com.google.gwt.emul.java.util.logging;

/**
 * Emulate the Level class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 * 
 */
public class Level {

    public static final Level ALL = new Level("ALL", Integer.MIN_VALUE);

    public static final Level CONFIG = new Level("CONFIG", 700);

    public static final Level FINE = new Level("FINE", 500);

    public static final Level FINER = new Level("FINER", 400);

    public static final Level FINEST = new Level("FINEST", 300);

    public static final Level INFO = new Level("INFO", 800);

    public static final Level OFF = new Level("OFF", Integer.MAX_VALUE);

    public static final Level SEVERE = new Level("SEVERE", 1000);

    public static final Level WARNING = new Level("WARNING", 900);

    public static synchronized Level parse(String name) throws IllegalArgumentException {
	// Check the name
        if ("OFF".equals(name)) {
            return OFF;
        } else if("SEVERE".equals(name)) {
            return SEVERE;
        } else if("WARNING".equals(name)) {
            return WARNING;
        } else if("INFO".equals(name)) {
            return INFO;
        } else if("FINEST".equals(name)) {
            return FINEST;
        } else if("FINER".equals(name)) {
            return FINER;
        } else if("FINE".equals(name)) {
            return FINE;
        } else if("CONFIG".equals(name)) {
            return CONFIG;
        } else if("ALL".equals(name)) {
            return ALL;
        }

        // Could be an integer
        try {
            int x = Integer.parseInt(name);
            if (x == Integer.MAX_VALUE) {
                return OFF;
            } else if(x == 1000) {
                return SEVERE;
            } else if(x == 900) {
                return WARNING;
            } else if(x == 800) {
                return INFO;
            } else if(x == 300) {
                return FINEST;
            } else if(x == 400) {
                return FINER;
            } else if(x == 500) {
                return FINE;
            } else if(x == 700) {
                return CONFIG;
            } else if(x == Integer.MIN_VALUE) {
                return ALL;
            }
            return new Level(name, x);
        } catch (NumberFormatException ex) {
        }
        // OK, we've tried everything and failed
        throw new IllegalArgumentException("Bad level \"" + name + "\"");
    }

    private String name;

    private int value;

    public Level(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public boolean equals(Object obj) {
        try {
            Level level = (Level) obj;
            return level.value == this.value;
        } catch (Exception e) {
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public int hashCode() {
        return this.value;
    }

    public int intValue() {
	return value;
    }    
}
