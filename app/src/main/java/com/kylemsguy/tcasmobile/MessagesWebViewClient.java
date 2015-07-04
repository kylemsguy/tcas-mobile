package com.kylemsguy.tcasmobile;

import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MessagesWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO: implement the following
        // if going to something related to home, jump to homefragment
        // if going to something related to ask, jump to askfragment
        // if going to something related to answer, jump to answerfragment
        // If going to something related to messages or forum, just load in WebView
        // Else, load in Chrome.

        // TODO currently opens everything in WebView
        return false;
    }
}
