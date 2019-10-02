package com.example.idamusic_mobile;

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


    public interface OfflinePlayerDevListener{
        public void onSongCompletion();
        public void onPlayerStateChange(String state);
    }
}



