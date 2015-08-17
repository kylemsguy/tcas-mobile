package com.kylemsguy.tcasmobile.backend;

import java.util.Collection;

/**
 * Created by kyle on 13/08/15.
 * An object that contains metadata about a folder
 */
public class MessageFolder implements MessageObject {
    private String key;
    private String formattedName;

    /**
     * Constructor for MessageFolder object
     *
     * @param key          Name of folder
     * @param formattedName formatted key of folder
     */
    public MessageFolder(String key, String formattedName) {
        this.key = key;
        this.formattedName = formattedName;
    }

    /**
     * Returns the user-readable key of the folder
     *
     * @return the user-readable key of the folder
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the formatted key of the folder
     *
     * @return the formatted key of the folder
     */
    public String getFormattedName() {
        return formattedName;
    }

    /**
     * Interface methods
     */
    public Type getType() {
        return Type.MESSAGE_FOLDER;
    }

    public static boolean isValid(Collection<MessageFolder> collection, MessageFolder folder) {
        if (folder == null) {
            return true; // is inbox
        } else {
            for (MessageFolder item : collection) {
                if (item.getKey().equals(folder.getKey())) {
                    return true;
                }
            }
            return false;
        }
    }
}
