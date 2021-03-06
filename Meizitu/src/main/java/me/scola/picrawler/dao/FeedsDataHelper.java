package me.scola.picrawler.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;
import com.google.gson.Gson;
import me.scola.picrawler.model.Feed;
import me.scola.picrawler.util.database.Column;
import me.scola.picrawler.util.database.SQLiteTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class FeedsDataHelper extends BaseDataHelper {
    public FeedsDataHelper(Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.FEEDS_CONTENT_URI;
    }

    private ContentValues getContentValues(Feed feed) {
        ContentValues values = new ContentValues();
        values.put(FeedsDBInfo.ID, feed.getId().toString());
        values.put(FeedsDBInfo.AUTHOR, new Gson().toJson(feed.getAuthor()));
        values.put(FeedsDBInfo.DATE, feed.getDate());
        values.put(FeedsDBInfo.IMGS, new Gson().toJson(feed.getImgs()));
        values.put(FeedsDBInfo.TITLE, feed.getTitle());
        values.put(FeedsDBInfo.URL, feed.getUrl());
//        values.put(FeedsDBInfo.TAGS, new Gson().toJson(feed.getTags()));

        return values;
    }

    public Feed query(String id) {
        Feed feed = null;
        Cursor cursor = query(null,  FeedsDBInfo.ID + "= ?",
                new String[] {
                        id
                }, null);
        if (cursor.moveToFirst()) {
            feed = Feed.fromCursor(cursor);
        }
        cursor.close();
        return feed;
    }

    public void bulkInsert(List<Feed> feeds) {
        ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
        for (Feed feed : feeds) {
            feed.setId(feed.getDate().replaceAll("\\W+", "") + UUID.randomUUID().toString());
            ContentValues values = getContentValues(feed);
            contentValues.add(values);
        }
        ContentValues[] valueArray = new ContentValues[contentValues.size()];
        bulkInsert(contentValues.toArray(valueArray));
    }

    public int deleteAll() {
        synchronized (DataProvider.DBLock) {
            DBHelper mDBHelper = DataProvider.getDBHelper();
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            int row = db.delete(FeedsDBInfo.TABLE_NAME, null, null);
            return row;
        }
    }

    public CursorLoader getCursorLoader() {
        return new CursorLoader(getContext(), getContentUri(), null, null, null, FeedsDBInfo.ID + " DESC");
    }

    public void setTableName(String name) {
        FeedsDBInfo.TABLE.setTableName("feeds" + name);
        synchronized (DataProvider.DBLock) {
            DBHelper mDBHelper = DataProvider.getDBHelper();
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            FeedsDBInfo.TABLE.create(db);
        }
    }

    public static final class FeedsDBInfo implements BaseColumns {
        private FeedsDBInfo() {
        }

        public static String TABLE_NAME = "feeds" + "meizitu";

        public static final String ID = "id";
        public static final String AUTHOR = "author";
        public static final String DATE = "date";
        public static final String IMGS = "imgs";
        public static final String TITLE = "title";
        public static final String URL = "url";


        public static final SQLiteTable TABLE = new SQLiteTable(TABLE_NAME)
                .addColumn(ID, Column.DataType.TEXT)
                .addColumn(AUTHOR, Column.DataType.TEXT)
                .addColumn(DATE, Column.DataType.TEXT)
                .addColumn(IMGS, Column.DataType.TEXT)
                .addColumn(TITLE, Column.DataType.TEXT)
                .addColumn(URL, Column.DataType.TEXT);
    }
}
