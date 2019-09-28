package com.example.idamusic_mobile;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PlayableItemSpotify extends PlayableItem {


    public PlayableItemSpotify(String uri, Player player1) {
        super(uri, player1);
    }
    public PlayableItemSpotify() {
        super();
    }

    @Override
    protected void getMetaData(String uri1, Player player1 ) {
        super.getMetaData(uri1,player1);


        player1.getAlbumTracks(uri1, new PlayerListenerAlbumTracks() {
            @Override
            public void success(Song song) {
                mSongs.add(song);

            }

            @Override
            public void error() {

            }
        });



    }
}


