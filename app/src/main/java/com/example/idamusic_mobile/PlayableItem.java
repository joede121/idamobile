package com.example.idamusic_mobile;


import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class PlayableItem {

    transient Player player;
    String uri;
    public String artist;
    public String name;
    public Bitmap image;
    public String spotify_uri;
    public List<Song> mSongs;
    final int ERROR_CNT_LIMIT = 20;
    int error_cnt;


    public PlayableItem(String uri, Player player1) {
        player = player1;
        this.uri = uri;;

        if("spotify:album:".equals(uri.substring(0,14))){
            this.uri = uri.substring(14);
        }
        spotify_uri = uri;
        if(!uri.startsWith("file:"))
            this.spotify_uri = "spotify:album:" + this.uri;
        mSongs = new ArrayList<>();
        getMetaData(uri,player1);
     }


public PlayableItem(){

}

    protected void getMetaData(String uri1, Player player1 ) {

        player1.getAlbum(uri1, new PlayerListenerAlbum() {
            @Override
            public void success(String album, String artist1, Bitmap cover) {
                name = album;
                artist = artist1;
                image = cover;
                Log.d("Album success", name + artist);

            }

            @Override
            public void error() {

            }
        });

    }

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
                Log.d("PLAYABLE_BITMAPURL_ER", e.toString());
                return getBitmapFromURL(src);
            } else {
                return null;
            }
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }



}
