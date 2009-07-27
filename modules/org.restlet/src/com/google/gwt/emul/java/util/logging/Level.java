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
    
}
