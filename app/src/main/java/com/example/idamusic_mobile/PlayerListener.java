package com.example.idamusic_mobile;

import android.graphics.Bitmap;

interface PlayerListener {

    public void onPlayerStateChange( String player_state );

    public void onTrackChange( String track_name, String uri, int duration, String artist);

    public void onAlbumCoverChange( Bitmap cover );

    public void onConnected( );

    public void onConnectedError( );

}


interface PlayerListenerPlaylists {

    public void success(String uri);

    public void error();

}

interface PlayerListenerAlbum {
    public void success(String album, String artist, Bitmap cover );

    public void error();

}

interface PlayerListenerAlbumTracks {
    public void success(Song song);

    public void error();

}

