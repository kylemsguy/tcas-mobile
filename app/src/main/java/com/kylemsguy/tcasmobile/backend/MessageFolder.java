package com.kylemsguy.tcasmobile.backend;

/**
 * Created by kyle on 13/08/15.
 */
public class MessageFolder {
    private String name;
    private String canonicalizedName;

    public MessageFolder(String name, String canonicalizedName) {
        this.name = name;
        this.canonicalizedName = canonicalizedName;
    }

    public String getName() {
        return name;
    }

    public String getCanonicalizedName() {
        return canonicalizedName;
    }
}
