package com.kylemsguy.tcasmobile;

/**
 * Created by kyle on 22/05/15.
 */
public interface AsyncTaskCallback<E> {
    void taskComplete(E... results);
}
