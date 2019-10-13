package com.example.idamusic_mobile;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OfflinePlayer extends Player implements OfflinePlayerDev.OfflinePlayerDevListener{
    private final static String FILENAME_PLAYABLES = "playable_items";
    private MainActivity activity;
    PlayableItems mPItemsOffline;
    PlayableItems mPitemsRemote;
    PlayableItems mActualPlayableItems;
    Context mContext;
    boolean mIsConnected;
    Song mActualSong;
    PlayableItemOffline mActualPlayable;
    String mActState ="";
    SettingsParcelable mSettings;
    OfflinePlayerDev mPlayerDevice;
    transient static final String FILENAME_SETTINGS = "SETTINGS";
    String mActualPlayer = SpotifyPlayer.THIS_DEVICE_NAME;
    PlayerListenerPlaylists mPlaylistListener;
    boolean mRemoteModeOn;

    public OfflinePlayer(Context context, PlayerListener listener) {
        this.listener = listener;
        this.mContext = context;
        mPlayerDevice = new OfflinePlayerAndroid(this, context);
        mActState = PLAYER_STATE_PAUSE;
        listener.onPlayerStateChange(mActState);
        mSettings = new SettingsParcelable(context, FILENAME_SETTINGS);
        listener.onTrackChange("Nix ausgewählt", "", 0, "");
        listener.onAlbumCoverChange(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_play_selection));
        mActualPlayer = SpotifyPlayer.THIS_DEVICE_NAME;
        actualPlayer = this;

    }

    @Override
    public void setActivity(PlayerListener listener, Context context) {
        this.listener = listener;
        this.mContext = context;
    }

    @Override
    public void connect() {
        // Oflline Sachen Holel);
        if (!this.isConnected()) {
            this.mPItemsOffline = getPlayableItemsFromFile(this.mContext);
            mActualPlayableItems = mPItemsOffline;
            if (mPItemsOffline != null) {
                mActualPlayable = mPItemsOffline.getPlayableItemFromUri(mSettings.offline_uri_playable);
                if (mActualPlayable != null) {
                    mActualSong = mActualPlayable.getSongFromId(mSettings.offline_id_song);
                    if (mActualSong != null) {
                        Log.d("trackchange", mActualSong.getTitle() + mActualPlayable.uri);
                        playActualSong();
                        pause();
                    }
                }

            }
            mIsConnected = true;
        }else{
            listener.onPlayerStateChange(mActState);
            listener.onTrackChange(mActualSong.getTitle(), mActualSong.getUri(),  mPlayerDevice.getDuration(),mActualSong.getArtist());
            listener.onAlbumCoverChange(mActualPlayable.image);
            Log.d("ConnectDuration", mPlayerDevice.getDuration() + "");

        }
        listener.onConnected();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do Nothing
    }

    @Override
    public void disconnect() {
        mIsConnected = false;
        mPlayerDevice.disconnect();
        if (mActualSong != null)
            mSettings.toFile(mActualPlayable.spotify_uri, mActualSong.getId());
    }

    @Override
    public String getActualTrackUri() {
        return mActualSong.getUri();
    }

    @Override
    public void play(String uri) {

        PlayableItemOffline pi = mActualPlayableItems.getPlayableItemFromUri(uri);
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
        if (song != null) {
            mActualSong = song;
            mPlayerDevice.playSong(mActualSong, mActualPlayable);
            mActState = PLAYER_STATE_PLAY;
            listener.onTrackChange(song.getTitle(), mActualPlayable.spotify_uri, mPlayerDevice.getDuration(), mActualSong.getArtist());
            listener.onAlbumCoverChange(mActualPlayable.image);
            listener.onPlayerStateChange(PLAYER_STATE_PLAY);
        }
    }

    @Override
    public void resume() {
        mPlayerDevice.resume();
        mActState = PLAYER_STATE_PLAY;
        listener.onPlayerStateChange(PLAYER_STATE_PLAY);
        listener.onTrackChange(mActualSong.getTitle(), mActualPlayable.spotify_uri, mPlayerDevice.getDuration(), mActualSong.getArtist());
    }

    @Override
    public void next() {
        Song song = mActualPlayable.getNextSong(mActualSong);
        if(song != null) {
            Log.d("next", song.getId() + song.getTitle());
            playSong(song);
            mSettings.toFile(mActualPlayable.spotify_uri, mActualSong.getId());
        }else{
            listener.onPlayerStateChange(mActState);
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
        mPlayerDevice.pause();
        mActState = PLAYER_STATE_PAUSE;
        listener.onPlayerStateChange(mActState);
    }

    @Override
    public void getPlayableItems(String prefix, PlayerListenerPlaylists listener) {
        mPlaylistListener = listener;
        if (mRemoteModeOn){
            mPlayerDevice.getPlayables(mContext);
        }else {
            for (PlayableItem pi : mPItemsOffline.mPlayableItems) {
                listener.success(pi.uri);
            }
        }
    }

    @Override
    public void getAlbum(String uri, PlayerListenerAlbum listener) {
        for(PlayableItem pi: mActualPlayableItems.mPlayableItems){
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
        setActivePlayer(SpotifyPlayer.PIEPSER_DEVICE_NAME);
    }

    @Override
    public ArrayList<String> getAllPlayers() {
        ArrayList<String> players = OfflinePlayerSqueeze.Companion.getAllPlayer();
        players.add(SpotifyPlayer.THIS_DEVICE_NAME);
        return players;
    }

    @Override
    public void onDurationChange(int duration) {
        listener.onTrackChange(mActualSong.getTitle(), mActualPlayable.spotify_uri, duration, mActualSong.getArtist());
    }

    @Override
    public void onPlayerStateChange(String state) {
        listener.onPlayerStateChange(state);
    }

    @Override
    public void setActivePlayer(String player) {
        if (!mActualPlayer.equals(player)){
            mActualPlayer = player;
            if (player.equals(SpotifyPlayer.THIS_DEVICE_NAME)){
                int pos = getCurrentPosition();
                mPlayerDevice.disconnect();
                mPlayerDevice = new OfflinePlayerAndroid(this, mContext);
                playSong(mActualSong);
                setCurrentPosition(pos);
            }else{
                int pos = getCurrentPosition();
                pos = pos - 1500;
                if (pos < 0) pos = 0;
                mPlayerDevice.disconnect();
                mPlayerDevice = new OfflinePlayerSqueeze(this);
                mPlayerDevice.setActivePlayer(player);
                playSong(mActualSong);
                try
                {
                    Thread.sleep(2000);
                }
                catch(InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
                setCurrentPosition(pos);
            }
        }

    }

    @Override
    public boolean supportBeamToPiepser() {
        return OfflinePlayerSqueeze.Companion.isAvailable();
    }

    @Override
    public String getActivePlayerDevice() {
        return mActualPlayer;
    }

    @Override
    public void onSongCompletion( ) {
        mActState = Player.PLAYER_STATE_PAUSE;
        if (mActualPlayable != null) {
            mSettings.toFile(mActualPlayable.spotify_uri, mActualSong.getId());
            next();
        }
    }

    private PlayableItems getPlayableItemsFromFile(Context context){
        String json = readJSON(context, FILENAME_PLAYABLES);
        PlayableItems pis = new Gson().fromJson(json, PlayableItems.class);
        if (pis == null){
            pis = new PlayableItems();
        }
        PlayableItems pis2 = new PlayableItems();
        for(PlayableItemOffline pi: pis.mPlayableItems){
            pi.image = getBitmapFromString(pi.image_string);
            pi.player = this;
            if (!pi.spotify_uri.substring(0,6).equals("spotify")) pi.spotify_uri = "spotify:album:" + pi.spotify_uri;
            pi.mSongs = new ArrayList<>();
            if (isOfflineAvailable(pi, true)){
                pis2.mPlayableItems.add(pi);
            } else if (isOfflineAvailable(pi, false)){
                pis2.mPlayableItems.add(pi);
            }
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

    private boolean isOfflineAvailable(PlayableItemOffline pio, boolean searchWithArtist){
        boolean found = false;
        ContentResolver musicResolver = this.mContext.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection;
        String[] selectionArgs;

        if (searchWithArtist) {
            selection = MediaStore.Audio.Media.ALBUM + " = ? AND "
                    + MediaStore.Audio.Media.ARTIST + " = ?";
            selectionArgs = new String[]{pio.name, pio.artist};
        }else{
             selection = MediaStore.Audio.Media.ALBUM + " = ?";
            selectionArgs = new String[]{pio.name};
        }
        String sort_order = MediaStore.Audio.Media.TRACK + " ASC";

        //selectionArgs[1] = pio.artist;PlayerListenerPlaylists listener
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
                pio.mSongs.add(new Song(musicCursor.getLong(idColumn), musicCursor.getString(titleColumn), musicCursor.getString(artistColumn), musicCursor.getString(idColumn)));
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
    public int getCurrentPosition(){
        return mPlayerDevice.getCurrentPosition();
    }

    @Override
    public void setCurrentPosition(int currentPosition) {
        mPlayerDevice.setCurrentPosition(currentPosition);
    }

    @Override
    public boolean isPaused() {
        if(mActState.equals(PLAYER_STATE_PAUSE)) return true;
        return false;
    }

    public void getAlbumTracks( String uri, PlayerListenerAlbumTracks listener){
        for (Song s : mActualPlayable.mSongs){
            listener.success(s);
        }
    }

    @Override
    public PlayableItem getActPlayableItem(){
        return mActualPlayable;
    }

    @Override
    public void play_song(String uri){
        playSong(mActualPlayable.getSongFromUri(uri));
    }

    @Override
    public void onProgressChange(int progress) {
        listener.setTrackProgress(progress);
    }

    @Override
    public boolean isRemotePlayer() {
       return mPlayerDevice.isRemotePlayer();
    }


    @Override
    public void setRemoteMode(boolean remoteModeOn) {
        mPlayerDevice.setRemoteMode( remoteModeOn );
        mRemoteModeOn = remoteModeOn;
        if(remoteModeOn) {
            mActualPlayableItems.mPlayableItems.clear();
        }else{
            mActualPlayableItems.mPlayableItems.clear();
            mActualPlayableItems.mPlayableItems = mPItemsOffline.mPlayableItems;
        }

    }

    @Override
    public void onPlaylistItemChange(PlayableItemOffline pi) {
        mActualPlayableItems.mPlayableItems.add(pi);
        mPlaylistListener.success(pi.uri);
    }

    @Override
    public boolean isRemoteModeOn() {
        return mRemoteModeOn;
    }
}
