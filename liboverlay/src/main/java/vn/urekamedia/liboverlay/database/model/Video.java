package vn.urekamedia.liboverlay.database.model;

import android.os.Bundle;

public class Video {
    public static final String TABLE_NAME = "videos";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_UUID = "uuid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_SRC = "src";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_STATUS = "status";

    private int id;
    private String name;
    private String src;
    private String timestamp;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_UUID + " INTEGER,"
                    + COLUMN_STATUS + " INTEGER,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_URL + " TEXT,"
                    + COLUMN_SRC + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    public Video() {
    }

    public Video(int id, String name, String timestamp, String src) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.src = src;
    }

    public int getId() {
        return id;
    }

    public String getSrc(){
        return this.src;
    }
    public void setSrc(String src){
        this.src = src;
    }

    public String getVideo() {
        return this.name;
    }

    public void setVideo(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    protected void onActivityCreated(Bundle bundle) {
    }

    protected void onResume() {
    }

    protected void onPause() {
    }
}
