package com.kylemsguy.tcasmobile.apiwrapper;

import java.util.HashMap;

/** Decodes an arbitrary server response with encoding detection. */
final class Decoder {
    private static final ApiEncoding V0_ENCODING = new ApiEncodingV0();
    
    private static final HashMap<String, ApiEncoding> ENCODING_BY_PREFIX;
    
    static {
        HashMap<String, ApiEncoding> lookup = new HashMap<>();
        lookup.put(V0_ENCODING.getVersionPrefix(), V0_ENCODING);
        ENCODING_BY_PREFIX = lookup;
    }
    
    public static Object decode(String rawValue) {
        int pipeLocation = rawValue.indexOf("|");
        String prefix = rawValue.substring(0, pipeLocation + 1);
        ApiEncoding encoding = ENCODING_BY_PREFIX.get(prefix);
        if (encoding == null) {
            throw new IllegalStateException("Unrecognized encoding format.");
        }
        
        return encoding.decode(rawValue);
    }
}
