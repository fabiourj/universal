package com.sherdle.universal.providers.fav;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sherdle.universal.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *  This adapter is used to manage the database where all the users favorite items are kept, 
 *  It contains methods to check for duplicates, create new database, remove values, etc.
 */
public class FavDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_OBJECT = "obj";
    public static final String KEY_PROVIDER = "provider";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    //create new database
    private static final String DATABASE_CREATE =
        "create table notes (_id integer primary key autoincrement, "
        + "title text not null, "
        + "obj varbinary not null, "
        + "provider text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database " + oldVersion + " to "
                    + newVersion + ", all data will be destroyed");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public FavDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    //Open the database
    public FavDbAdapter open() throws SQLException {
        try {
    	mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        } catch (Exception e){
        	Log.w(TAG, "Exception");
        }
        return this;
    }

    //close the database
    public void close() {
        mDbHelper.close();
    }

    //Create a new favorite
    public long addFavorite(String title, Serializable object, String provider) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_OBJECT, getSerializedObject(object));
        initialValues.put(KEY_PROVIDER, provider);
        
        //This adds respectively the folowing values
        //-title
        //-object
        //-type
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a favorite
    public boolean deleteFav(String title) {

        return mDb.delete(DATABASE_TABLE, KEY_TITLE + "= ?", new String[] {title}) > 0;
    }

    // Delete all favorites
    public void emptyDatabase(){
    	mDb.delete(DATABASE_TABLE, null, null);
    }

    
    //Get all favorites
    public Cursor getFavorites() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_TITLE,
                KEY_OBJECT, KEY_PROVIDER}, null, null, null, null, null);
    }
    
    //check for duplicates
    public boolean checkEvent(String title, Serializable object, String provider)
    {

        try (Cursor cursor = mDb.query(DATABASE_TABLE,
                new String[] {KEY_TITLE},
                KEY_TITLE + " = ?" ,
                new String[] {title}, null, null, null)) {

            return !cursor.moveToFirst();
        }
    }

    public static byte[] getSerializedObject(Serializable s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(s);
        } catch (IOException e) {
            //Logger.e(loggerTag, e.getMessage(), e);
            return null;
        } finally {
            try {
                oos.close();
            } catch (IOException e) {}
        }
        byte[] result = baos.toByteArray();
        //Logger.d(loggerTag, "Object " + s.getClass().getSimpleName() + "
         //       written tobyte[]: " + result.length);
        return result;
    }

    public static Serializable readSerializedObject(byte[] in) {
        Object result = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            result = ois.readObject();
        } catch (Exception e) {
            result = null;
        } finally {
            try {
                ois.close();
            } catch (Throwable e) {
            }
        }
        return (Serializable) result;
    }
}
