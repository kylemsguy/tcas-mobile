package com.kylemsguy.tcasmobile.backend;

/**
 * Created by kyle on 13/08/15.
 * An object that contains metadata about a folder
 */
public class MessageFolder {
    private String name;
    private String canonicalizedName;

    /**
     * Constructor for MessageFolder object
     *
     * @param name              Name of folder
     * @param canonicalizedName canonicalized name of folder
     */
    public MessageFolder(String name, String canonicalizedName) {
        this.name = name;
        this.canonicalizedName = canonicalizedName;
    }

    /**
     * Returns the user-readable name of the folder
     *
     * @return the user-readable name of the folder
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the canonicalized name of the folder
     *
     * @return the canonicalized name of the folder
     */
    public String getCanonicalizedName() {
        return canonicalizedName;
    }
}
