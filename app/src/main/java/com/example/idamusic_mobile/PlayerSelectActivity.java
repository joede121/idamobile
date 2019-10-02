package com.example.idamusic_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;

public class PlayerSelectActivity extends AppCompatActivity implements PlayerSelectActivityAdapter.selectPlayerListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_item_players_list);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.listplayers);
        recyclerView.setAdapter(
                new PlayerSelectActivityAdapter(getIntent().getStringArrayListExtra("players"),
                                        this,
                                                getIntent().getStringExtra("actplayer")));
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }

    @Override
    public void onPlayerSelect(String uri) {
        Log.d("PlayerSelect", uri);
        Intent data = new Intent();
        data.putExtra("player", uri);
        setResult(RESULT_OK, data);
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Menu", item.getItemId() + "");
        switch (item.getItemId()) {
            case android.R.id.home:
                super.finish();
                break;
        }
        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
    }


        @Override
    protected void onResume() {
        super.onResume();;
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





