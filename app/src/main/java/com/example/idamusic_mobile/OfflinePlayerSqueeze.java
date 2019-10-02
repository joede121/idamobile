package com.example.idamusic_mobile;

import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class OfflinePlayerSqueeze extends OfflinePlayerDev {
    final static String SERVER_URL_2 = "http://192.168.2.6:9000/";
    final static String SERVER_URL = "http://192.168.2.8:9000/";
    final static String PLAYER_MAC = "74:da:38:5b:5d:08";

    String mActualServerUrl = SERVER_URL;
    String mActualPlayerMac = PLAYER_MAC;

    final static String PLAYER_LIVINGROOM = "Livingroom Touch";
    final static String PLAYER_BEDROOM = "Bedroom radio";
    final static String PLAYER_BATHROOM = "bathroommusik";




// Bedroom
    //final static String PLAYER_MAC = "00:04:20:22:7c:31";
    //final static String SERVER_URL = "http://192.168.2.6:9000/";

    OfflinePlayerDevListener mListener;
    PlayableItemOffline mActualPi;
    Song mActualSong = new Song(1, "1", "1");
    Integer mActualTime = 0;
    Integer mActualDuration = 0;
    static List<OfflinePlayerSqueezePlayer> mPlayers;
    static List<String> mSqueezeServerUrls;



    public OfflinePlayerSqueeze(OfflinePlayerDevListener listener){
        mListener = listener;


    }

    static{
        mPlayers = new ArrayList<>();
        mSqueezeServerUrls = new ArrayList<>();
        mSqueezeServerUrls.add(SERVER_URL);
        mSqueezeServerUrls.add(SERVER_URL_2);
    }

    public static ArrayList<String> getAllPlayer() {
        getPlayers();
        ArrayList<String> players = new ArrayList<>();
        for(OfflinePlayerSqueezePlayer pl: mPlayers){
            players.add(pl.mName);
        }

        return players;
    }

    public static boolean isAvailable(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(getRequest(new JSONArray().put("status"), SERVER_URL, PLAYER_MAC)).execute()) {
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

    static void getPlayers(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient();
        mPlayers.clear();
        for(String serverurl: mSqueezeServerUrls) {
            try (Response response = client.newCall(getRequest(
                    new JSONArray().put("serverstatus")
                            .put("-")
                            .put("-"), serverurl, PLAYER_MAC))
                    .execute()) {
                String res = response.body().string();
                Log.d("LOG_SQ_RESPONSE", response.message() + response.isSuccessful() + res);
                if (response.isSuccessful()) {
                    try {
                        JSONObject jo = (JSONObject) new JSONObject(res).get("result");
                        JSONArray jplayers = (JSONArray) jo.get("other_players_loop");
                        for (int i = 0; i < jplayers.length(); i++) {
                            JSONObject player = (JSONObject) jplayers.get(i);
                            mPlayers.add(
                                    new OfflinePlayerSqueezePlayer(
                                            (String) player.get("playerid"),
                                            (String) player.get("name"),
                                            (String) player.get("serverurl")));
                        }


                    } catch (JSONException err) {
                        Log.d("Error", err.toString());
                    }

                }

            } catch (IOException io) {
                Log.d("LOG_SQ_RESPONSE_ERROR", io.toString());
            }
        }
    }



    public void getStatusPlayer() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient();

            client.newCall(getRequest(new JSONArray().put("status"), mActualServerUrl, mActualPlayerMac)).enqueue(new Callback() {
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
                        if(mActualPi != null) {
                            Song song = mActualPi.mSongs.get(new Integer((String) rs.get("playlist_cur_index")));
                            double fl = (double) rs.get("duration");
                            mActualDuration = (int) Math.round(fl * 1000);
                            if (rs.get("time") instanceof Integer) {
                                mActualTime = Math.round((int) rs.get("time") * 1000);
                            }else if(rs.get("time") instanceof String) {
                                mActualTime = (int) Math.round(new Float((String) rs.get("time")) * 1000);
                            } else {
                                fl = (double) rs.get("time");
                                mActualTime = (int) Math.round(fl * 1000);
                            }

                            String mode = (String) rs.get("mode");
                            int cur_idx = new Integer((String) rs.get("playlist_cur_index"));
                            int ply_idx = (int) rs.get("playlist_tracks");
                            cur_idx = cur_idx + 1;


                            Log.d("LOG_SQ_RESPONSE", song + mActualDuration.toString() + mActualTime.toString());
                            if (!mActualSong.equals(song)) {
                                mActualSong = song;
                                mListener.onSongCompletion();
                                mActualSong = song;
                            }
                            if (mode.equals("stop") && (cur_idx == ply_idx)) {
                                mListener.onSongCompletion();
                            } else if( mode.equals("stop")) {
                                mListener.onPlayerStateChange(Player.PLAYER_STATE_PAUSE);
                            }


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
        try
        {
            Thread.sleep(300);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }


    private void playAlbum(PlayableItemOffline pi){
        if (pi != null) {
            setCmdUrlApi("&p0=playlist&p1=loadalbum&p2=*&p3=*&p4=" + pi.name, mActualServerUrl);
            mActualPi = pi;
        }
    }

    private void playTrack(Integer number){
        setCmdUrlApi("&p0=playlist&p1=jump&p2=" + number, mActualServerUrl);
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
        setCmdUrlApi("&p0=play", mActualServerUrl);
    }

    @Override
    public void pause() {
        setCmdUrlApi("&p0=pause&p1=1", mActualServerUrl);

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

        new OkHttpClient().newCall(OfflinePlayerSqueeze.getRequest(ja, mActualServerUrl, mActualPlayerMac)).enqueue(new Callback(){
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

    @Override
    public void setActivePlayer(String player) {
        Log.d("LOG_SQ_PLAYERS", mPlayers + "");
        Log.d("LOG_SQ_PLAYER", player);
        for(OfflinePlayerSqueezePlayer pl: mPlayers){
            if(pl.mName.equals(player)){
                mActualPlayerMac = pl.mMac;
                mActualServerUrl = pl.mServer_url;
            }
        }

    }


    private Request getRequestURLApi(String cmd, String url) {
        String url_req = url + "status.html?player=" + mActualPlayerMac + cmd;
        Log.d("LOG_SQ_RESPONSE_URL", url_req);
        return  new Request.Builder()
                .url(url_req)
                .get()
                .build();
    }

    private void setCmdUrlApi( String cmd, String url){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(getRequestURLApi(cmd, url)).execute()) {
            Log.d("LOG_SQ_RESPONSE_URL", response.message() + response.isSuccessful());
        } catch( IOException io) {
            Log.d("LOG_SQ_RESP_URL_ERROR", io.toString());
        }

    }

    private static Request getRequest(JSONArray cmd, String url, String mac){
        String jsonBody = "";
        try {
            jsonBody = new JSONObject()
                    .put("id", new Integer(1) )
                    .put("method", "slim.request")
                    .put("params", new JSONArray().put(mac).put(cmd))
                    .toString();

        } catch (JSONException e) {
        }

        return  new Request.Builder()
                .url(url + "jsonrpc.js")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody))
                .build();
    }

}
