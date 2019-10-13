package com.example.idamusic_mobile

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.ImageView

import com.google.gson.Gson

class SongSelectActivity : AppCompatActivity(), SongSelectActivityAdapter.selectSongListener {

    internal lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_item_song_list)
        mRecyclerView = findViewById<View>(R.id.listsongs) as RecyclerView
        val songs = Songs()
        songs.mSongs = Player.actualPlayer.actPlayableItem.mSongs
        mRecyclerView.adapter = SongSelectActivityAdapter(songs, this, Player.actualPlayer.actualTrackUri)
        val myToolbar = findViewById<View>(R.id.main_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
    }

    override fun onSongSelect(uri: String) {
        Log.d("SongSelect", uri)
        val data = Intent()
        data.putExtra("Song_Uri", uri)
        setResult(MainActivity.RESULT_OK, data)
        super.finish()
    }

    override fun onResume() {
        super.onResume()
        findViewById<View>(R.id.constraintLayoutNavSelect).setBackgroundColor(resources.getColor(R.color.colorPrimary))
        findViewById<View>(R.id.constraintLayoutNavPlay).setBackgroundColor(resources.getColor(R.color.colorPrimary))
        findViewById<View>(R.id.constraintLayoutNavSelectSong).background = resources.getDrawable(R.drawable.radius_bg)

        var iv = findViewById<ImageView>(R.id.imageViewNavSelect)
        iv.setColorFilter(resources.getColor(R.color.text))
        iv = findViewById(R.id.imageViewNavPlay)
        iv.setColorFilter(resources.getColor(R.color.text))
        iv = findViewById(R.id.imageViewNavSongSelect)
        iv.setColorFilter(resources.getColor(R.color.colorPrimary))

    }

    fun buttonClickSelect(view: View) {
        val intent = Intent(this, SelectionActivity::class.java)
        startActivityForResult(intent, MainActivity.INPUT_ACTIVITY_RESULT)
        super.finish()
    }

    fun buttonClickNavPlay(view: View) {
        super.finish()
    }

    fun buttonClickNavSongSelect(view: View) {

    }
}





