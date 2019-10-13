package com.example.idamusic_mobile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import kotlin.concurrent.thread


class OfflinePlayerSqueeze(
        // Bedroom
        //final static String PLAYER_MAC = "00:04:20:22:7c:31";
        //final static String SERVER_URL = "http://192.168.2.6:9000/";

        internal var mListener: OfflinePlayerDev.OfflinePlayerDevListener) : OfflinePlayerDev() {

    internal var mActualServerUrl = SERVER_URL
    internal var mActualPlayerMac = PLAYER_MAC
    internal var mActualPlayer = SpotifyPlayer.PIEPSER_DEVICE_NAME
    internal var maActualState = Player.PLAYER_STATE_PAUSE
    internal var mActualPi: PlayableItemOffline? = null
    internal var mActualSong = Song(1, "1", "1")
    internal var mActualTime: Int? = 0
    internal var mActualDuration: Int? = 0
    internal var mPollServer: Runnable
    internal var mPollServerHandler: Handler
    internal var mLastServerUpdate: Long? = null
    internal var mPlayablesRemote = mutableListOf<DummyContent.DummyItem>()
    internal val REMOTE_PREFIX = "ida_"
    internal var mRemoteModeOn: Boolean = false



    init {
        mLastServerUpdate = 0
        mPollServerHandler = Handler()
        mPollServer = object : Runnable {
            override fun run() {
                getStatusPlayer()
                mPollServerHandler.postDelayed(this, 5000)
            }
        }

    }

    fun getRemotePlayablesFromSqueeze(context: Context) {
        //1.Get all Playlists
        /*Alle Playlists
                curl -X POST -H 'application/json;' -i 'http://192.168.2.8:9000/jsonrpc.js' --data '{"id":1,"method":"slim.request","params":["-",["playlists", "0", "99", "tags:u"]]}'

        Alle Songs zur Playlist inkl. Artwork
        curl -X POST -H 'application/json;' -i 'http://192.168.2.8:9000/jsonrpc.js' --data '{"id":1,"method":"slim.request","params":["74:da:38:5b:5d:08",["playlists", "tracks", "0", "1", "playlist_id:148", "tags:caulK"]]}'

        {"params":["74:da:38:5b:5d:08",["playlists","tracks","0","1","playlist_id:148","tags:caulK"]],"method":"slim.request","id":"1","result":{"__playlistTitle":"ida_0010773043","playlisttracks_loop":[{"playlist index":"0","id":"-84872632","title":"Eule findet den Beat (Titel-Song)","coverid":"-84872632","artist":"Eule","url":"spotify://track:3Ejl3cCvjzjSr9l5vyzVEM","album":"Eule findet den Beat - Die Songs","artwork_url":"https://i.scdn.co/image/94431166ba7cac8d7093295040b9c174729a38b1"}],"count":10}}
 */


            thread( start = true){
                var js = JSONArray();
                js.put("playlists")
                js.put("0")
                js.put("999")
                js.put("tags:u")
                val jPlaylists: JSONArray = (JSONObject(getCmdJsonRpcSync(js)).get("result") as JSONObject).get("playlists_loop") as JSONArray
                for( i in 0 until jPlaylists.length()){
                    val playlist = (jPlaylists.get(i) as JSONObject)
                    if((playlist.get("playlist") as String).startsWith(REMOTE_PREFIX)) {
                        Log.d("LOG_SQ_PLAYLIST", playlist.get("playlist") as String)
                        js = JSONArray()
                        js.put("playlists")
                        js.put("tracks")
                        js.put("0")
                        js.put("99")
                        js.put("playlist_id:" + playlist.get("id"))
                        js.put("tags:caulK")
                        val pi = PlayableItemOffline()
                        pi.uri = playlist.get("url").toString()
                        pi.mSongs = mutableListOf<Song>()
                        var jSong = JSONObject()
                        try {
                            val jSongs: JSONArray = (JSONObject(getCmdJsonRpcSync(js)).get("result") as JSONObject).get("playlisttracks_loop") as JSONArray
                            for (j in 0 until jSongs.length()) {
                                jSong = (jSongs.get(j) as JSONObject)
                                if(!jSong.isNull("album")) {
                                    pi.name = jSong.get("album").toString()
                                }else{
                                    pi.name = "Kein Album"
                                }
                                if(!jSong.isNull("artist")) {
                                    pi.artist = jSong.get("artist").toString()
                                }else{
                                    pi.artist ="Kein Artist"
                                }
                                pi.mSongs.add(Song(jSong.get("id").toString().toLong(),jSong.get("title").toString(), pi.artist, jSong.get("url").toString()))
                                Log.d("LOG_SQ_SONGS", jSong.get("title").toString())
                            }

                            if(!jSong.isNull("artwork_url")) {
                                pi.image = pi.getBitmapFromURL(jSong.get("artwork_url").toString())
                            }else{
                                pi.image = PlayableItem.drawableToBitmap(context.resources.getDrawable(R.drawable.ic_einhorn))
                            }
                            pi.isRemote = true
                            pi.spotify_uri = pi.uri
                            mListener.onPlaylistItemChange(pi)
                            mPlayablesRemote.add(DummyContent.DummyItem(i.toString(),"","",pi))
                        } catch (err: JSONException) {
                            Log.d("LOG_SQ_SONGS", err.toString())
                        }

                    }

                    }
                }
 }

    fun getStatusPlayer() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val client = OkHttpClient()

        client.newCall(getRequest(JSONArray().put("status"), mActualServerUrl, mActualPlayerMac)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("LOG_SQ_RESPONSE_ERROR", e.toString())
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                val res = response.body()!!.string()
                Log.d("LOG_SQ_RESPONSE", res)

                try {
                    mLastServerUpdate = System.currentTimeMillis()
                    val rs1 = JSONObject(res)
                    val rs = rs1.get("result") as JSONObject
                    if (mActualPi != null) {
                        val song = mActualPi!!.mSongs[(rs.get("playlist_cur_index") as String).toInt()]
                        var fl = rs.get("duration") as Double
                        val duration = Math.round(fl * 1000).toInt()
                        if (duration != mActualDuration) {
                            Handler(Looper.getMainLooper()).post(Runnable(){
                                fun run() {
                                    mListener.onDurationChange(duration)
                                }
                            })

                        }
                        mActualDuration = duration
                        if (rs.get("time") is Int) {
                            mActualTime = Math.round((rs.get("time") as Int * 1000).toFloat())
                        } else if (rs.get("time") is String) {
                            mActualTime = Math.round((rs.get("time") as String).toFloat() * 1000)
                        } else {
                            fl = rs.get("time") as Double
                            mActualTime = Math.round(fl * 1000).toInt()
                        }

                        Handler(Looper.getMainLooper()).post(Runnable(){
                            fun run() {
                                mListener.onProgressChange(mActualTime!!)
                            }
                        })


                        val mode = rs.get("mode") as String
                        Log.d("LOG_SQ_PLAYER_MODE", mode)

                        var cur_idx = (rs.get("playlist_cur_index") as String).toInt()
                        val ply_idx = rs.get("playlist_tracks")
                        cur_idx = cur_idx + 1


                        Log.d("LOG_SQ_RESPONSE", song.toString() + mActualDuration!!.toString() + mActualTime!!.toString())
                        if (mActualSong != song) {
                            mActualSong = song
                            mListener.onSongCompletion()
                            mActualSong = song
                        }
                        if (mode == "pause" && cur_idx == ply_idx) {
                            mListener.onSongCompletion()
                        }
                        if (mode == "pause" && maActualState != Player.PLAYER_STATE_PAUSE) {
                            Handler(Looper.getMainLooper()).post(Runnable(){
                                fun run() {
                                    mListener.onPlayerStateChange(Player.PLAYER_STATE_PAUSE)
                                }
                            })

                        } else if (mode == "play" && maActualState != Player.PLAYER_STATE_PLAY) {

                            Handler(Looper.getMainLooper()).post(Runnable(){
                                fun run() {
                                    mListener.onPlayerStateChange(Player.PLAYER_STATE_PLAY)
                                }
                            })

                        }
                        maActualState = Player.PLAYER_STATE_PLAY
                        if (mode != "play") {
                            mPollServerHandler.removeCallbacks(mPollServer)
                            maActualState = Player.PLAYER_STATE_PAUSE
                            Handler(Looper.getMainLooper()).post(Runnable(){
                                fun run() {
                                    mListener.onPlayerStateChange(Player.PLAYER_STATE_PAUSE)
                                    pause()
                                }
                            })
                        }
                    }

                } catch (err: JSONException) {
                    Log.d("Error", err.toString())
                }


            }

        })
    }

    override fun disconnect() {
        pause()
        try {
            Thread.sleep(2000)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }

    }


    private fun playAlbum(pi: PlayableItemOffline?, song_idx: Int?) {
        if (pi != null) {
            val json = JSONArray()
            if(!pi.isRemote) {
                //setCmdUrlApi("&p0=playlist&p1=loadalbum&p2=*&p3=*&p4=" + pi.name, mActualServerUrl);
                Log.d("LOG_SQ_CMD", "playalbum")
                json.put("playlist")
                json.put("loadalbum")
                json.put("*")
                json.put("*")
                json.put(pi.name)
            }else{
                json.put("playlist")
                json.put("play")
                json.put(pi.uri)
            }

            mActualPi = pi

            OkHttpClient().newCall(OfflinePlayerSqueeze.getRequest(json, mActualServerUrl, mActualPlayerMac)).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("LOG_SQ_RESPONSE_ERROR", e.toString())
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    playTrack(song_idx)
                    val res = response.body()!!.string()
                    Log.d("LOG_SQ_RESPONSE", res)
                }

            })

        }
    }

    private fun playTrack(number: Int?) {
        val cmd = JSONArray().put("playlist")
        cmd.put("index")
        cmd.put(number)
        OkHttpClient().newCall(OfflinePlayerSqueeze.getRequest(cmd, mActualServerUrl, mActualPlayerMac)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("LOG_SQ_RESPONSE_ERROR", e.toString())
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val res = response.body()!!.string()
                Log.d("LOG_SQ_RESPONSE", res)
                mPollServerHandler.post(mPollServer)

            }

        })
    }

    override fun playSong(song: Song, pi: PlayableItemOffline) {
        if (mActualPi == null) {
            playAlbum(pi, pi.getNumberOfSong(song))
            mActualTime = 0
        } else {
            if (pi != mActualPi) {
                playAlbum(pi, pi.getNumberOfSong(song))
                mActualTime = 0
            } else {
                if (mActualSong != song)
                    playTrack(pi.getNumberOfSong(song))
                mActualTime = 0
            }
        }
        mActualSong = song
    }

    override fun getDuration(): Int {
        return mActualDuration!!
    }

    override fun resume() {
        //setCmdUrlApi("&p0=play", mActualServerUrl);
        setCmdJsonRpc(JSONArray().put("play"))
        mPollServerHandler.post(mPollServer)
    }

    override fun pause() {
        //setCmdUrlApi("&p0=pause&p1=1", mActualServerUrl);
        val j = JSONArray().put("pause")
        j.put("1")
        setCmdJsonRpc(j)
        mPollServerHandler.removeCallbacks(mPollServer)
    }

    override fun getCurrentPosition(): Int {
        // getStatusPlayer();
        return mActualTime!! + (System.currentTimeMillis() - mLastServerUpdate!!).toInt()
    }

    override fun setCurrentPosition(currentPosition: Int) {
        mActualTime = currentPosition
        val db = currentPosition.toDouble() / 1000
        val ja = JSONArray()
        ja.put("time")
        ja.put(db)

        OkHttpClient().newCall(OfflinePlayerSqueeze.getRequest(ja, mActualServerUrl, mActualPlayerMac)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("LOG_SQ_RESPONSE_ERROR", e.toString())
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val res = response.body()!!.string()
                Log.d("LOG_SQ_RESPONSE", res)
            }

        })
    }

    override fun setActivePlayer(player: String) {
        Log.d("LOG_SQ_PLAYERS", mPlayers.toString())
        Log.d("LOG_SQ_PLAYER", player)
        for (pl in mPlayers) {
            if (pl.mName == player) {
                mActualPlayerMac = pl.mMac
                mActualServerUrl = pl.mServer_url
                mActualPlayer = pl.mName
            }
        }
    }

    fun setCmdJsonRpc(cmd: JSONArray) {
        Log.d("LOG_SQ_CMD", cmd.toString())
        OkHttpClient().newCall(OfflinePlayerSqueeze.getRequest(cmd, mActualServerUrl, mActualPlayerMac)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("LOG_SQ_RESPONSE_ERROR", e.toString())
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val res = response.body()!!.string()
                Log.d("LOG_SQ_RESPONSE", res)
            }

        })
    }

    fun getCmdJsonRpcSync(cmd: JSONArray) : String{
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        Log.d("LOG_SQ_CMD", cmd.toString())
        try {
            OkHttpClient().newCall(OfflinePlayerSqueeze.getRequest(cmd, mActualServerUrl, mActualPlayerMac)).execute().use {
                response ->
                return response.body()!!.string()
                Log.d("LOG_SQ_RESPONSE_SYNC", response.message() + response.isSuccessful) }
        } catch (io: IOException) {
            Log.d("LOG_SQ_RESP_SYNC_ERROR", io.toString())
            return ""
        }
        return ""
    }

    private fun getRequestURLApi(cmd: String, url: String): Request {
        val url_req = url + "status.html?player=" + mActualPlayerMac + cmd
        Log.d("LOG_SQ_RESPONSE_URL", url_req)
        return Request.Builder()
                .url(url_req)
                .get()
                .build()
    }

    private fun setCmdUrlApi(cmd: String, url: String) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val client = OkHttpClient()
        try {
            client.newCall(getRequestURLApi(cmd, url)).execute().use {
                response -> Log.d("LOG_SQ_RESPONSE_URL", response.message() + response.isSuccessful) }
        } catch (io: IOException) {
            Log.d("LOG_SQ_RESP_URL_ERROR", io.toString())
        }

    }


    override fun isRemotePlayer(): Boolean {
        Log.d("LOG_SQ_IS", mActualServerUrl + " " + SERVER_URL)
        return (mActualServerUrl == SERVER_URL || mActualServerUrl == SERVER_URL_ALT)
    }

    override fun setRemoteMode(remoteMode: Boolean) {
        mRemoteModeOn = remoteMode
    }

    override fun getPlayables(context: Context) {
        getRemotePlayablesFromSqueeze(context)
    }

    companion object {
        internal val SERVER_URL_2:String = "http://192.168.2.6:9000/"
        internal val SERVER_URL:String = "http://192.168.2.8:9000/"
        internal val SERVER_URL_ALT = "http://192.168.2.184:9000/"
        internal val PLAYER_MAC:String = "74:da:38:5b:5d:08"

        public val PLAYER_BEDROOM: String = "Bedroom musik"
        public val PLAYER_BATHROOM:String = "bathroommusik"
        public val PLAYER_LIVINGROOM:String = "Livingroom Touch"
        public var mPlayers: ArrayList<OfflinePlayerSqueezeDevice>
        public var mSqueezeServerUrls: ArrayList<String>

        init {
            mPlayers = ArrayList()
            mSqueezeServerUrls = ArrayList()
            mSqueezeServerUrls.add(SERVER_URL)

            mSqueezeServerUrls.add(SERVER_URL_2)
        }

        val allPlayer: ArrayList<String>
            get() {
                getPlayers()
                val players = ArrayList<String>()
                for (pl in mPlayers) {
                    players.add(pl.mName)
                }

                return players
            }

        val isAvailable: Boolean
            get() {

                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val client = OkHttpClient()

                try {
                    client.newCall(getRequest(JSONArray().put("status"), SERVER_URL, PLAYER_MAC)).execute().use { response ->
                        Log.d("LOG_SQ_RESPONSE", response.message() + response.isSuccessful + response.body()!!.string())
                        return if (response.isSuccessful) {
                            true
                        } else {
                            false
                        }

                    }
                } catch (io: IOException) {
                    Log.d("LOG_SQ_RESPONSE_ERROR", io.toString())
                    return false
                }

            }

        internal fun getPlayers() {

            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val client = OkHttpClient()
            mPlayers.clear()
            for (serverurl in mSqueezeServerUrls) {
                try {
                    client.newCall(getRequest(
                            JSONArray().put("serverstatus")
                                    .put("-")
                                    .put("-"), serverurl, PLAYER_MAC))
                            .execute().use { response ->
                                val res = response.body()!!.string()
                                Log.d("LOG_SQ_RESPONSE", response.message() + response.isSuccessful + res)
                                if (response.isSuccessful) {
                                    try {
                                        val jo = JSONObject(res).get("result") as JSONObject
                                        val jplayers = jo.get("other_players_loop") as JSONArray
                                        for (i in 0 until jplayers.length()) {
                                            val player = jplayers.get(i) as JSONObject
                                            mPlayers.add(
                                                    OfflinePlayerSqueezeDevice(
                                                            player.get("playerid") as String,
                                                            player.get("name") as String,
                                                            player.get("serverurl") as String))
                                        }

                                        Collections.sort(mPlayers, Comparator { first, second ->
                                            if (first.mName == SpotifyPlayer.PIEPSER_DEVICE_NAME) {
                                                return@Comparator -1
                                            } else if (second.mName == SpotifyPlayer.PIEPSER_DEVICE_NAME) {
                                                return@Comparator 1
                                            }
                                            first.mName.toLowerCase().compareTo(second.mName.toLowerCase())
                                        })

                                    } catch (err: JSONException) {
                                        Log.d("Error", err.toString())
                                    }

                                }

                            }
                } catch (io: IOException) {
                    Log.d("LOG_SQ_RESPONSE_ERROR", io.toString())
                }

            }
        }

        private fun getRequest(cmd: JSONArray, url: String, mac: String): Request {
            var jsonBody = ""
            try {
                jsonBody = JSONObject()
                        .put("id", 1)
                        .put("method", "slim.request")
                        .put("params", JSONArray().put(mac).put(cmd))
                        .toString()

            } catch (e: JSONException) {
            }

            return Request.Builder()
                    .url(url + "jsonrpc.js")
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody))
                    .build()
        }
    }

}
/*
Alle Playlists
curl -X POST -H 'application/json;' -i 'http://192.168.2.8:9000/jsonrpc.js' --data '{"id":1,"method":"slim.request","params":["-",["playlists", "0", "99", "tags:u"]]}'

Alle Songs zur Playlist inkl. Artwork
curl -X POST -H 'application/json;' -i 'http://192.168.2.8:9000/jsonrpc.js' --data '{"id":1,"method":"slim.request","params":["74:da:38:5b:5d:08",["playlists", "tracks", "0", "1", "playlist_id:148", "tags:caulK"]]}'

{"params":["74:da:38:5b:5d:08",["playlists","tracks","0","1","playlist_id:148","tags:caulK"]],"method":"slim.request","id":"1","result":{"__playlistTitle":"ida_0010773043","playlisttracks_loop":[{"playlist index":"0","id":"-84872632","title":"Eule findet den Beat (Titel-Song)","coverid":"-84872632","artist":"Eule","url":"spotify://track:3Ejl3cCvjzjSr9l5vyzVEM","album":"Eule findet den Beat - Die Songs","artwork_url":"https://i.scdn.co/image/94431166ba7cac8d7093295040b9c174729a38b1"}],"count":10}}⏎

Playlist Play (URL)
curl -X POST -H 'application/json;' -i 'http://192.168.2.8:9000/jsonrpc.js' --data '{"id":1,"method":"slim.request","params":["00:04:20:22:7c:31",["playlist", "play", "file:///home/pi/playlist/ida_0007168731.m3u"]]}'

Player Play/Stop
curl -X POST -H 'application/json;' -i 'http://192.168.2.8:9000/jsonrpc.js' --data '{"id":1,"method":"slim.request","params":["00:04:20:22:7c:31",["play"]]}'

<playerid> playlist index <index|+index|-index|?> <fadeInSecs>

The "playlist index" command sets or queries the song that is currently playing by index. When setting, a zero-based value may be used to indicate which song to play. An explicitly positive or negative number may be used to jump to a song relative to the currently playing song. The index can only be set if the playlist is not empty. If an index parameter is set then "fadeInSecs" may be passed to specify fade-in period. The value of the current song index may be obtained by passing in "?" as a parameter.

Examples:

    Request: "04:20:00:12:23:45 playlist index +1<LF>"
    Response: "04:20:00:12:23:45 playlist index +1<LF>"

    Request: "04:20:00:12:23:45 playlist index 5<LF>"
    Response: "04:20:00:12:23:45 playlist index 5<LF>"

    Request: "04:20:00:12:23:45 playlist index ?<LF>"
    Response: "04:20:00:12:23:45 playlist index 5<LF>"

<playerid> playlist loadalbum <genre> <artist> <album>

The "playlist loadalbum" command puts songs matching the specified genre artist and album criteria on the playlist. Songs previously in the playlist are discarded.

Examples:

    Request: "04:20:00:12:23:45 playlist loadalbum Rock Abba *<LF>"
    Response: "04:20:00:12:23:45 playlist loadalbum Rock Abba *<LF>"

 */