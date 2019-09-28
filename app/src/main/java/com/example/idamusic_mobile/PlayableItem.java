package com.example.idamusic_mobile;


import android.graphics.Bitmap;

import android.util.Log;

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


    public PlayableItem(String uri, Player player1) {
        player = player1;
        this.uri = uri;
        if("spotify:album:".equals(uri.substring(0,14))){
            this.uri = uri.substring(14);
        }
        this.spotify_uri = "spotify:album:" + this.uri;
        mSongs = new ArrayList<>();
        getMetaData(uri,player1);
     }

    public PlayableItem() {

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





}
