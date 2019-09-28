package com.example.idamusic_mobile;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.idamusic_mobile.DummyContent.DummyItem;

import android.util.Log;

public class SelectionActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener {
    private RecyclerView recyclerView;
    private MyItemRecyclerViewAdapter mAdapter;
    int mFactor = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_item_list_2x2);
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

        switch (item.getItemId()) {
            case R.id.action_refresh:
                DummyContent.refresh();
                break;
            case R.id.action_back:
                super.finish();
                break;
            case R.id.action_change_layout:
                changeLayout();
                break;

        }


        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
    }

}