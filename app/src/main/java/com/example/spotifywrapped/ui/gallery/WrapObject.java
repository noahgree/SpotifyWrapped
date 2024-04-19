package com.example.spotifywrapped.ui.gallery;
import java.util.Date;
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

    public String getUsername() {
        return username;
    }

    public String getTimeFrame() {
        return timeFrame;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getAlsoPublic() {
        return alsoPublic;
    }

    private String songImage;
    private String artistName;
    private String songName;
    private String timeFrame;
    private String username;
    private String creationDate;
    private String alsoPublic;
    private int num;

    private boolean publicWrap;

    public WrapObject(int num, String name, String artistImage, String songImage, String artistName, String songName, String timeFrame, String username, String creationDate, String alsoPublic) {
        this.name = name;
        this.artistImage = artistImage;
        this.songImage = songImage;
        this.artistName = artistName;
        this.songName = songName;
        this.num = num;
        if (timeFrame.equals("short")) {
            this.timeFrame = "4 Weeks";
        } else if (timeFrame.equals("medium")) {
            this.timeFrame = "6 Months";
        } else {
            this.timeFrame = "1 Year";
        }
        this.username = username;
        this.creationDate = creationDate;
        this.alsoPublic = alsoPublic;
        this.publicWrap = false;
    }

    public boolean isPublicWrap() {
        return publicWrap;
    }

    public void setPublicWrap(boolean publicWrap) {
        this.publicWrap = publicWrap;
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
