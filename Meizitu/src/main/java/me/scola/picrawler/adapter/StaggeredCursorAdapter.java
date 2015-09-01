package me.scola.picrawler.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.origamilabs.library.views.StaggeredGridView;

import me.scola.picrawler.app.R;
import me.scola.picrawler.data.ImageCacheManager;
import me.scola.picrawler.model.Feed;
import me.scola.picrawler.util.CLog;
import me.scola.picrawler.view.ScaleImageView;


/**
 * Created by Sam on 14-3-26.
 */
public class StaggeredCursorAdapter extends CursorAdapter {
    private LayoutInflater mLayoutInflater;

//    private ListView mListView;

    private Drawable mDefaultImageDrawable = new ColorDrawable(Color.argb(255, 201, 201, 201));

    public StaggeredCursorAdapter(Context context) {
        super(context, null, false);
        mLayoutInflater = ((Activity) context).getLayoutInflater();
//        mListView = listView;
    }

    @Override
    public Feed getItem(int position) {
        if (mCursor == null || mCursor.getCount() <= position) {
            return null;
        }
        mCursor.moveToPosition(position);
        return Feed.fromCursor(mCursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        CLog.d("newView");
        View view =  mLayoutInflater.inflate(R.layout.row_staggered_demo, null);
        Holder holder = new Holder();
        holder.imageView = (ScaleImageView) view.findViewById(R.id.imageView1);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Holder holder = (Holder) view.getTag();
//        if (holder.imageRequest != null) {
//            holder.imageRequest.cancelRequest();
//        }

//        view.setEnabled(!mListView.isItemChecked(cursor.getPosition()
//                + mListView.getHeaderViewsCount()));

        Feed feed = Feed.fromCursor(cursor);
//        if(!feed.getImgs().isEmpty()) {
//            holder.imageRequest = ImageCacheManager.loadImage(feed.getImgs().get(0), ImageCacheManager
//                    .getImageListener(holder.image, mDefaultImageDrawable, mDefaultImageDrawable));
//        }
        CLog.d("bindView " + feed.getImgs());
        holder.imageRequest = ImageCacheManager.loadImage(feed.getImgs().get(0), ImageCacheManager
                .getImageListener(holder.imageView, mDefaultImageDrawable, mDefaultImageDrawable));
//        StaggeredAdapter subGridAdapter = new StaggeredAdapter(context, R.id.imageView1, feed.getImgs());
//        holder.gridview.setAdapter(subGridAdapter);
//        subGridAdapter.notifyDataSetChanged();

//        holder.caption.setText(feed.getTitle());
    }

    static class Holder {
        //        StaggeredGridView gridview;
        ScaleImageView imageView;
        public ImageLoader.ImageContainer imageRequest;
    }
}
