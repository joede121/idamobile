package com.example.idamusic_mobile;

import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class OfflinePlayerSqueeze extends OfflinePlayerDev {

    final static String SERVER_URL = "http://192.168.2.8:9000/";
    final static String PLAYER_MAC = "74:da:38:5b:5d:08";

    // Bedroom
    //final static String PLAYER_MAC = "00:04:20:22:7c:31";
    //final static String SERVER_URL = "http://192.168.2.6:9000/";

    OfflinePlayerDevListener mListener;
    PlayableItemOffline mActualPi;
    Song mActualSong = new Song(1, "1", "1");
    Integer mActualTime = 0;
    Integer mActualDuration = 0;


    public OfflinePlayerSqueeze(OfflinePlayerDevListener listener){
        mListener = listener;
    }



    public static boolean isAvailable(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(getRequest(new JSONArray().put("status"))).execute()) {
            Log.d("LOG_SQ_RESPONSE", response.message() + response.isSuccessful() + response.body().string());
            if (response.isSuccessful()) {
                return true;
            }else {
                return false;
            }

        } catch( IOException io) {
            Log.d("LOG_SQ_RESPONSE_ERROR", io.toString());
                return false;
            }
    }



    public void getStatusPlayer() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient();

            client.newCall(getRequest(new JSONArray().put("status"))).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("LOG_SQ_RESPONSE_ERROR", e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String res = response.body().string();
                    Log.d("LOG_SQ_RESPONSE", res );

                    try {
                        JSONObject rs1 = new JSONObject(res);
                        JSONObject rs =  (JSONObject) rs1.get("result");
                        Song song = mActualPi.mSongs.get(new Integer((String) rs.get("playlist_cur_index")));
                        double fl = (double) rs.get("duration");
                        mActualDuration = (int) Math.round(fl * 1000);
                        if (rs.get("time") instanceof Integer){
                            mActualTime = Math.round((int) rs.get("time")  * 1000);

                        }else{
                            fl = (double) rs.get("time");
                            mActualTime = (int) Math.round(fl * 1000);
                        }

                        String mode = (String)  rs.get("mode");
                        int cur_idx = new Integer((String) rs.get("playlist_cur_index"));
                        int ply_idx = (int) rs.get("playlist_tracks");
                        cur_idx = cur_idx + 1;


                        Log.d("LOG_SQ_RESPONSE", song + mActualDuration.toString() + mActualTime.toString() );
                        if(!mActualSong.equals(song)){
                            mActualSong = song;
                            mListener.onSongCompletion();
                            mActualSong = song;
                        }if (mode.equals("stop") && (cur_idx == ply_idx)){
                            mListener.onSongCompletion();
                        }

                    }catch (JSONException err){
                        Log.d("Error", err.toString());
                    }


                }

            });
    }

    @Override
    public void disconnect() {
        pause();
    }


    private void playAlbum(PlayableItemOffline pi){
if (pi != null) {
    setCmdUrlApi("&p0=playlist&p1=loadalbum&p2=*&p3=*&p4=" + pi.name);
    mActualPi = pi;
}
    }

    private void playTrack(Integer number){
        setCmdUrlApi("&p0=playlist&p1=jump&p2=" + number);
        getStatusPlayer();
    }

    @Override
    public void playSong(Song song, PlayableItemOffline pi) {
        if (mActualPi == null){
            playAlbum(pi);
            playTrack(pi.getNumberOfSong(song));
            mActualTime = 0;
        }else {
            if (!pi.equals(mActualPi)) {
                playAlbum(pi);
                playTrack(pi.getNumberOfSong(song));
                mActualTime = 0;
            }else {
                if (!mActualSong.equals(song))
                playTrack(pi.getNumberOfSong(song));
                mActualTime = 0;
            }
        }
        mActualSong = song;
        getStatusPlayer();
    }

    @Override
    public int getDuration() {
        return mActualDuration;
    }

    @Override
    public void resume() {
        setCmdUrlApi("&p0=play");
    }

    @Override
    public void pause() {
        setCmdUrlApi("&p0=pause&p1=1");

    }

    @Override
    public int getCurrentPosition() {
        getStatusPlayer();
        return mActualTime;
    }

    @Override
    public void setCurrentPosition(int currentPosition) {
        mActualTime = currentPosition;
        Double db = (double) currentPosition / 1000;
        JSONArray ja = new JSONArray();
        ja.put("time");
        ja.put(db);

        new OkHttpClient().newCall(OfflinePlayerSqueeze.getRequest(ja)).enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("LOG_SQ_RESPONSE_ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d("LOG_SQ_RESPONSE", res );
            }

        });
    }




    private Request getRequestURLApi(String cmd) {
        String url = SERVER_URL + "status.html?player=" + PLAYER_MAC + cmd;
        Log.d("LOG_SQ_RESPONSE_URL", url);
        return  new Request.Builder()
                .url(url)
                .get()
                .build();
    }

    private void setCmdUrlApi( String cmd){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(getRequestURLApi(cmd)).execute()) {
            Log.d("LOG_SQ_RESPONSE_URL", response.message() + response.isSuccessful());
        } catch( IOException io) {
            Log.d("LOG_SQ_RESP_URL_ERROR", io.toString());
        }

    }

    private static Request getRequest(JSONArray cmd){
        String jsonBody = "";
        try {
            jsonBody = new JSONObject()
                    .put("id", new Integer(1) )
                    .put("method", "slim.request")
                    .put("params", new JSONArray().put(PLAYER_MAC).put(cmd))
                    .toString();

        } catch (JSONException e) {
        }

        return  new Request.Builder()
                .url(SERVER_URL + "jsonrpc.js")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody))
                .build();
    }

}
