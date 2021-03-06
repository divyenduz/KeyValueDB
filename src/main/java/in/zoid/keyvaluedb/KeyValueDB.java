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
    private static Context sContext;
    private static KeyValueDB sInstance;

    private static String DATABASE_NAME = "_app";
    private static String DATABASE_TABLE = "_cache";
    private static int DATABASE_VERSION = 1;

    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static final String PERSIST = "PERSIST";
    private static final String KEY_CREATED_AT = "KEY_CREATED_AT";

    private static String createDBQueryBuilder(String database) {
        return "CREATE TABLE "
                + database + "(" + KEY + " TEXT PRIMARY KEY," + VALUE
                + " TEXT," + PERSIST + " INTEGER," + KEY_CREATED_AT
                + " DATETIME" + ")";
    }

    private static String alterTableQueryBuilder(String table, long count, long limit) {
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
     * Method to initialize the context in KeyValueDB instance
     *
     * @param context Any context object.
     */
    public static void init(Context context) {
        init(context, DATABASE_NAME, DATABASE_TABLE);
    }

    /**
     * Method to initialize the context, DB name, table name in KeyValueDB instance
     *
     * @param context      Any context object.
     * @param databaseName DB name
     * @param tableName    Table name
     */
    private static void init(Context context, String databaseName, String tableName) {
        sContext = context;
        setDBName(databaseName);
        setTableName(tableName);
    }

    /**
     * Set the DB name
     *
     * @param name DB name
     */
    private static void setDBName(String name) {
        KeyValueDB.DATABASE_NAME = name;
    }

    /**
     * Set the cache table name
     * WARNING: Migration is needed if the name is changed for 2nd time
     * This API is used to run only for the 1st time i.e. 1st install of the app
     *
     * @param name Table name
     */
    private static void setTableName(String name) {
        KeyValueDB.DATABASE_TABLE = name;
    }

    /**
     * Set the database version
     *
     * @param version DB version
     */
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
     * @param key     The URL or some other unique id for data can be used
     * @param value   String data to be saved
     * @param persist Whether to delete this (key, value, time, persist) tuple, when cleaning cache in
     *                clearCacheByLimit() method. 1 Means persist, 0 Means remove.
     * @return rowid of the insertion row
     */
    public static synchronized long set(String key, String value, Boolean persist) {
        return set(sContext, key, value, persist);
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
    public static synchronized long set(Context context, String key, String value, Boolean persist) {
        Log.i(TAG, getState());
        key = DatabaseUtils.sqlEscapeString(key);
        KeyValueDB dbHelper = getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long row = 0;
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(KEY, key);
            values.put(VALUE, value);

            // Wrapper of Boolean around 0, 1. This was original architecture.
            // Boolean persist was added for aesthetics
            if (persist) {
                values.put(PERSIST, 1);
            } else {
                values.put(PERSIST, 0);
            }

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
     * Getter method. Gets a value based on it's key pair in sqlite3 db.
     *
     * @param key          The URL or some other unique id for data can be used
     * @param defaultValue value to be returned in case something goes wrong or no data is found
     * @return value stored in DB if present, defaultValue otherwise.
     */
    public static synchronized String get(String key, String defaultValue) {
        return get(sContext, key, defaultValue);
    }

    /**
     * Getter method. Gets a value based on it's key pair in sqlite3 db.
     *
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
     * @param limit amount of data to be retained in FIFO, rest would be removed like a queue
     * @return number of rows affected on success
     */
    public static synchronized long clearCacheByLimit(long limit) {
        return clearCacheByLimit(sContext, limit);
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

    /*
       Below methods are set / get wrappers for primitive data type wrapper classes.
     */
    public static long setInteger(Context context, String key, Integer value, Boolean persist) {
        return set(context, key, String.valueOf(value), persist);
    }

    public static Integer getInteger(Context context, String key, Integer defaultValue) {
        return Integer.parseInt(get(context, key, String.valueOf(defaultValue)));
    }

    public static long setFloat(Context context, String key, Float value, Boolean persist) {
        return set(context, key, String.valueOf(value), persist);
    }

    public static Float getFloat(Context context, String key, Float defaultValue) {
        return Float.parseFloat(get(context, key, String.valueOf(defaultValue)));
    }

    public static long setDouble(Context context, String key, Double value, Boolean persist) {
        return set(context, key, String.valueOf(value), persist);
    }

    public static Double getDouble(Context context, String key, Double defaultValue) {
        return Double.parseDouble(get(context, key, String.valueOf(defaultValue)));
    }

    public static long setBoolean(Context context, String key, Boolean value, Boolean persist) {
        return set(context, key, String.valueOf(value), persist);
    }

    public static Boolean getBoolean(Context context, String key, Boolean defaultValue) {
        return Boolean.parseBoolean(get(context, key, String.valueOf(defaultValue)));
    }

    public static long setByte(Context context, String key, Byte value, Boolean persist) {
        return set(context, key, String.valueOf(value), persist);
    }

    public static Byte getByte(Context context, String key, Byte defaultValue) {
        return Byte.parseByte(get(context, key, String.valueOf(defaultValue)));
    }

    public static long setLong(Context context, String key, Long value, Boolean persist) {
        return set(context, key, String.valueOf(value), persist);
    }

    public static Long getLong(Context context, String key, Long defaultValue) {
        return Long.parseLong(get(context, key, String.valueOf(defaultValue)));
    }

    public static long setShort(Context context, String key, Short value, Boolean persist) {
        return set(context, key, String.valueOf(value), persist);
    }

    public static Short getShort(Context context, String key, Short defaultValue) {
        return Short.parseShort(get(context, key, String.valueOf(defaultValue)));
    }

    /*
       Below methods are set / get wrappers for primitive data type wrapper classes.
       This set of methods to not need a passed context.
     */
    public static long setInteger(String key, Integer value, Boolean persist) {
        return setInteger(sContext, key, value, persist);
    }

    public static Integer getInteger(String key, Integer defaultValue) {
        return getInteger(sContext, key, defaultValue);
    }

    public static long setFloat(String key, Float value, Boolean persist) {
        return setFloat(sContext, key, value, persist);
    }

    public static Float getFloat(String key, Float defaultValue) {
        return getFloat(sContext, key, defaultValue);
    }

    public static long setDouble(String key, Double value, Boolean persist) {
        return setDouble(sContext, key, value, persist);
    }

    public static Double getDouble(String key, Double defaultValue) {
        return getDouble(sContext, key, defaultValue);
    }

    public static long setBoolean(String key, Boolean value, Boolean persist) {
        return setBoolean(sContext, key, value, persist);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        return getBoolean(sContext, key, defaultValue);
    }

    public static long setByte(String key, Byte value, Boolean persist) {
        return setByte(sContext, key, value, persist);
    }

    public static Byte getByte(String key, Byte defaultValue) {
        return getByte(sContext, key, defaultValue);
    }

    public static long setLong(String key, Long value, Boolean persist) {
        return setLong(sContext, key, value, persist);
    }

    public static Long getLong(String key, Long defaultValue) {
        return getLong(sContext, key, defaultValue);
    }

    public static long setShort(String key, Short value, Boolean persist) {
        return setShort(sContext, key, value, persist);
    }

    public static Short getShort(String key, Short defaultValue) {
        return getShort(sContext, key, defaultValue);
    }

    /*
        Below are the private DB creating methods.
     */
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