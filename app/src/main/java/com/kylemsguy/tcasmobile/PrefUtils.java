package com.kylemsguy.tcasmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.lang.reflect.Type;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.cookie.Cookie;
import org.apache.http.protocol.HTTP;

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
     * Called to save supplied List&lt;String&gt; in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param values  List of values to save
     */
    public static void saveStringListToPrefs(Context context, String key, List<String> values) {
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
     * Called to save supplied List&lt;URI&gt; in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param values  List of values to save
     */
    public static void saveURIListToPrefs(Context context, String key, List<URI> values) {
        // First serialize to string
        Gson gson = new Gson();
        Type t = new TypeToken<List<URI>>() {
        }.getType();
        String serializedValues = gson.toJson(values, t);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, serializedValues);
        editor.apply();
    }

    /**
     * Called to save supplied List&lt;HttpCookie&gt; in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param values  List of values to save
     */
    public static void saveHttpCookieListToPrefs(Context context, String key, List<HttpCookie> values) {
        // First serialize to string
        Gson gson = new Gson();
        Type t = new TypeToken<List<HttpCookie>>() {
        }.getType();
        String serializedValues = gson.toJson(values, t);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, serializedValues);
        editor.apply();
    }

    public static void saveURICookieMapToPrefs(Context context, String key, Map<URI, List<HttpCookie>> cookieJar) {
        // First serialize to string
        Gson gson = new Gson();
        Type t = new TypeToken<Map<URI, List<HttpCookie>>>() {
        }.getType();
        String serializedValues = gson.toJson(cookieJar, t);

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
     * Called to retrieve required List&lt;String&gt; from shared preferences, identified by given key.
     * null will be returned of no value found or error occurred.
     *
     * @param context Context of caller activity
     * @param key     Key to find value against
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static List<String> getStringListFromPrefs(Context context, String key) {
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

    /**
     * Called to retrieve required List&lt;URI&gt; from shared preferences, identified by given key.
     * null will be returned of no value found or error occurred.
     *
     * @param context Context of caller activity
     * @param key     Key to find value against
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static List<URI> getUriListFromPrefs(Context context, String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String serializedList = sharedPrefs.getString(key, null);
            Gson gson = new Gson();
            Type listOfString = new TypeToken<List<Uri>>() {
            }.getType();
            return gson.fromJson(serializedList, listOfString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Called to retrieve required List&lt;HttpCookie&gt; from shared preferences, identified by given key.
     * null will be returned of no value found or error occurred.
     *
     * @param context Context of caller activity
     * @param key     Key to find value against
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static List<HttpCookie> getHttpCookieListFromPrefs(Context context, String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String serializedList = sharedPrefs.getString(key, null);
            Gson gson = new Gson();
            Type listOfString = new TypeToken<List<HttpCookie>>() {
            }.getType();
            return gson.fromJson(serializedList, listOfString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<URI, List<HttpCookie>> getURICookieMapFromPrefs(Context context, String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String serializedList = sharedPrefs.getString(key, null);
            Gson gson = new Gson();
            Type listOfString = new TypeToken<Map<URI, List<HttpCookie>>>() {
            }.getType();
            return gson.fromJson(serializedList, listOfString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

