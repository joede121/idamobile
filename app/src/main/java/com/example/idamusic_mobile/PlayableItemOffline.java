package com.example.idamusic_mobile;

import java.util.ArrayList;
import java.util.List;

public class PlayableItemOffline extends PlayableItem {

    public String image_string;


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

}


