package me.scola.picrawler.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.Space;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.etsy.android.grid.StaggeredGridView;

import java.util.ArrayList;
import java.util.List;

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
        View view = mLayoutInflater.inflate(R.layout.imagegroup, null);
        Holder holder = new Holder();
//        holder.imageView = (ScaleImageView) view.findViewById(R.id.imageView1);
        holder.imageGroup =  (LinearLayout) view.findViewById(R.id.linear);

        Feed feed = Feed.fromCursor(cursor);
        if(!feed.getImgs().isEmpty()) {
            for (String img : feed.getImgs()) {
                View  v =  mLayoutInflater.inflate(R.layout.scaleimgeview, null);
                ScaleImageView imageView = (ScaleImageView) v.findViewById(R.id.imageView1);
//                ImageLoader.ImageContainer imageRequest = ImageCacheManager.loadImage(img, ImageCacheManager
//                        .getImageListener(imageView, mDefaultImageDrawable, mDefaultImageDrawable));
//                holder.imageRequestList.add(imageRequest);
                holder.imageGroup.addView(imageView);
                if (feed.getImgs().indexOf(img) != feed.getImgs().size() - 1) {
                    Space space = new Space(context);
                    holder.imageGroup.addView(space);
                    space.getLayoutParams().height = context.getResources().getDimensionPixelSize(R.dimen.margin);
                }
            }
        }
        holder.imgList = feed.getImgs();
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Holder holder = (Holder) view.getTag();
        if (holder.imageRequestList != null && holder.imageRequestList.size() > 0) {
            for (ImageLoader.ImageContainer request : holder.imageRequestList) {
                request.cancelRequest();
            }
            holder.imageRequestList.clear();
        }

//        view.setEnabled(!mListView.isItemChecked(cursor.getPosition()
//                + mListView.getHeaderViewsCount()));

//        Feed feed = Feed.fromCursor(cursor);
//        List<String> im = holder.imgList;
        if(holder.imgList.isEmpty()) {
            return;
        }

        CLog.d("bindView " + holder.imgList);

        for (int i = 0; i < holder.imageGroup.getChildCount(); i++) {
            CLog.d("feed.getImgs().size " + holder.imgList.size() + " holder.imageGroup.getChildCount " + holder.imageGroup.getChildCount());
            if (i % 2 == 1) continue;
            if (i / 2 >= holder.imgList.size()) break;
            ScaleImageView imageView = (ScaleImageView) holder.imageGroup.getChildAt(i);
            ImageLoader.ImageContainer imageRequest = ImageCacheManager.loadImage(holder.imgList.get(i / 2), ImageCacheManager
                    .getImageListener(imageView, mDefaultImageDrawable, mDefaultImageDrawable));
            holder.imageRequestList.add(imageRequest);
        }

//        for (String img : feed.getImgs()) {
//            View  v =  mLayoutInflater.inflate(R.layout.scaleimgeview, null);
//            ScaleImageView imageView = (ScaleImageView) v.findViewById(R.id.imageView1);
//            ImageLoader.ImageContainer imageRequest = ImageCacheManager.loadImage(img, ImageCacheManager
//                .getImageListener(imageView, mDefaultImageDrawable, mDefaultImageDrawable));
//            holder.imageRequestList.add(imageRequest);
//            holder.imageGroup.addView(imageView);
//            if (feed.getImgs().indexOf(img) != feed.getImgs().size() - 1) {
//                Space space = new Space(context);
//                holder.imageGroup.addView(space);
//                space.getLayoutParams().height = context.getResources().getDimensionPixelSize(R.dimen.margin);
//            }
//        }
    }

    class Holder {
        LinearLayout imageGroup;
        List<String> imgList;
        public List<ImageLoader.ImageContainer> imageRequestList = new ArrayList<ImageLoader.ImageContainer>();
    }
}
