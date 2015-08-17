package com.kylemsguy.tcasmobile.backend;

/**
 * Created by kyle on 16/08/15.
 * Implemented by all objects that hold Message data
 */
public interface MessageObject {
    Type getType();

    enum Type {
        MESSAGE_FOLDER,
        MESSAGE_THREAD,
        MESSAGE,
        HAS_NEXT_PAGE
    }
}
