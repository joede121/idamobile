package com.example.idamusic_mobile;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.StrictMode;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpotifyWebApi {

    public static String getPlayers(String mAccessToken) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    OkHttpClient client = new OkHttpClient();

    String url = "https://api.spotify.com/v1/me/player/devices";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return (response).body().string();
        }
    }


    public static void setActivePlayer(String id, String mAccessToken) throws IOException{
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.spotify.com/v1/me/player";
        String jsonBody = "";
        try {
            jsonBody = new JSONObject()
                    .put("device_ids", new JSONArray()
                            .put(id)).toString();
        }catch(JSONException e){}

/*        String jsonBody= "{\"device_ids\": " +
                      "[\"" +id+ "\"]" +
                      "[\"" +id+ "\"]" +
                      "}";

*/      MediaType JSON = MediaType.parse("application/json; charset=utf-8");;
        RequestBody body1 = RequestBody.create(JSON, jsonBody);
        Log.d("setDevice", body1.toString() + jsonBody );

        Request request = new Request.Builder()
                .url(url)
                .put(body1)
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        Log.d("setDevice", request.toString() + request.headers().toString() + request.body().toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("LOG_TAG_ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("LOG_TAG_BODY", response.body().string());
                Log.d("LOG_TAG", response.toString());

            }

        });

       // Response response = client.newCall(request).execute();
       // Log.d("setDevice", "Guten Tag" + response.body().string() + response.body().contentType());
    }




    }



