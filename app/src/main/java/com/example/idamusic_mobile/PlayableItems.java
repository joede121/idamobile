package com.example.idamusic_mobile;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PlayableItems {
    public List<PlayableItemOffline> mPlayableItems = new ArrayList<>();

    public PlayableItemOffline getPlayableItemFromUri(String uri){
        Log.d("getplayableitemsuri", uri);
        for(PlayableItemOffline pi: mPlayableItems){
            Log.d("getplayableitemsuri1", pi.spotify_uri);
            Log.d("getplayableitemsuri12", pi.uri);
            if(pi.spotify_uri.equals(uri)){
                return pi;
            }
        }
        return  null;
    }

}
