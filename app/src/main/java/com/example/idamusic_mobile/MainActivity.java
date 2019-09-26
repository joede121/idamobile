package com.example.idamusic_mobile;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import android.util.Log;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PlayerListener, PopupMenu.OnMenuItemClickListener {
    private String uri = "";
    private ImageView imStop;
    private Player player;
    public static final int INPUT_ACTIVITY_RESULT = 9999;
    private boolean mShowBeamOption = false;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 9998;
    String mMode = Player.MODE_ONLINE;


    public void onPlayerStateChange( String player_state){
        if (player_state.equals(Player.PLAYER_STATE_PLAY)) {
            imStop.setForeground(getResources().getDrawable(R.drawable.btn_pause));
        } else {
            imStop.setForeground(getResources().getDrawable(R.drawable.btn_play));
        }
    }

    public void onTrackChange( String track_name, String uri){
        setTextTrack(track_name);
        this.uri = uri;
    }

    public void onAlbumCoverChange( Bitmap cover ){
       setTrackImage( cover );

    }

    public void onConnected(){
         DummyContent.refresh(player);
         this.mShowBeamOption = player.supportBeamToPiepser();
         this.invalidateOptionsMenu();
    }

    public void onConnectedError(){
        if (mMode.equals(Player.MODE_ONLINE))
            goOffline();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique
        }


        player = Player.factory(this, this, mMode);
        player.connect();
        //btn = findViewById(R.id.button);
        imStop = findViewById(R.id.imageView);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!player.isConnected()) player.connect();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        player.onActivityResult(requestCode, resultCode, data);

        if (INPUT_ACTIVITY_RESULT == requestCode) {
            if (resultCode == RESULT_OK) {
                int id = Integer.parseInt(data.getStringExtra("PlayableID"));
                id--;
                String uri = DummyContent.ITEMS.get(id).playable.spotify_uri;
                spotPlay(uri);
                Log.d("MainActivity", id + "Auswal main " + this.uri);
                this.uri = uri;
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.pause();
        player.disconnect();
    }

    public void setURI( String uri ){
        this.uri = uri;
    }



    public void spotPlay(String uri) {
        Log.d("MainActivity", "URI NEU" + uri + "URI ALT" + player.getActualAlbum());
        // Play a playlist
        if (uri != "") {
            if (!uri.equals(player.getActualAlbum())) {
                player.play(uri);
            } else {
                spotResume();
                Log.d("MainActivity", "RESUME");
            }
        } else {
            openSelectActicity();
        }
    }

    public void setTrackImage( Bitmap image ){
        ImageView img = findViewById(R.id.imageViewArt);
        img.setImageBitmap(image);
    }

    public void setTextTrack(String track) {
        TextView editText = findViewById(R.id.textView2);
        Log.d("TextTrack", "RESUME" + track);
        editText.setText(track);

    }


    public void spotPause() {
        // Play a playlist
        player.pause();
    }

    public void spotResume() {
        // Play a playlist
        player.resume();
    }

    public void onClickNext(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        player.next();
    }

    public void onClickPrev(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        player.prev();

    }

    public void buttonOnClick(View view) {

        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        if (player.isPaused()) {
            if (uri != "") {
                spotResume();
            } else {
                openSelectActicity();
            }
        } else if (!player.isPaused()) {
            spotPause();
        }
    }

    void openSelectActicity() {
        Intent intent = new Intent(this, SelectionActivity.class);
        startActivityForResult(intent, INPUT_ACTIVITY_RESULT);
    }

    public void buttonClickSelect(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        openSelectActicity();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_start, menu);
        menu.findItem(R.id.action_beam).setVisible(mShowBeamOption);
        //menu.findItem(R.id.action_select_beam).setVisible(mShowBeamOption);
        if(mMode.equals(Player.MODE_ONLINE)){
            menu.findItem(R.id.action_on).setTitle("Online");
            menu.findItem(R.id.action_on).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_wifi));
        }else{
            menu.findItem(R.id.action_on).setTitle("Offline");
            menu.findItem(R.id.action_on).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_wifi_off));
        }
        Log.d("Menu", new Boolean(mShowBeamOption).toString());
        return super.onCreateOptionsMenu(menu);
    }

    void goOnline(){
        player.disconnect();
        player = Player.factory(this, this, Player.MODE_ONLINE);
        mMode = Player.MODE_ONLINE;
        player.connect();
    }

    void goOffline(){
        player.disconnect();
        player = Player.factory(this, this, Player.MODE_OFFLINE);
        new AsyncWaitConnect().execute();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!player.isConnected()){
                    player.connect();
                }else{
                    this.onConnected();
                    // DummyContent.refresh();
                }
                break;
            case R.id.action_back:
                super.finish();
                break;
            case R.id.action_beam:
                if(!player.getActivePlayerDevice().equals(SpotifyPlayer.PIEPSER_DEVICE_NAME)) {
                    player.setPiepserAsActivePlayer();
//                    if (player.getActivePlayerDevice().equals(SpotifyPlayer.PIEPSER_DEVICE_NAME))
                        item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cast_connected));
                }else{
                    player.setActivePlayer(SpotifyPlayer.THIS_DEVICE_NAME);
//                    if (player.getActivePlayerDevice().equals(SpotifyPlayer.THIS_DEVICE_NAME))
                        item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cast));
                }
                break;
            case R.id.action_select_beam:
                showPopup(findViewById(R.id.action_select_beam));
                break;
            case R.id.action_on:
                if (mMode.equals(Player.MODE_ONLINE)){
                    goOffline();
                }else{
                    goOnline();
                }

        }


        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(MainActivity.this);
        inflater.inflate(R.menu.menu_start_popup_players, popup.getMenu());
        int i = 0;
        if(mMode.equals(Player.MODE_ONLINE)) {
            SubMenu sub = popup.getMenu().addSubMenu("Beamen nach...");
            i = 5000;
            for (String player : player.getAllPlayers()) {
                sub.add(0, i++, 0, player);
            }
            popup.getMenu().add(0, 9999, 0, "Offline speichern");

        }
        popup.show();
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        switch(id){

            case 9999:
                // Offline
                new AsyncTaskOffline().execute();
                return true;

            default:
                CharSequence player_string = item.getTitle();
                Log.d("MenuItemClick", player_string.toString());
                player.setActivePlayer(player_string.toString());
                return true;
        }

    }

private class AsyncWaitConnect extends AsyncTask<String, String, String>{

    private String resp;
    ProgressDialog progressDialog;


    @Override
    protected String doInBackground(String... params) {
        publishProgress("Sleeping..."); // Calls onProgressUpdate()
        try {
            int time = 3*1000;
            Thread.sleep(time);
            resp = "Slept for " + params[0] + " seconds";
        } catch (InterruptedException e) {
            e.printStackTrace();
            resp = e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            resp = e.getMessage();
        }
        return resp;
    }

    @Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation

        mMode = Player.MODE_OFFLINE;
        player.connect();
        progressDialog.dismiss();
    }


    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(MainActivity.this,
                "Bitte warten...",
                "...bald fertig");
    }
}


    private class AsyncTaskOffline extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;


        @Override
        protected String doInBackground(String... strings) {
            List<PlayableItem> pis = new ArrayList<>();
            for(DummyContent.DummyItem dummyItem: DummyContent.ITEMS){
                pis.add(dummyItem.playable);
            }
            OfflinePlayer.makeOffline(pis, getApplicationContext());
            return "Sucess";
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Bitte warten...",
                    "...bald fertig");
        }
    }



}