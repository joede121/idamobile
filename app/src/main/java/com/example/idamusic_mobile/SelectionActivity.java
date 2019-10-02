package com.example.idamusic_mobile;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.idamusic_mobile.DummyContent.DummyItem;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class SelectionActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener {
    private RecyclerView recyclerView;
    private MyItemRecyclerViewAdapter mAdapter;
    int mFactor = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_item_list_2x2);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);


        mAdapter = new MyItemRecyclerViewAdapter(DummyContent.ITEMS, this, getWindowManager().getDefaultDisplay());
        recyclerView.setAdapter(mAdapter);
        for (DummyItem item : DummyContent.ITEMS) {
            Log.d("MainActivity", "playable" + item.playable.artist);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        DummyContent.sort();

    }

    @Override
    protected void onResume() {
        super.onResume();;
        findViewById(R.id.constraintLayoutNavSelect).setBackground(getResources().getDrawable(R.drawable.radius_bg));
        ;
        findViewById(R.id.constraintLayoutNavPlay).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        findViewById(R.id.constraintLayoutNavSelectSong).setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        ImageView iv = findViewById(R.id.imageViewNavSelect);
        iv.setColorFilter(getResources().getColor(R.color.colorPrimary));
        iv = findViewById(R.id.imageViewNavPlay);
        iv.setColorFilter(getResources().getColor(R.color.text));
        iv = findViewById(R.id.imageViewNavSongSelect);
        iv.setColorFilter(getResources().getColor(R.color.text));

    }

    @Override
    public void onListFragmentInteraction(DummyItem item) {
        Log.d("MainActivity", "auswahl" + item.playable.artist + item.playable.name + item.id + item.toString());
        Intent data = new Intent();
        data.putExtra("PlayableID", item.id);
        setResult(RESULT_OK, data);
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void changeLayout(){
        int factor = 1;
        if (mFactor == 1) factor = 2;
        mAdapter.changeLayout(factor);
        mFactor = factor;
        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new GridLayoutManager(context, factor));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Menu", item.getItemId() + "");
        switch (item.getItemId()) {
            case R.id.action_refresh:
                DummyContent.refresh();
                break;
            case R.id.action_change_layout:
                changeLayout();
                break;
            case android.R.id.home:
                super.finish();
                break;


        }


        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
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