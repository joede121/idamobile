package com.example.idamusic_mobile;

import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;




public class SpotifyPlayer extends Player {
    private static final String CLIENT_ID = "e7f95f2ddd144617b3afafa12d8089ae";
    private static final String REDIRECT_URI = "http://com.yourdomain.yourapp/callback";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    public static final int AUTH_CODE_REQUEST_CODE = 0x11;
    public static final String THIS_DEVICE_NAME = "LG-H815";
    public static final String PIEPSER_DEVICE_NAME = "idamusik";
//    private static final String PIEPSER_DEVICE_NAME = "Livingroom Touch";

    private static final String FILENAME_SETTING = "SETTING_ONLINE";
    SettingsParcelable mSetting;
    boolean mOverwriteSetting;


    private SpotifyAppRemote mSpotifyAppRemote;
    private MainActivity activity;
    private PlayerState playerstate;

    private String mAccessToken;

    SpotifyDevice mSelectedDevice;

    public SpotifyPlayer(MainActivity activity, PlayerListener listener) {
        this.activity = activity;
        this.listener = listener;
        mSetting = new SettingsParcelable(activity.getApplicationContext(), FILENAME_SETTING);
    }

    void setPlayerstate(PlayerState playerstate) {
        this.playerstate = playerstate;
    }

    @Override
    public void connect() {
        mOverwriteSetting = false;
        // We will start writing our code here.
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(activity, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");
                        mSpotifyAppRemote.getPlayerApi()
                                .subscribeToPlayerState()
                                .setEventCallback(playerState -> {
                                    final Track track = playerState.track;
                                    setPlayerstate(playerState);
                                    if (track != null) {
                                        onTrackChange(track);
  //                                      if (listener != null ) listener.onTrackChange(track.name + " " + track.album, track.uri);
                                        // Get image from track
                                        mSpotifyAppRemote.getImagesApi()
                                                .getImage(track.imageUri, Image.Dimension.LARGE)
                                                .setResultCallback(bitmap -> {
                                                    if (listener != null ) listener.onAlbumCoverChange ( bitmap );
                                                });
                                    }
                                    if (playerstate.isPaused) {
                                        if (listener != null ) listener.onPlayerStateChange(Player.PLAYER_STATE_PAUSE);
                                    } else if (!playerstate.isPaused) {
                                        if (listener != null ) listener.onPlayerStateChange(Player.PLAYER_STATE_PLAY);
                                    }

                                });



                        // Now you can start interacting with App Remote
                        //connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d("Error Connected", "Error passier" + throwable.getMessage());
                        listener.onConnectedError();

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });


        if (mAccessToken == null) {
            final AuthenticationRequest request = getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);
            AuthenticationClient.openLoginActivity(this.activity, AUTH_TOKEN_REQUEST_CODE, request);
        }



    }
//, "user-modify-playback-state"
    private AuthenticationRequest getAuthenticationRequest(AuthenticationResponse.Type type) {
        return new AuthenticationRequest.Builder(CLIENT_ID, type, "http://com.yourdomain.yourapp/callback")
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email", "playlist-read-private", "user-read-playback-state", "user-modify-playback-state","user-follow-read"})
                .setCampaign("your-campaign-token")
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
        Log.d("MainActivity", "result " + requestCode + requestCode);

        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();

            Log.d("MainActivity", "Access Token " + mAccessToken);
            Log.d("MainActivity", "Resonse Error " + response.getError());

           if (response.getError()!=null){
                listener.onConnectedError();
                Log.d("MainActivity", "Access Token ERROR " + mAccessToken);
            }else {
                if (listener != null) listener.onConnected();
                Log.d("MainActivity", "Access Token OK " + mAccessToken);
            }

        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            Log.d("MainActivity", "Req Token " + mAccessToken);
        }
    }

    void onTrackChange(Track track){
        if (!mOverwriteSetting){
            if(!getActualAlbum().equals(mSetting.online_uri_playable)){
                play(mSetting.online_uri_playable);
                pause();
            }
            mOverwriteSetting = true;
        }
        mSetting.toFile(getActualAlbum());

        this.listener.onTrackChange( track.name, track.uri);

    }

    @Override
    public void disconnect(){
        if(isConnected()) {
            pause();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        }
    }

    @Override
    public String getActualTrackUri() {
        return playerstate.track.uri;
    }

    @Override
    public boolean isPaused() {
        return playerstate.isPaused;
    }

    @Override
    public void play( String uri){
        mSpotifyAppRemote.getPlayerApi().play( uri );
        // Check if correct player is used
        if (isActiceDeviceOK()) {
            if (mSelectedDevice == null) {
                setActiveDevice(THIS_DEVICE_NAME);
            } else {
                setActiveDevice(mSelectedDevice.name);
            }
        }else{
            setActiveDevice(THIS_DEVICE_NAME);
        }
    }

    @Override
    public void resume() {
         mSpotifyAppRemote.getPlayerApi().resume();
        if (isActiceDeviceOK()) {
            if (mSelectedDevice == null) {
                setActiveDevice(THIS_DEVICE_NAME);
            } else {
                setActiveDevice(mSelectedDevice.name);
            }
        }else{
            setActiveDevice(THIS_DEVICE_NAME);
        }
    }

    @Override
    public void next() {
        mSpotifyAppRemote.getPlayerApi().skipNext();
    }

    @Override
    public void prev() {
        mSpotifyAppRemote.getPlayerApi().skipPrevious();
    }

    @Override
    public void pause() {
        mSpotifyAppRemote.getPlayerApi().pause();
    }

    @Override
    public void getPlayableItems(String prefix, PlayerListenerPlaylists listener) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(mAccessToken);
        SpotifyService spotify = api.getService();
        List<PlayableItem> PlayableItems = new ArrayList<>();
        // PlayableItems = null;
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.LIMIT, 50);
        options.put(SpotifyService.OFFSET, 0);

        spotify.getMyPlaylists(options, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                for (PlaylistSimple playlist : playlistSimplePager.items) {
                    if (playlist.name.startsWith(prefix)) {
                        spotify.getPlaylistTracks(playlist.owner.id, playlist.id, new Callback<Pager<PlaylistTrack>>() {
                            @Override
                            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                                listener.success(playlistTrackPager.items.get(0).track.album.id);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d("MainActivity", "Error Fetch Playlist");
                                listener.error();
                            }
                        });

                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void getAlbum(String uri, PlayerListenerAlbum listenertimo) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(mAccessToken);
        SpotifyService spotify = api.getService();
        spotify.getAlbum(uri, new Callback<Album>() {
            @Override
            public void success(Album album, Response response) {
                if (listenertimo != null)
                    listenertimo.success(album.name,album.artists.get(0).name,getBitmapFromURL(album.images.get(0).url));
                Log.d("Album success", album.name + album.images.get(0).url);
            }

            @Override
            public void failure(RetrofitError error) {
                error_cnt++;
                Log.d("Album failure", error.toString());
                if (error_cnt < ERROR_CNT_LIMIT)
                    getAlbum(uri, listenertimo);
            }
        });
    }


    boolean isActiceDeviceOK() {
        SpotifyDevice act = getActiveDevice();
        if (act != null) {
            if (act.name.equals(THIS_DEVICE_NAME)) return true;
            if(mSelectedDevice != null) {
                if (act.name.equals(mSelectedDevice.name)) return true;
            }
        }
    return false;
    }


    SpotifyDevice getActiveDevice(){
        SpotifyDevices devs = getDevices();
        if( devs != null) return devs.getActiveDevice();
        return null;
    }

    SpotifyDevices getDevices(){
        try {
            String players = SpotifyWebApi.getPlayers(mAccessToken);
            Log.d("Players", players);
            Gson gson = new Gson();
            SpotifyDevices mSpotifyDevices = gson.fromJson(players, SpotifyDevices.class);
            if (mSpotifyDevices.devices == null) return null;
                for (SpotifyDevice spot_device : mSpotifyDevices.devices) {
                    Log.d("Player_Object", spot_device.id + spot_device.name + spot_device.is_active);
                }
                return mSpotifyDevices;
            }
        catch(IOException error){
                Log.d("PlayersError", error.toString());
                return null;
            }
        }

    void setActiveDevice(String name){
        try {
            SpotifyDevice dev = getDevices().getDeviceByName(name);
            SpotifyDevice act = getDevices().getActiveDevice();

            if(dev != null && act != null){
                Log.d("PlayerSet", dev.name + act.name);
                if(!dev.id.equals(act.id)) SpotifyWebApi.setActivePlayer(dev.id, mAccessToken);
            }else {
                if (dev != null) SpotifyWebApi.setActivePlayer(dev.id, mAccessToken);
            }
            if( dev != null ) mSelectedDevice = dev;

        }
        catch(IOException error){
            Log.d("PlayerssetError", error.toString());
        }
    }


    @Override
    public String getActualAlbum(){
        return this.playerstate.track.album.uri;
    }

    @Override
    public boolean isConnected(){
        if (mSpotifyAppRemote != null) {
            return mSpotifyAppRemote.isConnected();
        }else{
            return false;
        }
    }

   @Override
   public boolean supportBeamToPiepser(){
        if (!isConnected()) return false;
        SpotifyDevices devs = getDevices();
        if(devs == null) return false;
        SpotifyDevice dev = getDevices().getDeviceByName(PIEPSER_DEVICE_NAME);
        if(dev != null) return true;
        return false;
    }

    @Override
    public void setPiepserAsActivePlayer() {
        setActiveDevice(PIEPSER_DEVICE_NAME);
    }

    @Override
    public List<String> getAllPlayers() {
        List<String> players = new ArrayList<>();
        SpotifyDevices devs = getDevices();
        if(devs.devices == null) return players;
        for(SpotifyDevice dev: devs.devices){
            players.add(dev.name);
        }
        return players;

    }

    @Override
    public void setActivePlayer(String player) {
        setActiveDevice(player);
    }

    @Override
    public String getActivePlayerDevice(){
        return getActiveDevice().name;

    }
}

