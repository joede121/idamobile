package com.example.idamusic_mobile;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class SettingsParcelable {

    public String offline_uri_playable = "";
    public long offline_id_song = 0b0;
    public String online_uri_playable = "";

    transient Context mContext;
    transient String mFilename;

    public SettingsParcelable(Context context, String filename){
        mContext = context;
        mFilename = filename;
        String json = OfflinePlayer.readJSON(mContext, filename);
        if( json != null ) {
            Log.d("OfflineSettingsImport", "JSON" + json);
            SettingsParcelable set = new Gson().fromJson(json, SettingsParcelable.class);
            this.offline_id_song = set.offline_id_song;
            this.offline_uri_playable = set.offline_uri_playable;
            this.online_uri_playable = set.online_uri_playable;

        }

    }

    public void toFile(String uri, Long id){
        offline_uri_playable = uri;
        offline_id_song = id;
        String json = new Gson().toJson(this);
        Log.d("OfflineSettingsExport", "JSON" + json);
        OfflinePlayer.writeJSON(json, mContext, mFilename);
    }

    public void toFile(String uri){
        online_uri_playable = uri;
        String json = new Gson().toJson(this);
        Log.d("OfflineSettingsExport", "JSON" + json);
        OfflinePlayer.writeJSON(json, mContext, mFilename);
    }



}


