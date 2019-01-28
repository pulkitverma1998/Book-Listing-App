package com.example.android.findbooks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private static final String MAIN_URL = "https://www.googleapis.com/books/v1/volumes?q=search+terms";

    private QueryUtils() {

    }

    public static List<Book> fetchBookData(String textEntered) {
        // Create URL object
        URL url = createUrl(textEntered);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<Book> books = extractFeatureFromJson(jsonResponse);

        return books;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String textEntered) {
        URL url = null;
        String modifiedURL = textEntered.trim().replaceAll("\\s+", "+");
        try {
            url = new URL(MAIN_URL + modifiedURL);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;

    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Book> extractFeatureFromJson(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        List<Book> books = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            int count = baseJsonResponse.getInt("totalItems");
            if (count == 0) {
                return null;
            }

            JSONArray bookArray = baseJsonResponse.getJSONArray("items");

                for (int i = 0; i < bookArray.length(); i++) {

                    JSONObject currentEarthquake = bookArray.getJSONObject(i);

                    JSONObject volumeInfo = currentEarthquake.getJSONObject("volumeInfo");

                    String bookName = volumeInfo.getString("title");

                    String authorName = null;
                    if (volumeInfo.has("authors")) {
                        JSONArray authors = volumeInfo.getJSONArray("authors");
                        authorName = extractAllAuthors(authors);
                    }

                    Bitmap thumbnail = null;
                    if (volumeInfo.has("imageLinks")) {
                        JSONObject imageLink = volumeInfo.getJSONObject("imageLinks");
                        String thumbnailLink = imageLink.getString("thumbnail");
                        thumbnail = getBitmapFromURL(thumbnailLink);
                    }
                    Book book = new Book(bookName, authorName, thumbnail);

                    books.add(book);
                }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the books JSON results", e);
        }

        return books;
    }

    private static String extractAllAuthors(JSONArray authorsArray) throws JSONException {

        String authorsList = null;

        if (authorsArray.length() == 0)
            authorsList = "No Author Found";

        for (int i = 0; i < authorsArray.length(); i++) {
            if (i == 0)
                authorsList = authorsArray.getString(0);
            else
                authorsList = authorsList + ", " + authorsArray.getString(i);
        }

        return authorsList;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
