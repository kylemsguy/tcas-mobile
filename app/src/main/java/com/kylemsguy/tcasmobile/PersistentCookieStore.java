package com.kylemsguy.tcasmobile;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kyle on 06/07/15.
 *
 * Implementation of a CookieStore that stores data of a CookieStore every time it is modified
 */
public class PersistentCookieStore implements CookieStore {

    private Map<URI, List<HttpCookie>> cookieJar;
    private Context context;

    public PersistentCookieStore(Context context) {
        // get the default in memory cookie store
        //store = new CookieManager().getCookieStore();
        this.context = context;

        // load cookies from SharedPreferences
        Map<URI, List<HttpCookie>> cookies = PrefUtils.getURICookieMapFromPrefs(context, PrefUtils.PREF_COOKIES_KEY);
        if (cookies == null)
            cookieJar = new HashMap<>();
        else
            cookieJar = cookies;

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
    public synchronized void add(URI uri, HttpCookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie == null");
        }
        //store.add(uri, cookie);
        uri = cookiesUri(uri);
        List<HttpCookie> cookies = cookieJar.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<>();
            cookieJar.put(uri, cookies);
        } else {
            cookies.remove(cookie);
        }
        cookies.add(cookie);
        //System.out.println(uri.toString() + " " + cookie.toString());
        writeCookiesToPrefs();
    }

    private URI cookiesUri(URI uri) {
        if (uri == null) {
            return null;
        }
        try {
            return new URI("http", uri.getHost(), null, null);
        } catch (URISyntaxException e) {
            return uri; // probably a URI with no host
        }
    }

    /**
     * Retrieve cookies associated with given URI, or whose domain matches the given URI.
     * Only cookies that have not expired are returned. This is called for every outgoing HTTP request.
     *
     * @param uri The URI
     * @return an immutable list of HttpCookie, return empty list if no cookies match the given URI
     */
    @Override
    public synchronized List<HttpCookie> get(URI uri) {
        //return store.get(uri);
        //return cookieJar.get(uri);
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        List<HttpCookie> result = new ArrayList<>();
        // get cookies associated with given URI. If none, returns an empty list
        List<HttpCookie> cookiesForUri = cookieJar.get(uri);
        if (cookiesForUri != null) {
            for (Iterator<HttpCookie> i = cookiesForUri.iterator(); i.hasNext(); ) {
                HttpCookie cookie = i.next();
                if (cookie.hasExpired()) {
                    i.remove(); // remove expired cookies
                } else {
                    result.add(cookie);
                }
            }
        }
        // get all cookies that domain matches the URI
        for (Map.Entry<URI, List<HttpCookie>> entry : cookieJar.entrySet()) {
            if (uri.equals(entry.getKey())) {
                continue; // skip the given URI; we've already handled it
            }
            List<HttpCookie> entryCookies = entry.getValue();
            for (Iterator<HttpCookie> i = entryCookies.iterator(); i.hasNext(); ) {
                HttpCookie cookie = i.next();
                if (!HttpCookie.domainMatches(cookie.getDomain(), uri.getHost())) {
                    continue;
                }
                if (cookie.hasExpired()) {
                    i.remove(); // remove expired cookies
                } else if (!result.contains(cookie)) {
                    result.add(cookie);
                }
            }
        }
        writeCookiesToPrefs();
        return Collections.unmodifiableList(result);
    }

    /**
     * Get all not-expired cookies in cookie store.
     *
     * @return an immutable list of http cookies; return empty list if there's no http cookie in store
     */
    @Override
    public synchronized List<HttpCookie> getCookies() {
        //return store.getCookies();
        List<HttpCookie> result = new ArrayList<>();
        for (List<HttpCookie> list : cookieJar.values()) {
            for (Iterator<HttpCookie> i = list.iterator(); i.hasNext(); ) {
                HttpCookie cookie = i.next();
                if (cookie.hasExpired()) {
                    i.remove(); // remove expired cookies
                } else if (!result.contains(cookie)) {
                    result.add(cookie);
                }
            }
        }
        writeCookiesToPrefs();
        return Collections.unmodifiableList(result);
    }

    /**
     * Get all URIs which identify the cookies in this cookie store.
     *
     * @return an immutable list of URIs; return empty list if no cookie in this cookie store is associated with an URI
     */
    @Override
    public synchronized List<URI> getURIs() {
        //return store.getURIs();
        List<URI> result = new ArrayList<>(cookieJar.keySet());
        result.remove(null); // sigh // lull
        return Collections.unmodifiableList(result);
    }

    /**
     * Remove cookie from store
     *
     * @param uri the uri this cookie associated with. if null, the cookie to be removed is not associated with an URI when added; if not null, the cookie to be removed is associated with the given URI when added.
     * @param cookie The cookie to remove
     * @return true if this store contained the specified cookie
     */
    @Override
    public synchronized boolean remove(URI uri, HttpCookie cookie) {
        //boolean contained = store.remove(uri, cookie);
        // sync cookies back to SharedPreferences
        if (cookie == null) {
            throw new NullPointerException("cookie == null");
        }
        List<HttpCookie> cookies = cookieJar.get(cookiesUri(uri));
        if (cookies != null) {
            writeCookiesToPrefs();
            return cookies.remove(cookie);
        } else {
            return false;
        }
        //writeCookiesToPrefs();
        //return contained;
    }

    /**
     * Remove all cookies in this cookie store.
     *
     * @return true if this store changed as a result of the call
     */
    @Override
    public synchronized boolean removeAll() {
        //boolean changed = store.removeAll();
        boolean changed = !cookieJar.isEmpty();
        cookieJar.clear();
        // sync cookies back to SharedPreferences
        writeCookiesToPrefs();
        return changed;
    }

    private void writeCookiesToPrefs() {
        //List<HttpCookie> cookies = store.getCookies();

        //PrefUtils.saveHttpCookieListToPrefs(context, PrefUtils.PREF_COOKIES_KEY, cookies);

        PrefUtils.saveURICookieMapToPrefs(context, PrefUtils.PREF_COOKIES_KEY, cookieJar);

    }
}
