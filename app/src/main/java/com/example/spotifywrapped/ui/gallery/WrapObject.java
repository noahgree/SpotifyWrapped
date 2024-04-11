package com.example.spotifywrapped.ui.gallery;
import java.util.UUID;

//This is for any event created. Subclasses include: Class, Exam, Assignment, & Other
//TODO Subclass implementation if needed. Also need to look at what object is best at storing time
public class WrapObject{
    private String name;
    private String artistImage;

    public String getArtistImage() {
        return artistImage;
    }

    public String getSongImage() {
        return songImage;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSongName() {
        return songName;
    }

    private String songImage;
    private String artistName;
    private String songName;
    private int num;

    public WrapObject(int num, String name, String artistImage, String songImage, String artistName, String songName) {
        this.name = name;
        this.artistImage = artistImage;
        this.songImage = songImage;
        this.artistName = artistName;
        this.songName = songName;
        this.num = num;
    }


    public int getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
