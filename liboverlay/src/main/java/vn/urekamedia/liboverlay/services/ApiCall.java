package vn.urekamedia.liboverlay.services;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiCall {
        private static int GET = 1;
        private static int POST = 2;
        public static int responseCode = 0;
        private Exception exception;
        private Context applicationContext = null;

        private static final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");


        public static JSONObject getJsonFromUrl(String url){
            return getJson(url,GET,null);
        }

        public static JSONObject postJsonFromUrl(String url,String jsonString){
            return getJson(url,POST,jsonString);
        }

        private static JSONObject getJson(String url,int method,String jsonStringParams){
            JSONObject json = null;
            // try parse the string to a JSON object
            try {
                String jsonString = makeServiceCall(url, method, jsonStringParams);

                if (jsonString != null) {
                    json = new JSONObject(jsonString);
                }
                return json;
            } catch (Exception e) {
                Log.e("ApiCall", "Error parsing data " + e.toString());
                return json;
            }
        }

        private static String makeServiceCall(String url,int method, String jsonStringParams) throws IOException {
            OkHttpClient client = new OkHttpClient();

            Request request;
            if (method == POST){
                RequestBody body = RequestBody.create(JSON, jsonStringParams);
                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
            }else{
                request = new Request.Builder()
                        .url(url)
                        .build();
            }
            responseCode = 0;
            Response responses = client.newCall(request).execute();
            Log.i("Reponse Return",String.valueOf(responses.code()));
            if ((responseCode = responses.code()) == 200) {
                // Get response
                String jsonData = responses.body().string();
                // Transform reponse to JSon Object
                try {
                    JSONObject json = new JSONObject(jsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("Url_json_method",String.valueOf(jsonData));
                return jsonData;
            }

            return "";
        }
        public static String getData(String myurl) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); // time in milliseconds
                conn.setConnectTimeout(15000); // time in milliseconds
                conn.setRequestMethod("GET"); // request method GET OR POST
                conn.setDoInput(true);
                // Starts the query
                conn.connect(); // calling the web address
                int response = conn.getResponseCode();
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readInputStream(is);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        public static String readInputStream(InputStream stream) throws IOException {
            int n = 0;
            char[] buffer = new char[1024 * 4];
            InputStreamReader reader = new InputStreamReader(stream, "UTF8");
            StringWriter writer = new StringWriter();
            while (-1 != (n = reader.read(buffer)))
                writer.write(buffer, 0, n);
            return writer.toString();
        }

        private static String convertInputStreamToString(InputStream inputStream) throws IOException{
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            inputStream.close();
            return result;

        }
}
