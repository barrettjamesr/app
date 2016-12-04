package com.smartphoneappdev.wcd.alienalbum;

import java.util.Date;

/**
 * Created by JRB on 3/12/2016.
 */

public class AlienVideo {

    private Date dateUploaded;
    private Date lastPlayed;

    private int userID;
    private String userName;
    private String comment;

    private String thumbnailRef;
    private String videoRef;

    private String genre;
    private String tags;

    private int length;
    private boolean favourite;
    private boolean privacy;


    public AlienVideo (Date dateUploaded, Date lastPlayed, int userID, String userName, String comment, String thumbnailRef, String videoRef, String genre, String tags, int length, boolean favourite,  boolean privacy){
        this.dateUploaded = dateUploaded;
        this.lastPlayed = lastPlayed;
        this.userID = userID;
        this.userName = userName;
        this.comment = comment;
        this.thumbnailRef = thumbnailRef;
        this.videoRef = videoRef;
        this.genre = genre;
        this.tags = tags;
        this.length = length;
        this.favourite = favourite;
        this.privacy = privacy;

    }

    public Date getDateUploaded(){
        return this.dateUploaded;
    }
    public Date getLastPlayed(){
        return this.lastPlayed;
    }
    public int getUserID(){
        return this.userID;
    }
    public String getUserName(){
        return this.userName;
    }
    public String getComment(){
        return this.comment;
    }
    public String getThumbnailRef(){
        return this.thumbnailRef;
    }
    public String getVideoRef(){
        return this.videoRef;
    }
    public String getGenre(){
        return this.genre;
    }
    public String getTags(){
        return this.tags;
    }
    public int getLength(){
        return this.length;
    }
    public boolean getIsFavourite(){
        return this.favourite;
    }
    public boolean getIsPrivate(){
        return this.privacy;
    }
}
