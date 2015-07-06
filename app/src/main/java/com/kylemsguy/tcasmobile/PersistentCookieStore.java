package com.kylemsguy.tcasmobile;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kyle on 06/07/15.
 *
 * Implementation of a CookieStore that stores data of a CookieStore every time it is modified
 */
public class PersistentCookieStore implements CookieStore {

    private Map<URI, HttpCookie> cookieJar;
    private CookieStore store;
    private Context context;

    public PersistentCookieStore(Context context) {
        // get the default in memory cookie store
        //cookieJar = new HashMap<>();
        store = new CookieManager().getCookieStore();
        this.context = context;

        // load cookies from SharedPreferences
        // we are not loading list of URIs because not necessary
        List<HttpCookie> cookies = PrefUtils.getHttpCookieListFromPrefs(context, PrefUtils.PREF_COOKIES_KEY);
        if (cookies != null)
            for (HttpCookie cookie : cookies) {
                //System.out.println(cookie.toString());
                store.add(null, cookie);
            }

        // add a shutdown hook to write out the in memory cookies
        // NOTE this should be done in the activity
        //Runtime.getRuntime().addShutdownHook(new Thread(this));

    }

    /**
     * Adds one HTTP cookie to the store. This is called for every incoming HTTP response.
     * A cookie to store may or may not be associated with an URI. If it is not associated with an URI,
     * the cookie's domain and path attribute will indicate where it comes from. If it is associated with an URI
     * and its domain and path attribute are not speicifed, given URI will indicate where this cookie comes from.
     * If a cookie corresponding to the given URI already exists, then it is replaced with the new one
     *
     * @param uri    the uri this cookie associated with. if null, this cookie will not be associated with an URI
     * @param cookie The cookie to store
     */
    @Override
    public void add(URI uri, HttpCookie cookie) {
        store.add(uri, cookie);
        //System.out.println(uri.toString() + " " + cookie.toString());
        writeCookiesToPrefs();
    }

    /**
     * Retrieve cookies associated with given URI, or whose domain matches the given URI.
     * Only cookies that have not expired are returned. This is called for every outgoing HTTP request.
     *
     * @param uri The URI
     * @return an immutable list of HttpCookie, return empty list if no cookies match the given URI
     */
    @Override
    public List<HttpCookie> get(URI uri) {
        return store.get(uri);
    }

    /**
     * Get all not-expired cookies in cookie store.
     *
     * @return an immutable list of http cookies; return empty list if there's no http cookie in store
     */
    @Override
    public List<HttpCookie> getCookies() {
        return store.getCookies();
    }

    /**
     * Get all URIs which identify the cookies in this cookie store.
     *
     * @return an immutable list of URIs; return empty list if no cookie in this cookie store is associated with an URI
     */
    @Override
    public List<URI> getURIs() {
        return store.getURIs();
    }

    /**
     * Remove cookie from store
     *
     * @param uri the uri this cookie associated with. if null, the cookie to be removed is not associated with an URI when added; if not null, the cookie to be removed is associated with the given URI when added.
     * @param cookie The cookie to remove
     * @return true if this store contained the specified cookie
     */
    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        boolean contained = store.remove(uri, cookie);
        // sync cookies back to SharedPreferences
        writeCookiesToPrefs();
        return contained;
    }

    /**
     * Remove all cookies in this cookie store.
     *
     * @return true if this store changed as a result of the call
     */
    @Override
    public boolean removeAll() {
        boolean changed = store.removeAll();
        // sync cookies back to SharedPreferences
        writeCookiesToPrefs();
        return changed;
    }

    private void writeCookiesToPrefs() {
        //List<URI> uris = store.getURIs();
        List<HttpCookie> cookies = store.getCookies();

        //PrefUtils.saveURIListToPrefs(context, PrefUtils.PREF_URLS_KEY, uris);
        PrefUtils.saveHttpCookieListToPrefs(context, PrefUtils.PREF_COOKIES_KEY, cookies);
    }
}
