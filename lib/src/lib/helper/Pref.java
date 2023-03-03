package lib.helper;

import java.util.prefs.Preferences;


public class Pref {

    private static Preferences getPref() {
        return Preferences.userRoot().node("TanziEngine");
    }

    public static Boolean isPrefSet(String key) {
        return getPref().get(key, null) != null;
    }

    public static String getString(String key, String defaultValue) {
        return getPref().get(key, defaultValue);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        return getPref().getBoolean(key, defaultValue);
    }

    public static void putBoolean(String key, Boolean value) {
        getPref().putBoolean(key, value);
    }

    public static void putString(String key, String value) {
        getPref().put(key, value);
    }

    public static int getInt(String key, int defaultValue) {
        return getPref().getInt(key, defaultValue);
    }

    public static void putInt(String key, int value) {
        getPref().putInt(key, value);
    }

}
