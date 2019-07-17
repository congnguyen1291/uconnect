package vn.urekamedia.liboverlay.services;



import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Nani on 11/5/2016.
 */

public class OkHttpHelper {
    private static final String TAG = "OkHttpHelper";
    private static final long TIME_OUT = 3 * 60 * 1000;
    public static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public static void call(Request request, Callback callback) {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(TIME_OUT, TimeUnit.MILLISECONDS);
        b.writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS);
        // set other properties
        OkHttpClient client = b.build();
        client.newCall(request).enqueue(callback);
    }

    public static void doPost(String url, String json, Callback callback){
        RequestBody body = RequestBody.create(TYPE_JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        call(request, callback);
    }
    public static void doGet(String url, Callback callback){
        Request request = new Request.Builder()
                .url(url)
                .build();
        call(request, callback);
    }

}
