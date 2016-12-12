package com.smartphoneappdev.wcd.alienalbum;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JRB on 3/12/2016.
 * implemented as parcelable to allow transfer between activities, but couldn't get it to work in time.
 */

public class AlienVideo implements Parcelable {

    private Date dateUploaded;
    private Date lastPlayed;

    private int userID;
    private String comment;

    private String thumbnailRef;
    private String videoRef;

    private String genre;
    private String tags;

    private int length;
    private boolean favourite;
    private boolean privacy;


    public AlienVideo (Date dateUploaded, Date lastPlayed, int userID, String comment, String thumbnailRef, String videoRef, String genre, String tags, int length, boolean favourite,  boolean privacy){
        this.dateUploaded = dateUploaded;
        this.lastPlayed = lastPlayed;
        this.userID = userID;
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

    // Parcelling part
    public int describeContents(){
        return this.hashCode();
    }

    public AlienVideo(Parcel in){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            this.dateUploaded = fmt.parse(in.readString());
            this.lastPlayed = fmt.parse(in.readString());
            this.userID = in.readInt();
            this.comment = in.readString();
            this.thumbnailRef = in.readString();
            this.videoRef = in.readString();
            this.genre = in.readString();
            this.tags = in.readString();
            this.length = in.readInt();
            this.favourite = Boolean.valueOf(in.readString());
            this.privacy = Boolean.valueOf(in.readString());
        } catch (Exception e) {

        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.dateUploaded.toString());
        dest.writeString(this.lastPlayed.toString());
        dest.writeInt(this.userID);
        dest.writeString(this.comment);
        dest.writeString(this.thumbnailRef);
        dest.writeString(this.videoRef);
        dest.writeString(this.genre);
        dest.writeString(this.tags);
        dest.writeInt(Integer.valueOf(this.length));
        dest.writeString(String.valueOf(this.favourite));
        dest.writeString(String.valueOf(this.privacy));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public AlienVideo createFromParcel(Parcel in) {
            return new AlienVideo(in);
        }
        public AlienVideo[] newArray(int size) {
            return new AlienVideo[size];
        }
    };
}
