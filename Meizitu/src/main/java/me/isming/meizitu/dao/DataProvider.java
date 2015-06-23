package me.isming.meizitu.dao;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import me.isming.meizitu.App;


public class DataProvider extends ContentProvider {
    static final String TAG = DataProvider.class.getSimpleName();

    static final Object DBLock = new Object();

    public static final String AUTHORITY = "me.isming.meizitu.provider";

    public static final String SCHEME = "content://";

    // messages
    public static String PATH_FEEDS = "/feeds";

    public static final String PATH_FEED = "/feeds";

    public static final String PATH_LIKES = "/likes";

    public static Uri FEEDS_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_FEEDS);

    public static final Uri LIKES_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_LIKES);

    private static final int LIKES = 1;
    private static final int FEEDS = 0;

    /*
     * MIME type definitions
     */
    public static final String FEED_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.me.isming.meizitu.feed";

    public static final String LIKE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.me.isming.meizitu.like";

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "feeds", FEEDS);
        sUriMatcher.addURI(AUTHORITY, "likes", LIKES);
    }

    private static DBHelper mDBHelper;

    public static DBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(App.getContext());
        }
        return mDBHelper;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        synchronized (DBLock) {
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            String table = matchTable(uri);
            queryBuilder.setTables(table);

            SQLiteDatabase db = getDBHelper().getReadableDatabase();
            Cursor cursor = queryBuilder.query(db, // The database to
                    // queryFromDB
                    projection, // The columns to return from the queryFromDB
                    selection, // The columns for the where clause
                    selectionArgs, // The values for the where clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    sortOrder // The sort order
            );

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case FEEDS:
                return FEED_CONTENT_TYPE;
            case LIKES:
                return  LIKE_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        synchronized (DBLock) {
            String table = matchTable(uri);
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            long rowId = 0;
            db.beginTransaction();
            try {
                rowId = db.insert(table, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                db.endTransaction();
            }
            if (rowId > 0) {
                Uri returnUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(uri, null);
                return returnUri;
            }
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        synchronized (DBLock) {
            SQLiteDatabase db = getDBHelper().getWritableDatabase();

            int count = 0;
            String table = matchTable(uri);
            db.beginTransaction();
            try {
                count = db.delete(table, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        synchronized (DBLock) {
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            int count;
            String table = matchTable(uri);
            db.beginTransaction();
            try {
                count = db.update(table, values, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);

            return count;
        }
    }

    private String matchTable(Uri uri) {
        String table = null;
        switch (sUriMatcher.match(uri)) {
            case FEEDS:
                table = FeedsDataHelper.FeedsDBInfo.TABLE_NAME;
                break;
            case LIKES:
                table = LikesDataHelper.FeedsDBInfo.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return table;
    }

    public static void reInitArgs(int sectionNumber) {
        PATH_FEEDS = PATH_FEED + Integer.toString(sectionNumber);
        FEEDS_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_FEEDS);
        sUriMatcher.addURI(AUTHORITY, "feeds" + sectionNumber, FEEDS);
//        FeedsDataHelper.FeedsDBInfo.TABLE_NAME += Integer.toString(sectionNumber);
    }

}
