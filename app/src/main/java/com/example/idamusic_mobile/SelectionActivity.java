package com.example.idamusic_mobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.idamusic_mobile.DummyContent.DummyItem;

import android.util.Log;

public class SelectionActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_item_list);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        mAdapter = new MyItemRecyclerViewAdapter(DummyContent.ITEMS, this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                DummyContent.refresh();
                break;
            case R.id.action_back:
                super.finish();
                break;
        }


        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
    }

}