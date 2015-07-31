package me.scola.picrawler.model;

import android.database.Cursor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.scola.picrawler.dao.FeedsDataHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 14-4-22.
 */
public class Feed extends BaseModel implements Comparable<Feed> {
    private String id;
    private Map<String, String> author;
    private String date;
    private ArrayList<String> imgs;
    private String title;
    private String url;

    public Map<String, String> getAuthor() {
        return author;
    }

    public void setAuthor(Map<String, String> author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Feed() {
//        id = UUID.randomUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public ArrayList<String> getTags() {
//        return tags;
//    }
//
//    public void setTags(ArrayList<String> tags) {
//        this.tags = tags;
//    }

    public ArrayList<String> getImgs() {
        return imgs;
    }

    public void setImgs(ArrayList<String> imgs) {
        this.imgs = imgs;
    }

    public static Feed fromJson(String jsonStr) {
        return new Gson().fromJson(jsonStr, Feed.class);
    }

    public static Feed fromCursor(Cursor cursor) {
        Feed feed = new Feed();
        feed.setId(cursor.getString(cursor.getColumnIndex(FeedsDataHelper.FeedsDBInfo.ID)));
        feed.setTitle(cursor.getString(cursor.getColumnIndex(FeedsDataHelper.FeedsDBInfo.TITLE)));
        feed.setAuthor((Map<String, String>) new Gson().fromJson(
                cursor.getString(cursor.getColumnIndex(FeedsDataHelper.FeedsDBInfo.AUTHOR)),
                new TypeToken<Map<String, String>>() {
                }.getType()));
        feed.setImgs((ArrayList<String>) new Gson().fromJson(
                cursor.getString(cursor.getColumnIndex(FeedsDataHelper.FeedsDBInfo.IMGS)),
                new TypeToken<List<String>>() {
                }.getType()));
        feed.setDate(cursor.getString(cursor.getColumnIndex(FeedsDataHelper.FeedsDBInfo.DATE)));
        feed.setUrl(cursor.getString(cursor.getColumnIndex(FeedsDataHelper.FeedsDBInfo.URL)));
        return feed;
    }

    public static class FeedRequestData {
//        public int status;
//        public String msg;
        public ArrayList<Feed> data;

    }

    @Override
    public int compareTo(Feed feed) {
        //write code here for compare name
        return feed.getDate().compareTo(this.date);
    }

//    public static class Item {
////        public Result result;
////        public static class Result {
//            public Map<String, String> author;
//            public String date;
//            public ArrayList<String> imgs;
//            public String title;
////            public String taskid;
////            public String updatetime;
//            public String url;
////        }
//    }
}
