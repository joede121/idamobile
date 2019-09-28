package com.example.idamusic_mobile;

import android.os.Parcelable;

import java.io.Serializable;

public class Song implements Serializable {
    private String title;
    private String artist;
    private long id;


    private String uri;

    public Song(long id, String title, String artist){
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public Song(long id, String title, String artist, String uri){
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.uri = uri;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getUri() {
        return uri;
    }










}
