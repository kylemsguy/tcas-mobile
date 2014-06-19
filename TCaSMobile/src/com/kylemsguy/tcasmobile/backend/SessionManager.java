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
package com.kylemsguy.tcasparser;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SessionManager {
	// let's define some constants
	private final String USER_AGENT = "Mozilla/5.0";
	public static final String BASE_URL = "http://twocansandstring.com/";
	private final String LOGIN = BASE_URL + "login/";

	private List<String> cookies;
	private HttpURLConnection connection;

	public SessionManager() {
		// TODO Auto-generated constructor stub
	}

	public boolean checkLoggedIn() {
		try {
			String page = getPageContent(AnswerManager.QUESTION_URL);
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

		// GET form's data
		String page = getPageContent(LOGIN);
		String postParams = getFormParams(page, username, password);

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
		//connection.setRequestMethod("POST");
		connection.setRequestProperty("Host", "twocansandstring.com");
		connection
				.setRequestProperty("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		connection.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");

		connection.setRequestProperty("Connection", "keep-alive");
		//connection.setRequestProperty("Connection", "close");
		
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
	public String getPageContent(String url) throws Exception {
		// start the connection
		URL obj = new URL(url);
		connection = (HttpURLConnection) obj.openConnection();

		// default is GET
		connection.setRequestMethod("GET");

		connection.setUseCaches(false);

		// act like a browser
		// connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setRequestProperty("User-Agent", "");
		connection
				.setRequestProperty("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				connection
						.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = connection.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Get the response cookies
		this.setCookies(connection.getHeaderFields().get("Set-Cookie"));

		return response.toString();
	}

	/**
	 * Gets the parameters of a form on a page.
	 * 
	 * @param html
	 *            the HTMl of the page
	 * @param username
	 *            The username that should be used
	 * @param password
	 *            The password that should be used
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getFormParams(String html, String username, String password)
			throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		// form id
		Element loginform = doc.getElementsByTag("form").get(0);
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("login_username"))
				value = username;
			else if (key.equals("login_password"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

	public HttpURLConnection getConnection() {
		return connection;
	}

}
