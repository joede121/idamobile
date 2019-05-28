package com.example.idamusic_mobile;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class SettingsParcelable {

    public String offline_uri_playable = "";
    public long offline_id_song = 0b0;
    transient Context mContext;
    transient static final String FILENAME_SETTINGS = "SETTINGS";

    public SettingsParcelable(Context context){
        mContext = context;
        String json = OfflinePlayer.readJSON(mContext, FILENAME_SETTINGS);
        if( json != null ) {
            Log.d("OfflineSettingsImport", "JSON" + json);
            SettingsParcelable set = new Gson().fromJson(json, SettingsParcelable.class);
            this.offline_id_song = set.offline_id_song;
            this.offline_uri_playable = set.offline_uri_playable;
        }

    }

    public void toFile(String uri, Long id){
        offline_uri_playable = uri;
        offline_id_song = id;
        String json = new Gson().toJson(this);
        Log.d("OfflineSettingsExport", "JSON" + json);
        OfflinePlayer.writeJSON(json, mContext, FILENAME_SETTINGS);
    }

}


