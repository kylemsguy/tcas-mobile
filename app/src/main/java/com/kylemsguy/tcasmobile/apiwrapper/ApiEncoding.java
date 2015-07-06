package com.kylemsguy.tcasmobile.apiwrapper;

import java.io.UnsupportedEncodingException;

abstract class ApiEncoding {
	private final String encodingVersionPrefix;
	
	public ApiEncoding(String encodingVersionPrefix) {
		this.encodingVersionPrefix = encodingVersionPrefix;
	}
	
	public String getVersionPrefix() {
		return encodingVersionPrefix;
	}
	
	public abstract Object decode(String rawValue);
	public abstract String encode(Object value);
}
