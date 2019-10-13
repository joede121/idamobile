package com.example.idamusic_mobile;

import android.content.Context;

import java.security.PublicKey;
import java.util.List;

public abstract class OfflinePlayerDev{

    public abstract void disconnect();
    public abstract void playSong(Song song, PlayableItemOffline pi);
    public abstract int getDuration();
    public abstract void resume();
    public abstract void pause();
    public abstract int getCurrentPosition();

    public abstract void setCurrentPosition(int currentPosition);


    public abstract void setActivePlayer(String player);

    public abstract boolean isRemotePlayer();

    public abstract void setRemoteMode(boolean remoteMode);
    public abstract void getPlayables(Context context);


    public interface OfflinePlayerDevListener{
        public void onSongCompletion();
        public void onPlayerStateChange(String state);
        public void onDurationChange(int duration);
        public void onProgressChange(int progress);
        public void onPlaylistItemChange(PlayableItemOffline pi);
    }
}



