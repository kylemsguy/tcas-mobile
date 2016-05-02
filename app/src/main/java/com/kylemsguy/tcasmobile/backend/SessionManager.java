package com.kylemsguy.tcasmobile.backend;

import com.kylemsguy.tcasmobile.apiwrapper.LoginRequest;
import com.kylemsguy.tcasmobile.apiwrapper.LoginResponse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SessionManager {
    // let's define some constants
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; TCaSMobile/1.x)"; // TODO perhaps include sysinfo?
    public static final String BASE_URL = "http://twocansandstring.com/";
    public static final boolean BACKEND_DEBUG = false;
    private static final String LOGIN = BASE_URL + "login/";

    // Multipart form stuff
    private static final String CRLF = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private static final String BOUNDARY = "*****";

    private static final boolean MANUAL_COOKIE = false;

    private List<String> cookies;
    private HttpURLConnection connection;
    private CookieManager cookieManager;

    public SessionManager() {
        cookieManager = new CookieManager();
        // Making sure cookies are enabled
        CookieHandler.setDefault(cookieManager);

    }

    public SessionManager(CookieStore cookieJar) {
        cookieManager = new CookieManager(cookieJar, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        // Making sure cookies are enabled
        CookieHandler.setDefault(cookieManager);

    }

    public boolean checkLoggedIn() {
        if (BACKEND_DEBUG)
            System.out.println("chkLoggedIn: Checking if logged in.");
        try {
            String page = getPageContent(AnswerManager.QUESTION_URL);
            if (BACKEND_DEBUG) {
                System.out.println("chkLoggedIn: " + cookieManager.getCookieStore().get(new URI(AnswerManager.QUESTION_URL)));
                System.out.println("chkLoggedIn: Page: " + page);
            }
            return !page.startsWith("<!DOCTYPE html PUBLIC");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Logs in to the site with the given username and password.
     *
     * @param username Username of user
     * @param password Password of user
     * @throws Exception
     */
    public void login(String username, String password) throws Exception {
        // GET form's data
        String page = getPageContent(LOGIN);
        String postParams = getFormParams(page, username, password);

        // Send data to login
        String response = sendPost(LOGIN, postParams);

    }


    /**
     * Uses the TwoCans Mobile API to make a Login request.
     * <p/>
     * Currently only used to get user info because Login is the only endpoint in the API.s
     *
     * @param username user's username
     * @param password user's password
     */
    public LoginResponse mobileLogin(String username, String password) throws Exception {
        LoginRequest requestBuilder = new LoginRequest()
                .setUsername(username)
                .setPassword(password);

        String requestBody = requestBuilder.getRequestBody();
        String requestUrl = requestBuilder.getRequestUrl();

        String rawResponse = sendPost(requestUrl, requestBody);
        LoginResponse response = new LoginResponse(rawResponse);
        if (BACKEND_DEBUG) {
            System.out.println("Request: " + requestBody);
            System.out.println("RawResponse: " + rawResponse);
            System.out.println("Status: " + response.getStatus());
            System.out.println("User ID: " + response.getUserId());
            System.out.println("CanonicalizedUsername: " + response.getUsernameCanonicalized());
            System.out.println("FormattedUsername: " + response.getUsernameFormatted());
        }

        return response;

    }

    /**
     * Logs out of Two Cans and String
     */
    public void logout() {
        try {
            getPageContent(BASE_URL + "logout/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a POST request to the url with a String as the parameters. The
     * parameters must have been pre-formatted beforehand.
     *
     * @param url        The URL to send the POST request to
     * @param postParams The parameters to be sent. This will be sent as-is.
     * @return The response of the request as a string.
     * @throws Exception
     */
    public String sendPost(String url, String postParams) throws Exception {
        // start the connection
        URL obj = new URL(url);
        connection = (HttpURLConnection) obj.openConnection();

        // now time to act like a browser
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection
                .setRequestProperty("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setFixedLengthStreamingMode(postParams.getBytes().length);
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        // COOKIES
        if (cookies != null && MANUAL_COOKIE) {
            if (BACKEND_DEBUG)
                System.out.println("sm.sendPost: Adding cookies:");
            for (String cookie : this.cookies) {
                if (BACKEND_DEBUG)
                    System.out.println(cookie);
                connection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
            if (BACKEND_DEBUG)
                System.out.println("sm.getPageContent: Done adding cookies.");
        }
        String hostname = new URI(url).getHost();
        connection.setRequestProperty("Host", hostname);
        //System.out.println(hostname);

        connection.setRequestProperty("User-Agent", USER_AGENT);

        connection.setDoOutput(true);
        connection.setDoInput(true);

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

        if (BACKEND_DEBUG) {
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + postParams);
            System.out.println("Response Code ... " + status);
        }

        if (redirect) {

            // get redirect url from "location" header field
            String newUrl = connection.getHeaderField("Location");

            // get the cookie if need, for login
            String cookies = connection.getHeaderField("Set-Cookie");

            // open the new connnection again
            connection = (HttpURLConnection) new URL(newUrl).openConnection();
            connection.setRequestProperty("Cookie", cookies);
            connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            connection.addRequestProperty("User-Agent", "Mozilla");
            // because the old url is the referer
            connection.addRequestProperty("Referer", url);

            if (BACKEND_DEBUG)
                System.out.println("Redirect to URL : " + newUrl);

        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String responseString = response.toString();


        connection.disconnect();

        return responseString;
    }

    public String sendMultipartPost(String url, List<MultipartRequestParam> params) throws IOException {
        URL obj = new URL("http://example.com/server.cgi");
        connection = (HttpURLConnection) obj.openConnection();

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

        DataOutputStream request = new DataOutputStream(connection.getOutputStream());

        for (MultipartRequestParam param : params) {
            String paramName = param.getParamName();
            String fileName = param.getFileName();
            byte[] data = param.getData();

            // build parameter from MultipartRequestParam
            StringBuilder sb = new StringBuilder();
            sb.append("Content-Disposition: form-data; name=\"");
            sb.append(paramName);
            if (fileName != null) {
                sb.append("\";filename=\"");
                sb.append(fileName);
            }
            sb.append("\"");
            sb.append(CRLF);

            request.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
            request.writeBytes(sb.toString());
            request.writeBytes(CRLF);
            request.write(data);
            request.writeBytes(CRLF);
            request.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
        }

        request.flush();
        request.close();

        // get the response
        InputStream responseStream = new BufferedInputStream(connection.getInputStream());

        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();

        String response = stringBuilder.toString();

        // cleanup
        responseStream.close();
        connection.disconnect();
        return response;
    }

    /**
     * Gets the content of a page.
     *
     * @param url The page whose content should be retrieved
     * @return Page contents as a string
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
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection
                .setRequestProperty("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        String hostname = new URI(url).getHost();
        connection.setRequestProperty("Host", hostname);
        if (cookies != null && MANUAL_COOKIE) {
            if (BACKEND_DEBUG)
                System.out.println("sm.getPageContent: Adding cookies:");
            for (String cookie : this.cookies) {
                if (BACKEND_DEBUG)
                    System.out.println(cookie);
                connection
                        .addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
            if (BACKEND_DEBUG)
                System.out.println("sm.getPageContent: Done adding cookies.");
        }
        int responseCode = connection.getResponseCode();
        if (BACKEND_DEBUG) {
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Get the response cookies
        if (MANUAL_COOKIE)
            this.setCookies(connection.getHeaderFields().get("Set-Cookie"));

        String pageContents = response.toString();

        connection.disconnect();

        return pageContents;
    }

    /**
     * Gets the parameters of a form on a page.
     *
     * @param html     the HTMl of the page
     * @param username The username that should be used
     * @param password The password that should be used
     * @return A filled out parameter list that can be passed into an HTTP request
     * @throws UnsupportedEncodingException
     */
    public String getFormParams(String html, String username, String password)
            throws UnsupportedEncodingException {

        if (BACKEND_DEBUG)
            System.out.println("Extracting form's data...");

        Document doc = Jsoup.parse(html);

        // form id
        Element loginform = doc.getElementsByTag("form").get(0);
        Elements inputElements = loginform.getElementsByTag("input");
        List<String> paramList = new ArrayList<>();
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
                result.append("&");
                result.append(param);
            }
        }
        return result.toString();
    }

    public String getDate(String html) {
        if (BACKEND_DEBUG)
            System.out.println("Getting date...");

        Document doc = Jsoup.parse(html);
        Elements spans = doc.getElementsByTag("span");

        Pattern datePattern = Pattern.compile("");
        // TODO implement date conversion/get from page
        return null;
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    public CookieStore getCookieStore() {
        return cookieManager.getCookieStore();
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    public static class GetRequestBuilder {
        private Map<String, String> params;

        public GetRequestBuilder() {

        }

        public void addParam(String name, String content) {
            params.put(name, content);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String key : params.keySet()) {
                sb.append(key);
                sb.append("=");
                try {
                    sb.append(URLEncoder.encode(params.get(key), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    // WHY IS UNICODE NOT SUPPORTED?! WHAT HAPPENED?!
                    return null;
                }
                sb.append("&");
            }

            if (sb.length() > 0)
                sb.setLength(sb.length() - 1);

            return sb.toString();

        }

    }

    public static class MultipartRequestParam {
        private String paramName;
        private String fileName;
        private byte[] data;

        private MultipartRequestParam(String paramName, String fileName, byte[] data) {
            this.paramName = paramName;
            this.fileName = fileName;
            this.data = data;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public static class Builder {
            private String paramName;
            private String fileName;
            private byte[] data;

            public Builder() {

            }

            public Builder setParamName(String paramName) {
                this.paramName = paramName;
                return this;
            }

            public Builder setFileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public Builder setData(byte[] data) {
                this.data = data;
                return this;
            }

            public MultipartRequestParam build() throws IllegalStateException {
                if (paramName == null || data == null)
                    throw new IllegalStateException("Parameters not set correctly");

                return new MultipartRequestParam(paramName, fileName, data);
            }
        }
    }

}
