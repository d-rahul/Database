package com.readyandroid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.readyandroid.BuildConfig;
import com.readyandroid.AppPreferences;

public class DatabaseHelper extends SQLiteOpenHelper {
    protected static Object lock = new Object();
    //    private Context mContext;
    protected static SQLiteDatabase database;
    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "readyandroid.sqlite";
    private static DatabaseHelper dbHelper;

    /**
     * Constructor used to set static database groupName and version
     *
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Constructor used to set manually database groupName and version
     *
     * @param context
     * @param dbname
     * @param dbversion
     */
    public DatabaseHelper(Context context, String dbname, int dbversion) {
        super(context, dbname, null, dbversion);
        DATABASE_VERSION = dbversion;
        DATABASE_NAME = dbname;
    }

    /**
     * Manually passing database groupName and version at the time of instance creation
     *
     * @param context
     * @param dbname
     * @param dbversion
     * @return
     */
    /*public static DatabaseHelper getInstance(Context context, String dbname, int dbversion) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context, dbname, dbversion);
        }
        return dbHelper;
    }*/

    /**
     * Setting database groupName and version
     * database groupName = user id
     * database version = app version code
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (dbHelper == null) {
            String dbName = AppPreferences.getInstance(context).getString(AppPreferences.PREFS_USER_ID, DATABASE_NAME);
            if (!dbName.endsWith(".sqlite")) {
                dbName = dbName.concat(".sqlite");
            }
            int dbVersion = BuildConfig.VERSION_CODE;
            dbHelper = new DatabaseHelper(context, dbName, dbVersion);
        }
        return dbHelper;
    }

    //public static synchronized void closeDBHelper() {

    public void closeDBHelper() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        dbHelper = null;

        if (database != null) {
            database.close();
        }
        database = null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /* create contact table */
        db.execSQL(TblContacts.TABLE_CONTACTS);
        /* create MESSAGE table */
        db.execSQL(TblMessages.TABLE_MESSAGE);
        /* create room table */
        db.execSQL(TblRoom.TABLE_ROOM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* update table query */
        /*if (oldVersion < newVersion) {
            truncateDatabase();
        }*/
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (database == null || !database.isOpen()) {
            database = super.getWritableDatabase();
        }
        return database;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (database == null || !database.isOpen()) {
            database = super.getWritableDatabase();
        }
        return database;
    }

    /**
     * Only use if user logout with.
     * It will drop all table and recreate it.
     */
    public void truncateDatabase() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("drop table " + TblContacts.TABLE_NAME);
            db.execSQL("drop table " + TblMessages.TABLE_NAME);
            db.execSQL("drop table " + TblRoom.TABLE_NAME);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
