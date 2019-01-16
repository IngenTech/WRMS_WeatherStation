package com.example.admin.wrms_weatherstation;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

    // Initial Configuration
    public static final String DB_NAME = "wrms_weather_station";
    private static final int DATABASE_VER = 2;
    private static final String TAG = "DBAdapter";

    private final Context context;

    private DatabaseHelper DBHelper;
    public SQLiteDatabase db = null;

    public static final String TABLE_DATE = "date_table";
    public static final String TABLE_DATE_RAINFALL = "rainfall_table";


    public static final String ID = "_id";

    public static final String CREATED = "created";

    public static final String RAINFALL = "rainfall";
    public static final String HOURS = "hour";
    public static final String DATE = "date";
    public static final String IMEI = "imei";






    private static final String CREATE_DATE_TABLE = "CREATE TABLE "
            + TABLE_DATE + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IMEI + " TEXT, "
            + DATE + " DATETIME);";

    private static final String CREATE_RAINFALL_TABLE = "CREATE TABLE "
            + TABLE_DATE_RAINFALL + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IMEI + " TEXT, "
            + DATE + " TEXT, "
            + HOURS + " TEXT, "
            + RAINFALL + " TEXT);";


    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VER);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DATE_TABLE);
            db.execSQL(CREATE_RAINFALL_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS "+TABLE_DATE);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_DATE_RAINFALL);
            onCreate(db);
        }
    }

    public SQLiteDatabase getSQLiteDatabase() {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        return db;
    }

    public void deletefromtable(String tablename) {
        db.delete(tablename, null, null);
    }

    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public Cursor getDateList(String imei) throws SQLException {
        return db.query(TABLE_DATE, new String[]{ID, DATE}, IMEI + "='" + imei + "'", null, null, null, null, "100");
    }


    public Cursor getDataByDate(String datee,String imei) {


        return db.query(true, TABLE_DATE_RAINFALL, null, DATE + "='" + datee + "' AND " + IMEI + " ='" + imei + "'", null, null, null, null, null);
    }
    public Cursor getAllData() {
        return db.query(true, TABLE_DATE_RAINFALL, null, null, null, null, null, null, null);
    }

    public Cursor getYesterDayData() {
        // String query = "SELECT * from " + TABLE_DATE + " WHERE " + DATE + " >= date('now','localtime', '-1 day')";

        String query = "SELECT * from " + TABLE_DATE + " WHERE " + DATE + " BETWEEN datetime('now', 'start of day') AND datetime('now', 'localtime')";
        Cursor c = db.rawQuery(query, null);

        return c;
    }
    public Cursor getAllDate() {
        return db.query(true, TABLE_DATE, null, null, null, null, null, null, null);
    }
    public Cursor getBetweenData(String d1,String d2) {
     //   String query = "SELECT * from " + TABLE_DATE + " WHERE " + DATE + " BETWEEN "+ d1 + " AND " + d2+"";

        Cursor mCursor = db.rawQuery("SELECT * FROM "+ TABLE_DATE +
                " WHERE " + DATE +
                " BETWEEN ?  AND ?", new String[]{d1, d2});
       // Cursor c = db.rawQuery(query, null);
        return mCursor;
    }





    public Cursor getToday() {
        String query = "SELECT * from " + TABLE_DATE + " WHERE " + DATE + " >= date('now','localtime', 'start of day')";

        Cursor c = db.rawQuery(query, null);

        return c;
    }

    public Cursor getLast7Data() {
      //  String query = "SELECT * from " + TABLE_AVG + " WHERE " + DATE_ONLY + " >= date('now','localtime', '-6 day')";

        String query = "SELECT * from " + TABLE_DATE + " WHERE " + DATE + " BETWEEN datetime('now', '-6 days') AND datetime('now', 'localtime')";


        Cursor c = db.rawQuery(query, null);

        return c;
    }

    public Cursor getLast30Data() {
      //  String query = "SELECT * from " + TABLE_AVG + " WHERE " + DATE_ONLY + " >= date('-6 day','localtime', '-29 day')";

        String query = "SELECT * from " + TABLE_DATE + " WHERE " + DATE + " BETWEEN datetime('now', 'start of month') AND datetime('now', 'localtime')";


        Cursor c = db.rawQuery(query, null);

        return c;
    }



    public void deleteDataOlderThan30Days() {

        String query = "SELECT * from " + TABLE_DATE_RAINFALL + " WHERE " + DATE + " <= date('now','-30 day')";
        db.execSQL(query);
    }




}
