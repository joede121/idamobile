package com.example.idamusic_mobile


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView

import com.google.gson.Gson

class PlayerSelectActivity : AppCompatActivity(), PlayerSelectActivityAdapter.selectPlayerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_item_players_list)
        val recyclerView = findViewById<View>(R.id.listplayers) as RecyclerView
        recyclerView.adapter = PlayerSelectActivityAdapter(intent.getStringArrayListExtra("players"),
                this,
                intent.getStringExtra("actplayer"))
        setSupportActionBar(findViewById<View>(R.id.main_toolbar) as Toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPlayerSelect(uri: String) {
        Log.d("PlayerSelect", uri)
        val data = Intent()
        data.putExtra("player", uri)
        setResult(MainActivity.RESULT_OK, data)
        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("Menu", item.itemId.toString() + "")
        when (item.itemId) {
            android.R.id.home -> super.finish()
        }
        return super.onOptionsItemSelected(item) //To change body of generated methods, choose Tools | Templates.
    }

    override fun onResume() {
        super.onResume()
    }

    fun buttonClickSelect(view: View) {
        // do nothing
    }

    fun buttonClickNavPlay(view: View) {
        super.finish()
    }

    fun buttonClickNavSongSelect(view: View) {

    }
}





