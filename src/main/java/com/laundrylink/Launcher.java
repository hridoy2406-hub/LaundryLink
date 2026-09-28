package com.laundrylink;

/**
 * Helper class so the app runs from IntelliJ without extra VM options.
 * (A main class that extends Application can't be started directly
 * when JavaFX is on the classpath, so we start it from here.)
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}