package core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppSettings {

    private static final Properties PROPS  = new Properties();
    private static boolean          loaded = false;

    static { load(); }

    private AppSettings() {}

    private static void load() {
        try (InputStream in = AppSettings.class
                .getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
                loaded = true;
            } else {
                System.err.println("[AppSettings] config.properties not found in classpath");
            }
        } catch (IOException e) {
            System.err.println("[AppSettings] Failed to load config: " + e.getMessage());
        }
    }

    public static String  get(String key)                      { return PROPS.getProperty(key); }
    public static String  get(String key, String defaultValue) { return PROPS.getProperty(key, defaultValue); }
    public static boolean isLoaded()                           { return loaded; }

    public static int getInt(String key, int defaultValue) {
        String val = PROPS.getProperty(key);
        if (val == null) return defaultValue;
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
