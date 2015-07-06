package com.kylemsguy.tcasmobile.apiwrapper;

public final class LoginRequest extends AbstractRequest {
	
	private String username = null;
	private String password = null;
	
	public LoginRequest setUsername(String username) {
		this.username = username;
		return this;
	}
	
	public LoginRequest setPassword(String password) {
		this.password = password;
		return this;
	}
	
	@Override
	public String getRequestUrl() {
		return "http://www.twocansandstring.com/mobileapi/login";
	}
	
	@Override
	public String getRequestBody() {
		return Encoder.encode(new Object[] { username, password });
	}
}