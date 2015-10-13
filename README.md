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

      - init(Context context)

  * Cache limit

      - clearCacheByLimit(Context context, long limit)

  * Basic get / set methods

      # If context is set using init

      - set(String key, String value, Integer persist)
      - get(String key, String defaultValue)

      # Independent of context being set using init

      - set(Context context, String key, String value, Integer persist)
      - get(Context context, String key, String defaultValue)

  * Wrappers around get / set

      # If context is set using init

      - setInteger(String key, Integer value, Integer persist)
      - getInteger(String key, Integer defaultValue)
      - setFloat(String key, Float value, Integer persist)
      - getFloat(String key, Float defaultValue)
      - setDouble(String key, Double value, Integer persist)
      - getDouble(String key, Double defaultValue)
      - setBoolean(String key, Boolean value, Integer persist)
      - getBoolean(String key, Boolean defaultValue)
      - setByte(String key, Byte value, Integer persist)
      - getByte(String key, Byte defaultValue)
      - setLong(String key, Long value, Integer persist)
      - getLong(String key, Long defaultValue)
      - setShort(String key, Short value, Integer persist)
      - getShort(String key, Short defaultValue)

      # Independent of context being set using init
      
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
