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

  - set(context, key, value, persist)
  - get(context, key, defaultValue)
  - clearCacheByLimit(context, limit)

### License 

MIT
