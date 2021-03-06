package com.example.idamusic_mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Player {
    public static final String PLAYER_STATE_PLAY = "PLAY";
    public static final String PLAYER_STATE_PAUSE= "PAUSE";
    public static final String MODE_ONLINE = "ONLINE";
    public static final String MODE_OFFLINE= "OFFLINE";
    protected PlayerListener listener;
    public static Player actualPlayer;

    Integer error_cnt;
    final static int ERROR_CNT_LIMIT = 99;

    public String getStringDuration(int duration){
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static Player factory(MainActivity act, PlayerListener listener, String mode) {
        if(mode.equals("OFFLINE")){
            return new OfflinePlayer(act.getApplicationContext(), listener);
        }else {
            return new SpotifyPlayer(act, listener);
        }
    }

    public abstract PlayableItem getActPlayableItem();

    public abstract void connect();

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    public void setPlayerListener(PlayerListener listener ){
        listener = listener;
    }

    public abstract void disconnect();

    public abstract String getActualTrackUri();

    public abstract boolean isPaused();

    public abstract void play(String uri);

    public abstract void resume( );

    public abstract void next();

    public abstract  void prev();

    public abstract  void pause();

    public abstract void play_song(String uri_song);

    public abstract void getPlayableItems( String prefix, PlayerListenerPlaylists listener);
    public abstract void getAlbum( String uri, PlayerListenerAlbum listener);
    public abstract void getAlbumTracks( String uri, PlayerListenerAlbumTracks listener);

    public abstract String getActualAlbum();

    public abstract boolean isConnected();

    public abstract void setPiepserAsActivePlayer();

    public abstract ArrayList<String> getAllPlayers();

    public abstract void setActivePlayer(String player);

    public abstract boolean supportBeamToPiepser();

    public abstract String getActivePlayerDevice();

    public abstract int getCurrentPosition();

    public abstract void setCurrentPosition( int currentPosition );

    public abstract boolean isRemotePlayer();

    public abstract void setRemoteMode(boolean remoteModeOn);

    public abstract boolean isRemoteModeOn();

    public abstract void setActivity(PlayerListener listener, Context context);

    public Bitmap getBitmapFromURL(String src) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Bitmap myBitmap;
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            error_cnt++;
            if (error_cnt < ERROR_CNT_LIMIT) {
                return getBitmapFromURL(src);
            } else {
                return null;
            }
        }
    }
}





