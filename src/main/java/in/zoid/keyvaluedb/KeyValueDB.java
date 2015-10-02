package in.zoid.keyvaluedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Divyendu on 7/4/15.
 */
public class KeyValueDB extends SQLiteOpenHelper {
    private static final String TAG = "KeyValueDB";
    private static KeyValueDB sInstance;

    private static String DATABASE_NAME = "_app";
    private static String DATABASE_TABLE = "_cache";
    private static int DATABASE_VERSION = 1;

    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static final String PERSIST = "PERSIST";
    private static final String KEY_CREATED_AT = "KEY_CREATED_AT";

    public static String createDBQueryBuilder(String database) {
        return "CREATE TABLE "
                + database + "(" + KEY + " TEXT PRIMARY KEY," + VALUE
                + " TEXT," + PERSIST + " INTEGER," + KEY_CREATED_AT
                + " DATETIME" + ")";
    }

    public static String alterTableQueryBuilder(String table, long count, long limit) {
        return "DELETE FROM " + table +
                " WHERE " + KEY
                + " IN (SELECT " + KEY + " FROM " + DATABASE_TABLE
                + " WHERE " + PERSIST + " = 0"
                + " ORDER BY " + KEY_CREATED_AT
                + " ASC LIMIT " + String.valueOf(count - limit) + ");";
    }

    /**
     * Returns the current state of KeyValueDB by returning DB / Table name and other parameter
     *
     * @return State information
     */
    private static String getState() {
        return "State: " + DATABASE_TABLE + " on " + DATABASE_NAME + " @ " + DATABASE_VERSION;
    }

    /**
     * Set the DB name
     *
     * @param name DB name
     */
    public static void setDBName(String name) {
        KeyValueDB.DATABASE_NAME = name;
    }

    /**
     * Set the cache table name
     * WARNING: Migration is needed if the name is changed for 2nd time
     * This API is used to run only for the 1st time i.e. 1st install of the app
     *
     * @param name Table name
     */
    public static void setTableName(String name) {
        KeyValueDB.DATABASE_TABLE = name;
    }

    private static void setDatabaseVersion(int version) {
        KeyValueDB.DATABASE_VERSION = version;
    }

    private static synchronized KeyValueDB getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new KeyValueDB(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     *
     * @param context Any context object.
     */
    private KeyValueDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, "onCreate");
        flush(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "onUpgrade");
        flush(db);
    }

    /**
     * Setter method. Sets a (key, value) pair in sqlite3 db.
     *
     * @param context Any context object.
     * @param key     The URL or some other unique id for data can be used
     * @param value   String data to be saved
     * @param persist Whether to delete this (key, value, time, persist) tuple, when cleaning cache in
     *                clearCacheByLimit() method. 1 Means persist, 0 Means remove.
     * @return rowid of the insertion row
     */
    public static synchronized long set(Context context, String key, String value, Integer persist) {
        Log.i(TAG, getState());
        key = DatabaseUtils.sqlEscapeString(key);
        KeyValueDB dbHelper = getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long row = 0;
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(KEY, key);
            values.put(VALUE, value);
            values.put(PERSIST, persist);
            values.put(KEY_CREATED_AT, "time('now')");
            Cursor c = null;
            try {
                row = db.replace(DATABASE_TABLE, null, values);
            } catch (SQLiteException e) {
                flush(e, db);
                set(context, key, value, persist);
            }
            db.close();
        }
        return row;
    }

    /**
     * @param context      Any context object.
     * @param key          The URL or some other unique id for data can be used
     * @param defaultValue value to be returned in case something goes wrong or no data is found
     * @return value stored in DB if present, defaultValue otherwise.
     */
    public static synchronized String get(Context context, String key, String defaultValue) {
        Log.i(TAG, getState());
        key = DatabaseUtils.sqlEscapeString(key);
        Log.v(TAG, "getting cache: " + key);
        KeyValueDB dbHelper = getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String value = defaultValue;
        if (db != null) {
            Cursor c = null;
            try {
                c = db.query(DATABASE_TABLE, new String[]{VALUE}, KEY + "=?", new String[]{key}, null, null, null);
            } catch (SQLiteException e) {
                flush(e, db);
                get(context, key, defaultValue);
            }
            if (c != null) {
                if (c.moveToNext()) {
                    value = c.getString(c.getColumnIndex(VALUE));
                }
                Log.v(TAG, "get cache size:" + String.valueOf(value.length()));
                c.close();
            }
            db.close();
        }
        return value;
    }

    /**
     * Clear the cache like a FIFO queue defined by the limit parameter.
     * Each function call made to this will remove count(*)-limit first rows from the DB
     * Only the data with (Persist, 0) will be removed
     *
     * @param context Any context object.
     * @param limit   amount of data to be retained in FIFO, rest would be removed like a queue
     * @return number of rows affected on success
     */
    public static synchronized long clearCacheByLimit(Context context, long limit) {
        KeyValueDB dbHelper = getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long numRows = 0;
        if (db != null) {
            Cursor c = null;
            try {
                c = db.query(DATABASE_TABLE, null, null, null, null, null, null);
            } catch (SQLiteException e) {
                flush(e, db);
                clearCacheByLimit(context, limit);
            }
            if (c != null) {
                long count = c.getCount();
                Log.v(TAG, "cached rows" + String.valueOf(count));
                if (count > limit) {
                    try {
                        db.execSQL(alterTableQueryBuilder(DATABASE_TABLE, count, limit));
                    } catch (SQLiteException e) {
                        flush(e, db);
                        clearCacheByLimit(context, limit);
                    }
                }
                try {
                    c = db.query(DATABASE_TABLE, null, null, null, null, null, null);
                } catch (SQLiteException e) {
                    flush(e, db);
                    clearCacheByLimit(context, limit);
                }
                numRows = count - c.getCount();
                c.close();
            }
            db.close();
        }
        return numRows;
    }

    private static void flush(Exception e, SQLiteDatabase db) {
        e.printStackTrace();
        flush(db);
    }

    private static void flush(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            db.execSQL(createDBQueryBuilder(DATABASE_TABLE));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("SQLException while flush. Have to drop caching");
        }
    }
}