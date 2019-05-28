package com.example.idamusic_mobile;

public class Song {
    private String title;
    private String artist;
    private long id;

    public Song(long id, String title, String artist){
        this.id = id;
        this.title = title;
        this.artist = artist;
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







}
