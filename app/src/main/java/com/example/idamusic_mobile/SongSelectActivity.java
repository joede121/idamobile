package com.example.idamusic_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;

public class SongSelectActivity extends AppCompatActivity implements SongSelectActivityAdapter.selectSongListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_item_song_list);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.listsongs);
        String json = getIntent().getStringExtra("Songs");
        Songs songs = new Gson().fromJson(json, Songs.class);
        recyclerView.setAdapter(new SongSelectActivityAdapter(songs, this));
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
      }

    @Override
    public void onSongSelect(String uri) {
        Log.d("SongSelect", uri);
        Intent data = new Intent();
        data.putExtra("Song_Uri", uri);
        setResult(RESULT_OK, data);
        super.finish();

    }

    @Override
    protected void onResume() {
        super.onResume();;
        findViewById(R.id.constraintLayoutNavSelect).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        findViewById(R.id.constraintLayoutNavPlay).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        findViewById(R.id.constraintLayoutNavSelectSong).setBackground(getResources().getDrawable(R.drawable.radius_bg));

        ImageView iv = findViewById(R.id.imageViewNavSelect);
        iv.setColorFilter(getResources().getColor(R.color.text));
        iv = findViewById(R.id.imageViewNavPlay);
        iv.setColorFilter(getResources().getColor(R.color.text));
        iv = findViewById(R.id.imageViewNavSongSelect);
        iv.setColorFilter(getResources().getColor(R.color.colorPrimary));

    }

    public void buttonClickSelect(View view) {
        // do nothing
    }

    public void buttonClickNavPlay(View view) {
        super.finish();
    }

    public void  buttonClickNavSongSelect(View view){

    }
}





