package vn.urekamedia.liboverlay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import vn.urekamedia.liboverlay.database.model.Video;

/**
 * Created by ravi on 15/03/18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "app_car_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create notes table
        db.execSQL(Video.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Video.TABLE_NAME);
        // Create tables again
        onCreate(db);
    }
    public long insertVideo(String uuid, String name, String url, String src, Integer status) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // no need to add them
        values.put(Video.COLUMN_UUID, uuid);
        values.put(Video.COLUMN_NAME, name);
        values.put(Video.COLUMN_URL, url);
        values.put(Video.COLUMN_SRC, src);
        values.put(Video.COLUMN_STATUS,status);
        long id = db.insert(Video.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public Video getNote(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Video.TABLE_NAME,
                new String[]{Video.COLUMN_ID,Video.COLUMN_UUID,Video.COLUMN_NAME,Video.COLUMN_TIMESTAMP,Video.COLUMN_SRC},
                Video.COLUMN_UUID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            // prepare note object
            Video note = new Video(
                    cursor.getInt(cursor.getColumnIndex(Video.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(Video.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(Video.COLUMN_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndex(Video.COLUMN_SRC)));

            // close the db connection
            cursor.close();
            return note;
        }
        return null;
    }

    public List<Video> getAllNotes(Integer status) {
        List<Video> notes = new ArrayList<>();
        // Select All Query

        String selectQuery = "SELECT  * FROM " + Video.TABLE_NAME +" WHERE " +Video.COLUMN_STATUS +"=" +status+ " ORDER BY " +
                Video.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Video note = new Video();
                note.setId(cursor.getInt(cursor.getColumnIndex(Video.COLUMN_ID)));
                note.setVideo(cursor.getString(cursor.getColumnIndex(Video.COLUMN_NAME)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(Video.COLUMN_TIMESTAMP)));
                note.setSrc(cursor.getString(cursor.getColumnIndex(Video.COLUMN_SRC)));

                notes.add(note);
            } while (cursor.moveToNext());
        }
        // close db connection
        db.close();
        // return notes list
        return notes;
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + Video.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateNote(Video note, Integer status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Video.COLUMN_STATUS, status);
        // updating row
        return db.update(Video.TABLE_NAME, values, Video.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(Video note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Video.TABLE_NAME, Video.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
    }
}