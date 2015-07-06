package com.kylemsguy.tcasmobile.apiwrapper;

/**
 * Base class for all requests.
 */
public abstract class AbstractRequest {
    
    /** URL to send a POST request to. */
    public abstract String getRequestUrl();
    
    /** Body for the HTTP request. */
    public abstract String getRequestBody();
}
