package org.janb.shoppinglist.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PredictionDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_ITEM = "itemTitle";

    private static final String TAG = "PredictionDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "predictionData";
    private static final String SQLITE_TABLE = "predictions";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                    KEY_ITEM + "," +
                    " UNIQUE (" + KEY_ITEM +"));";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data.");
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
            onCreate(db);
        }
    }

    public PredictionDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public PredictionDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    public long addPrediction(String item) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ITEM, item);
        return mDb.insert(SQLITE_TABLE, null, initialValues);
    }

    public void clearPredictions() {
        mDb.delete(SQLITE_TABLE, null , null);
    }

    public Cursor getPrediction(String input) throws SQLException {
        Cursor mCursor = mDb.query(true, SQLITE_TABLE, new String[] {KEY_ROWID,
                        KEY_ITEM}, KEY_ITEM + " like '%" + input + "%'", null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}