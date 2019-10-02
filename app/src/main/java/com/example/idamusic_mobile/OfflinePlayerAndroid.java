package com.example.idamusic_mobile;


import android.content.ContentUris;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class OfflinePlayerAndroid  extends OfflinePlayerDev implements MediaPlayer.OnCompletionListener {

    OfflinePlayerDevListener mListener;
    MediaPlayer mMediaPlayer;
    Context mContext;


    public OfflinePlayerAndroid(OfflinePlayerDevListener listener, Context context) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mListener = listener;
        mContext = context;

    }

    public void disconnect() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mListener.onSongCompletion();
    }

    public void playSong(Song song, PlayableItemOffline pi) {

        Uri myUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.getId());
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this.mContext, myUri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public void resume() {
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void setCurrentPosition(int currentPosition) {
        mMediaPlayer.seekTo(currentPosition);
    }

}
