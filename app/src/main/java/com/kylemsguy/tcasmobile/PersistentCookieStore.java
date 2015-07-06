package com.kylemsguy.tcasmobile;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kyle on 06/07/15.
 */
public class PersistentCookieStore implements CookieStore {

    private Map<URI, List<HttpCookie>> cookies;

    public PersistentCookieStore() {
        // TODO load cookies from SharedPreferences.
        Map<URI, List<HttpCookie>> prefCookies = null;
        if (prefCookies == null)
            cookies = new TreeMap<>();
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
        if (cookie == null)
            throw new NullPointerException();
        // TODO commit cookie store to SharedPreferences
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
        // TODO list must be immutable
        if (uri == null)
            throw new NullPointerException();
        return null;
    }

    /**
     * Get all not-expired cookies in cookie store.
     * @return an immutable list of http cookies; return empty list if there's no http cookie in store
     */
    @Override
    public List<HttpCookie> getCookies() {
        return null;
    }

    /**
     * Get all URIs which identify the cookies in this cookie store.
     * @return an immutable list of URIs; return empty list if no cookie in this cookie store is associated with an URI
     */
    @Override
    public List<URI> getURIs() {
        return null;
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
        // return true; if successfully stored
        if (cookie == null)
            throw new NullPointerException();
        return false;
    }

    /**
     * Remove all cookies in this cookie store.
     *
     * @return true if this store changed as a result of the call
     */
    @Override
    public boolean removeAll() {
        // return true; if successfully stored
        return false;
    }
}
