package com.example.idamusic_mobile;

import java.util.ArrayList;
import java.util.List;

public class PlayableItemOffline extends PlayableItem {

    public String image_string;
    public boolean isRemote;


    public PlayableItemOffline(String uri, Player player1) {
        super(uri, player1);
    }

    public PlayableItemOffline(PlayableItem pi) {
        super();
        this.artist = pi.artist;
        this.image = pi.image;
        this.name = pi.name;
        this.uri = pi.uri;
        this.spotify_uri = pi.uri;

    }

    public PlayableItemOffline(){
        super();
    }

    public Song getNextSong(Song song) {

        int i = 0;
        for (Song act_song : mSongs) {
            if (act_song.getId() == song.getId()) {
                if (i < mSongs.size() - 1) {
                    return mSongs.get(i + 1);
                } else {
                    return null;
                }
            }
            i++;
        }
        return null;
    }

    public Song getLastSong(Song song) {

        int i = 0;
        for (Song act_song : mSongs) {
            if (act_song.getId() == song.getId()) {
                if (i > 1) {
                    int n = i - 1;
                    return mSongs.get(n);
                } else {
                    return mSongs.get(0);
                }
            }
            i++;
        }
        return null;
    }

    public Song getSongFromId(Long id) {
        for (Song act_song: mSongs) {
            if (act_song.getId() == id)
                return act_song;
        }
        return null;
    }

    public Song getSongFromUri(String uri) {
        for (Song act_song: mSongs) {
            if (act_song.getUri().equals(uri))
                return act_song;
        }
        return null;
    }


    public int getNumberOfSong(Song song){
        int i = 0;
        for (Song act_song : mSongs) {
            if (act_song.getId() == song.getId()) {
                return i;
            }
            i++;
        }
        return 0;
    }

}


