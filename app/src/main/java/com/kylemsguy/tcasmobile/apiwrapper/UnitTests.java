package com.kylemsguy.tcasmobile.apiwrapper;

/**
 * Unit tests for API Wrappers.
 */
final class UnitTests {

	public static void main(String[] args) {
		testApiEncodingV0();
		testEncodingDetection();
		testLoginRequest();
		testLoginResponse();
		System.out.println("All tests pass!");
	}
	
	private static void testApiEncodingV0() {
		ApiEncoding v0Encoding = new ApiEncodingV0();
		
		roundTripEncodingTest(v0Encoding, 42);
		
		roundTripEncodingTest(v0Encoding,
			1, 2, 3, 3.14159, "kitty", true, false, null, 0);
		
		roundTripEncodingTest(v0Encoding,
			new Object[] { 1, 2, 3 },
			new Object[] { "one", "two", "three" },
			new Object[] { 1.0, 2.0, 3.0 },
			new Object[] { true, false, null }
		);
	}

	private static void testLoginRequest() {
		LoginRequest request = new LoginRequest()
			.setUsername("blake")
			.setPassword("12345");
		
		String actual = request.getRequestBody();
		String expected = Encoder.encode(new Object[] { "blake", "12345" });
		assertEquals(expected, actual, "LoginRequest");
		
		request = new LoginRequest();
		expected = Encoder.encode(new Object[] { null, null });
		actual = request.getRequestBody();
		assertEquals(expected, actual, "LoginRequest");
	}
	
	private static void testLoginResponse() {
		LoginResponse response = new LoginResponse(Encoder.encode(new Object[] { "OK", 42, "Blake", "blake" }));
		assertEquals(LoginResponse.LoginStatus.OK, response.getStatus(), "LoginResponse");
		assertEquals(true, response.isSuccess(), "LoginResponse");
		assertEquals(42, response.getUserId(), "LoginResponse");
		assertEquals("Blake", response.getUsernameFormatted(), "LoginResponse");
		assertEquals("blake", response.getUsernameCanonicalized(), "LoginResponse");
		
		response = new LoginResponse("Starbucks wifi portal! Current music: 96 Degrees in the Shade");
		assertEquals(LoginResponse.LoginStatus.UNRECOGNIZED_RESPONSE, response.getStatus(), "LoginResponse");
		assertEquals(false, response.isSuccess(), "LoginResponse");
		
		response = new LoginResponse(Encoder.encode(new Object[] { "ERR", "WORLD_IS_ENDING" }));
		assertEquals(LoginResponse.LoginStatus.UNKNOWN_ERROR, response.getStatus(), "LoginResponse");
		assertEquals(false, response.isSuccess(), "LoginResponse");
		
		response = new LoginResponse(Encoder.encode(new Object[] { "ERR", "BAD_LOGIN" }));
		assertEquals(LoginResponse.LoginStatus.BAD_LOGIN, response.getStatus(), "LoginResponse");
		assertEquals(false, response.isSuccess(), "LoginResponse");
		
		response = new LoginResponse(Encoder.encode(new Object[] { "OK", "LOL, J/K, NOT OK" }));
		assertEquals(LoginResponse.LoginStatus.UNRECOGNIZED_RESPONSE, response.getStatus(), "LoginResponse");
		assertEquals(false, response.isSuccess(), "LoginResponse");
	}
	
	private static void testEncodingDetection() {
		Object value = "kitties";
		String encoded1 = Encoder.encode(value);
		Object decoded = Decoder.decode(encoded1);
		String encoded2 = Encoder.encode(decoded);
		if (!encoded2.equals(encoded1)) {
			throw new IllegalStateException("Encoding detection failed.");
		}
		
		// TODO: test old encoders against general Decoder when new encodings are added.
	}
	
	private static String roundTripEncodingTest(ApiEncoding encoding, Object... values) {
		String encoded = values.length == 1
			? encoding.encode(values[0])
			: encoding.encode(values);
		Object decoded = encoding.decode(encoded);
		String encodedAgain = encoding.encode(decoded);
		if (encoded.equals(encodedAgain)) {
			return encoded;
		}
		throw new IllegalStateException("TEST FAILED");
	}
	
	private static void assertEquals(Object expected, Object actual, String message) {
		if (expected == null && actual == null) return;
		boolean error = expected == null || actual == null;
		if (!error) {
			error = !expected.equals(actual);
		}
		
		if (error) throw new IllegalStateException(message + "\nExpected: " + expected + "\nActual: " + actual);
	}
}