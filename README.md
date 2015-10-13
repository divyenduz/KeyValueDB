# KeyValueDB
A sqlite3 interface mainly to make a small cache

### Include

```
repositories {
  // ...
  maven { url "https://jitpack.io" }
}
```
 
```
dependencies {
  compile 'com.github.divyenduz:KeyValueDB:1.0.0'
}
```

### Docs

  * DB initialization

      - setDBName(String name)
      - setTableName(String name)

  * Cache limit

      - clearCacheByLimit(Context context, long limit)

  * Basic get / set methods

      - set(Context context, String key, String value, Integer persist)
      - get(Context context, String key, String defaultValue)

  * Wrappers around get / set

      - setInteger(Context context, String key, Integer value, Integer persist)
      - getInteger(Context context, String key, Integer defaultValue)
      - setFloat(Context context, String key, Float value, Integer persist)
      - getFloat(Context context, String key, Float defaultValue)
      - setDouble(Context context, String key, Double value, Integer persist)
      - getDouble(Context context, String key, Double defaultValue)
      - setBoolean(Context context, String key, Boolean value, Integer persist)
      - getBoolean(Context context, String key, Boolean defaultValue)
      - setByte(Context context, String key, Byte value, Integer persist)
      - getByte(Context context, String key, Byte defaultValue)
      - setLong(Context context, String key, Long value, Integer persist)
      - getLong(Context context, String key, Long defaultValue)
      - setShort(Context context, String key, Short value, Integer persist)
      - getShort(Context context, String key, Short defaultValue)
  }

### License 

MIT
