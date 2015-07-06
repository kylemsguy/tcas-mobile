package com.kylemsguy.tcasmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.reflect.Type;
import java.net.CookieStore;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.cookie.Cookie;

/**
 * Code taken from
 * https://stackoverflow.com/questions/19629625/best-place-for-storing-user-login-credentials-in-android
 * Extended by Kylemsguy 2015-07-04
 */

public class PrefUtils {
    public static final String PREF_LOGIN_USERNAME_KEY = "__USERNAME__";
    public static final String PREF_LOGIN_PASSWORD_KEY = "__PASSWORD__";
    public static final String PREF_LOGGED_IN_KEY = "__LOGGEDIN__";
    public static final String PREF_COOKIES_KEY = "__COOKIES__";
    public static final String PREF_COOKIESTORE_KEY = "__COOKIES__";

    private PrefUtils() {
        // Prevent anyone from instantiating this class...
    }

    /**
     * Called to save supplied String in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param value   Value to save
     */
    public static void saveToPrefs(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Called to save supplied boolean in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param value   Value to save
     */

    public static void saveToPrefs(Context context, String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Called to serialize and save supplied Object in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param cookies Value to save
     */
    public static void saveCookieStoreToPrefs(Context context, String key, CookieStore cookies) {
        // serialize cookies
        Gson gson = new Gson();
        Type cookieStore = new TypeToken<CookieStore>() {
        }.getType();
        String serializedCookies = gson.toJson(cookies, cookieStore);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, serializedCookies);
        editor.apply();
    }

    /**
     * Called to save supplied List&lt;String&gt; in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param values  List of values to save
     */

    public static void saveListToPrefs(Context context, String key, List<String> values) {
        // First serialize to string
        Gson gson = new Gson();
        Type listOfString = new TypeToken<List<String>>() {
        }.getType();
        String serializedValues = gson.toJson(values, listOfString);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, serializedValues);
        editor.apply();
    }

    /**
     * Called to retrieve required value from shared preferences, identified by given key.
     * Default value will be returned of no value found or error occurred.
     *
     * @param context      Context of caller activity
     * @param key          Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static String getFromPrefs(Context context, String key, String defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Called to retrieve required value from shared preferences, identified by given key.
     * Default value will be returned of no value found or error occurred.
     *
     * @param context      Context of caller activity
     * @param key          Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static boolean getFromPrefs(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getBoolean(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Called to retrieve required CookieStore from shared preferences, identified by given key.
     * null will be returned of no value found or error occurred.
     *
     * @param context Context of caller activity
     * @param key     Key to find value against
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static CookieStore getCookieStoreFromPrefs(Context context, String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String serializedCookies = sharedPrefs.getString(key, null);
            Gson gson = new Gson();
            Type cookieStore = new TypeToken<CookieStore>() {
            }.getType();
            return gson.fromJson(serializedCookies, cookieStore);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Called to retrieve required List&lt;String&gt; from shared preferences, identified by given key.
     * null will be returned of no value found or error occurred.
     *
     * @param context Context of caller activity
     * @param key     Key to find value against
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static List<String> getListFromPrefs(Context context, String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String serializedList = sharedPrefs.getString(key, null);
            Gson gson = new Gson();
            Type listOfString = new TypeToken<List<String>>() {
            }.getType();
            return gson.fromJson(serializedList, listOfString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
