package com.example.idamusic_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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
      }

    @Override
    public void onSongSelect(String uri) {
        Log.d("SongSelect", uri);
        Intent data = new Intent();
        data.putExtra("Song_Uri", uri);
        setResult(RESULT_OK, data);
        super.finish();

    }
}





