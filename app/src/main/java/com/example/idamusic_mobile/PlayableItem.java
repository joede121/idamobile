package com.example.idamusic_mobile;


import android.graphics.Bitmap;

import android.util.Log;


public class PlayableItem {

    transient Player player;
    String uri;
    public String artist;
    public String name;
    public Bitmap image;
    public String spotify_uri;


    public PlayableItem(String uri, Player player1) {
        player = player1;
        this.uri = uri;
        this.spotify_uri = "spotify:album:" + uri;
        getMetaData(uri,player1);
     }

    public PlayableItem() {

    }

    private void getMetaData(String uri1, Player player1 ) {

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
