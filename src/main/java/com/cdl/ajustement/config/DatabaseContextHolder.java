package com.cdl.ajustement.config;

public class DatabaseContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setDatabase(String database) {
        CONTEXT.set(database);
    }

    public static String getDatabase() {
        return CONTEXT.get() != null ? CONTEXT.get() : "CDL_NEW";
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
