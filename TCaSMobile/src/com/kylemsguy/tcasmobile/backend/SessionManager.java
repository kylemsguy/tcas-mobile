/*
	This is an API for Two Cans and String
	
    Copyright (C) 2014  Kyle Zhou <kylezhou2002@hotmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */
package com.kylemsguy.tcasmobile.backend;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.loopj.android.http.*;

public class SessionManager {
	// let's define some constants
	public static final String BASE_URL = "http://twocansandstring.com/";
	private final String LOGIN = BASE_URL + "login/";

	private static AsyncHttpClient client = new AsyncHttpClient();;

	public static boolean checkLoggedIn() {
		try {
			getPageContent(AnswerManager.QUESTION_URL, null,
					new AsyncHttpResponseHandler() {
						public void onSuccess(String response) {
							// Do something with the file
						}
					});
			if (page.startsWith("<!DOCTYPE html PUBLIC")) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Logs in to the site with the given username and password.
	 * 
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public void login(String username, String password) throws Exception {
		// make sure cookies are on
		CookieManager cm = new CookieManager();
		CookieHandler.setDefault(cm);

		// Send data to login
		String response = sendPost(LOGIN, postParams);

		// debug; prints success iff logged in
		if (checkLoggedIn()) {
			System.out.println("Login Successful");
		} else {
			System.out.println("Login Failed");
		}

	}

	/**
	 * Sends a POST request to the url with a String as the parameters. The
	 * parameters must have been pre-formatted beforehand.
	 * 
	 * @param url
	 *            The URL to send the POST request to
	 * @param postParams
	 *            The parameters to be sent. This will be sent as-is.
	 * @return
	 * @throws Exception
	 */
	public String sendPost(String url, String postParams) throws Exception {
		// start the connection
		URL obj = new URL(url);
		connection = (HttpURLConnection) obj.openConnection();

		// now time to act like a browser
		connection.setInstanceFollowRedirects(false);
		connection.setUseCaches(false);
		// connection.setRequestMethod("POST");
		connection.setRequestProperty("Host", "twocansandstring.com");
		connection
				.setRequestProperty("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		connection.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");

		connection.setRequestProperty("Connection", "keep-alive");
		// connection.setRequestProperty("Connection", "close");

		connection.setFixedLengthStreamingMode(postParams.getBytes().length);
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");

		// COOKIES
		for (String cookie : this.cookies) {
			connection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}

		// connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setRequestProperty("User-Agent", "");

		connection.setRequestProperty("Referrer",
				"http://twocansandstring.com/login/");

		connection.setDoOutput(true);
		connection.setDoInput(true);

		connection.connect();

		// Send post request
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		boolean redirect = false;

		// normally, 3xx is redirect
		int status = connection.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
		}

		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code ... " + status);

		/*
		 * if (redirect) {
		 * 
		 * // get redirect url from "location" header field String newUrl =
		 * connection.getHeaderField("Location");
		 * 
		 * // get the cookie if need, for login String cookies =
		 * connection.getHeaderField("Set-Cookie");
		 * 
		 * // open the new connnection again connection = (HttpURLConnection)
		 * new URL(newUrl).openConnection();
		 * connection.setRequestProperty("Cookie", cookies);
		 * connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
		 * // connection.addRequestProperty("User-Agent", "Mozilla");
		 * connection.addRequestProperty("User-Agent", "");
		 * connection.addRequestProperty("Referer", "google.com");
		 * 
		 * System.out.println("Redirect to URL : " + newUrl);
		 * 
		 * }
		 */

		BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	/**
	 * Gets the content of a page.
	 * 
	 * @param url
	 *            The page whose content should be retrieved
	 * @return
	 * @throws Exception
	 */
	public static void getPageContent(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		// start the connection
		client.get(url, params, responseHandler);
	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

}
