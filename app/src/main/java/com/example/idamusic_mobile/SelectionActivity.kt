package com.example.idamusic_mobile

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.example.idamusic_mobile.DummyContent.DummyItem

import android.util.Log
import android.view.View
import android.widget.ImageView

class SelectionActivity : AppCompatActivity(), ItemFragment.OnListFragmentInteractionListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: MyItemRecyclerViewAdapter
    internal var mFactor = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_item_list_2x2)

        val myToolbar = findViewById<View>(R.id.main_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        recyclerView = findViewById<View>(R.id.list) as RecyclerView

        mAdapter = MyItemRecyclerViewAdapter(DummyContent.ITEMS, this, windowManager.defaultDisplay)

        recyclerView!!.adapter = mAdapter
        for (item in DummyContent.ITEMS) {
            Log.d("MainActivity", "playable" + item.playable.artist)
        }

    }

    override fun onStart() {
        super.onStart()
        DummyContent.sort()

    }

    override fun onResume() {
        super.onResume()
        findViewById<View>(R.id.constraintLayoutNavSelect).background = resources.getDrawable(R.drawable.radius_bg)
        findViewById<View>(R.id.constraintLayoutNavPlay).setBackgroundColor(resources.getColor(R.color.colorPrimary))
        findViewById<View>(R.id.constraintLayoutNavSelectSong).setBackgroundColor(resources.getColor(R.color.colorPrimary))

        var iv = findViewById<ImageView>(R.id.imageViewNavSelect)
        iv.setColorFilter(resources.getColor(R.color.colorPrimary))
        iv = findViewById(R.id.imageViewNavPlay)
        iv.setColorFilter(resources.getColor(R.color.text))
        iv = findViewById(R.id.imageViewNavSongSelect)
        iv.setColorFilter(resources.getColor(R.color.text))

    }

    override fun onListFragmentInteraction(item: DummyItem) {
        Log.d("MainActivity", "auswahl" + item.playable.artist + item.playable.name + item.id + item.toString())
        val data = Intent()
        data.putExtra("PlayableID", item.id)
        setResult(MainActivity.RESULT_OK, data)
        super.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    private fun changeLayout() {
        var factor = 1
        if (mFactor == 1) factor = 2
        mAdapter!!.changeLayout(factor)
        mFactor = factor
        val context = recyclerView!!.context
        recyclerView!!.layoutManager = GridLayoutManager(context, factor)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("Menu", item.itemId.toString() + "")
        when (item.itemId) {
            R.id.action_refresh -> DummyContent.refresh()
            R.id.action_change_layout -> changeLayout()
            android.R.id.home -> super.finish()
        }


        return super.onOptionsItemSelected(item) //To change body of generated methods, choose Tools | Templates.
    }


    fun buttonClickSelect(view: View) {
        // do nothing
    }

    fun buttonClickNavPlay(view: View) {
        super.finish()
    }

    fun buttonClickNavSongSelect(view: View) {
        val intent = Intent(this, SongSelectActivity::class.java)
        startActivityForResult(intent, MainActivity.INPUT_ACTIVITY_RESULT)
        super.finish()
    }
}