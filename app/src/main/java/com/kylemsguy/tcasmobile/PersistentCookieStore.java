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

    @Override
    public void add(URI uri, HttpCookie cookie) {
        if (cookie == null)
            throw new NullPointerException();
        // TODO commit cookie store to SharedPreferences
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        // TODO list must be immutable
        if (uri == null)
            throw new NullPointerException();
        return null;
    }

    @Override
    public List<HttpCookie> getCookies() {
        return null;
    }

    @Override
    public List<URI> getURIs() {
        return null;
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        // return true; if successfully stored
        if (cookie == null)
            throw new NullPointerException();
        return false;
    }

    @Override
    public boolean removeAll() {
        // return true; if successfully stored
        return false;
    }
}
