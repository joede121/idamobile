package com.example.idamusic_mobile;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;
import java.util.List;

public class OfflinePlayer extends Player implements MediaPlayer.OnCompletionListener{
    private final static String FILENAME_PLAYABLES = "playable_items";
    private MainActivity activity;
    PlayableItems mPItems;
    Context mContext;
    boolean mIsConnected;
    MediaPlayer mMediaPlayer;
    Song mActualSong;
    PlayableItemOffline mActualPlayable;
    String mActState ="";
    SettingsParcelable mSettings;
    transient static final String FILENAME_SETTINGS = "SETTINGS";

    public OfflinePlayer(Context context, PlayerListener listener){
        this.listener = listener;
        this.mContext = context;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mActState = PLAYER_STATE_PAUSE;
        listener.onPlayerStateChange(mActState);
        mSettings = new SettingsParcelable(context, FILENAME_SETTINGS);

        listener.onTrackChange("Nix ausgewählt", "");
        listener.onAlbumCoverChange(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_play_selection));
    }

    @Override
    public void connect() {
        // Oflline Sachen Holel);
        this.mPItems = getPlayableItemsFromFile(this.mContext);
        if (mPItems != null) {
            mActualPlayable = mPItems.getPlayableItemFromUri(mSettings.offline_uri_playable);
            if (mActualPlayable != null) {
                mActualSong = mActualPlayable.getSongFromId(mSettings.offline_id_song);
                if (mActualSong != null) {
                    Log.d("trackchange", mActualSong.getTitle() + mActualPlayable.uri );
                    playActualSong();
                    pause();
                }
            }

        }
        mIsConnected = true;
        listener.onConnected();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do Nothing
    }

    @Override
    public void disconnect() {
        mIsConnected = false;
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mSettings.toFile(mActualPlayable.spotify_uri, mActualSong.getId());
    }

    @Override
    public String getActualTrackUri() {
        return null;
    }

    @Override
    public void play(String uri) {

        PlayableItemOffline pi = mPItems.getPlayableItemFromUri(uri);
        if (pi != null) {
            mActualPlayable = pi;

            Song song = pi.mSongs.get(0);
            playSong(song);
        }
    }

    void playActualSong() {
            playSong(mActualSong);
    }

    void playSong(Song song){

        mActualSong = song;

        Uri myUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.getId());
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this.mContext, myUri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mActState = PLAYER_STATE_PLAY;
        } catch (Exception e){
            Log.d("Error", e.toString());
        }

        listener.onPlayerStateChange(PLAYER_STATE_PLAY);
        listener.onTrackChange(song.getTitle(), mActualPlayable.spotify_uri);
        listener.onAlbumCoverChange(mActualPlayable.image);
    }

    @Override
    public void resume() {
        mMediaPlayer.start();
        mActState = PLAYER_STATE_PLAY;
        listener.onPlayerStateChange(PLAYER_STATE_PLAY);
        listener.onTrackChange(mActualSong.getTitle(), mActualPlayable.spotify_uri);
    }

    @Override
    public void next() {
        Song song = mActualPlayable.getNextSong(mActualSong);
        for (Song s: mActualPlayable.mSongs){
            Log.d("songs", s.getId() + s.getTitle() + mActualPlayable.name);
        }
        if(song != null) {
            Log.d("next", song.getId() + song.getTitle());
            playSong(song);
            mSettings.toFile(mActualPlayable.spotify_uri, mActualSong.getId());
        }
    }

    @Override
    public void prev() {
        Song song = mActualPlayable.getLastSong(mActualSong);
        Log.d("prev", song.getId() + song.getTitle());
        playSong(song);
    }

    @Override
    public void pause() {
        Log.d("Pause", "");
        mMediaPlayer.pause();
        mActState = PLAYER_STATE_PAUSE;
        listener.onPlayerStateChange(mActState);
    }

    @Override
    public void getPlayableItems(String prefix, PlayerListenerPlaylists listener) {
        for(PlayableItem pi: mPItems.mPlayableItems ){
            listener.success(pi.uri);
        }
    }

    @Override
    public void getAlbum(String uri, PlayerListenerAlbum listener) {
        for(PlayableItem pi: mPItems.mPlayableItems){
            if(pi.uri.equals(uri)) {
                listener.success(pi.name, pi.artist, pi.image);
                break;
            }
        }

    }

    @Override
    public String getActualAlbum() {
        if (mActualPlayable != null)
            return mActualPlayable.spotify_uri;
        return null;
    }

    @Override
    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public void setPiepserAsActivePlayer() {

    }

    @Override
    public List<String> getAllPlayers() {
        return null;
    }

    @Override
    public void setActivePlayer(String player) {

    }

    @Override
    public boolean supportBeamToPiepser() {
        return false;
    }

    @Override
    public String getActivePlayerDevice() {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mActState = Player.PLAYER_STATE_PAUSE;
        mSettings.toFile(mActualPlayable.spotify_uri, mActualSong.getId());
        next();
    }

    private PlayableItems getPlayableItemsFromFile(Context context){
        String json = readJSON(context, FILENAME_PLAYABLES);
        PlayableItems pis = new Gson().fromJson(json, PlayableItems.class);
        PlayableItems pis2 = new PlayableItems();
        for(PlayableItemOffline pi: pis.mPlayableItems){
            pi.image = getBitmapFromString(pi.image_string);
            pi.player = this;
            if (!pi.spotify_uri.substring(0,6).equals("spotify")) pi.spotify_uri = "spotify:album:" + pi.spotify_uri;
            pi.mSongs = new ArrayList<>();
            if (isOfflineAvailable(pi)) pis2.mPlayableItems.add(pi);
        }
        return  pis2;
    }

    public static void makeOffline( List<PlayableItem> items, Context context){

        PlayableItems play_items = new PlayableItems();
        for(PlayableItem pi: items) {
            PlayableItemOffline pio = new PlayableItemOffline(pi);
            pio.image_string = getStringFromBitmap(pi.image);
            play_items.mPlayableItems.add(pio);
        }

        String json = new Gson().toJson(play_items);
        Log.d("Offline - JSON", json);
        writeJSON(json, context, FILENAME_PLAYABLES);
    }

    public static void writeJSON(String json, Context context, String file){
        String filename = file;
        String fileContents = json;
        FileOutputStream outputStream;

        try {
            outputStream = context.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
            Log.d("Offline - Writing OK", filename);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Offline - Writing Error", e.toString());
        }
    }

    public static String readJSON(Context context, String file){
        String filename = file;

        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            Log.d("Offline - Read", sb.toString());
            return sb.toString();
        }catch(Exception e){
            Log.d("Offline - ReaError ", e.toString());
            return null;
            }
        }


    private static String getStringFromBitmap(Bitmap bitmapPicture) {
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    private Bitmap getBitmapFromString(String stringPicture) {
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    private boolean isOfflineAvailable(PlayableItemOffline pio){
        boolean found = false;
        ContentResolver musicResolver = this.mContext.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.ALBUM + " = ? AND "
                         + MediaStore.Audio.Media.ARTIST + " = ?";
        String[] selectionArgs = {pio.name, pio.artist};
        String sort_order = MediaStore.Audio.Media.TRACK + " ASC";

        //selectionArgs[1] = pio.artist;
        Log.d("music0 ", selection + selectionArgs[0]);
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, selectionArgs, sort_order);
        if(musicCursor != null){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            while (musicCursor.moveToNext()){
                Log.d("music0 ", musicCursor.toString());
                pio.mSongs.add(new Song(musicCursor.getLong(idColumn), musicCursor.getString(titleColumn), musicCursor.getString(artistColumn)));
                Log.d("music1 ", musicCursor.getString(idColumn));
                Log.d("music2 ", musicCursor.getString(artistColumn) + musicCursor.getString(titleColumn));
                found = true;

            }
            if(found) return true;
            return false;
        }
        return false;
    }

    @Override
    public boolean isPaused() {
        if(mActState.equals(PLAYER_STATE_PAUSE)) return true;
        return false;
    }
}
