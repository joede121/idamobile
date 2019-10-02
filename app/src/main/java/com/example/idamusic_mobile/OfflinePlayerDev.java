package com.example.idamusic_mobile;

import java.security.PublicKey;

public abstract class OfflinePlayerDev{

    public abstract void disconnect();
    public abstract void playSong(Song song, PlayableItemOffline pi);
    public abstract int getDuration();
    public abstract void resume();
    public abstract void pause();
     public abstract int getCurrentPosition();

    public abstract void setCurrentPosition(int currentPosition);


    public interface OfflinePlayerDevListener{
        public void onSongCompletion();
    }
}
