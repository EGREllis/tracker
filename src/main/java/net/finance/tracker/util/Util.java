package net.finance.tracker.util;

public class Util {
    public static void logException(Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace(System.err);
        System.err.flush();
    }
}
