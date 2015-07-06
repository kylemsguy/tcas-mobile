package com.kylemsguy.tcasmobile.apiwrapper;

/**
 * Buffer rest of code against encoder changes.
 */
final class Encoder {
	private static final ApiEncoding V0_ENCODING = new ApiEncodingV0();
	
	public static String encode(Object value) {
		return V0_ENCODING.encode(value);
	}
}