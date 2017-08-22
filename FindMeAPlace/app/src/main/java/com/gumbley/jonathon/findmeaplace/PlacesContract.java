package com.gumbley.jonathon.findmeaplace;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by jonat on 14/06/2017.
 */

public final class PlacesContract {

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlaceEntry.TABLE_NAME + " (" +
            PlaceEntry._ID + " INTEGER PRIMARY KEY," +
            PlaceEntry.COLUMN_NAME_TITLE + " TEXT," +
            PlaceEntry.COLUMN_NAME_LOCATION + " TEXT," +
            PlaceEntry.COLUMN_NAME_IMAGE_URL + " TEXT, " +
            PlaceEntry.COLUMN_NAME_ADDRESS + " TEXT, " +
            PlaceEntry.COLUMN_NAME_TYPE + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlaceEntry.TABLE_NAME;


    private PlacesContract() {}

    public static final class PlaceEntry implements BaseColumns {
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_IMAGE_URL = "imageUrl";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_TYPE = "type";
    }

}