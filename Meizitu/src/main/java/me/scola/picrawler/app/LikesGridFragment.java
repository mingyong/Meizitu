package me.scola.picrawler.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;


import me.scola.picrawler.App;
import me.scola.picrawler.adapter.StaggeredAdapter;
import me.scola.picrawler.dao.LikesDataHelper;
import me.scola.picrawler.model.Feed;
import me.scola.picrawler.util.CLog;

import com.origamilabs.library.views.StaggeredGridView;

import java.util.ArrayList;


/**
 * Created by Sam on 14-3-25.
 */
public class LikesGridFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String ARG_SECTION_NUMBER = "section_number";

    SwipeRefreshLayout mSwipeLayout;

    StaggeredGridView mGridView;

    private LikesDataHelper mDataHelper;

    private ArrayList<String> mUrls = new ArrayList<String>();
    private ArrayList<Integer> mIndexList = new ArrayList<Integer>();

    private StaggeredAdapter mAdapter;
    private Cursor mCursor;


    public static LikesGridFragment newInstance(int sectionNumber) {
        LikesGridFragment fragment = new LikesGridFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_feed_grid, container, false);

        mGridView = (StaggeredGridView)contentView.findViewById(R.id.staggeredGridView1);
        mSwipeLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);

        mDataHelper = new LikesDataHelper(App.getContext());
        getLoaderManager().initLoader(0, null, this);
        
        int margin = getResources().getDimensionPixelSize(R.dimen.margin);
        
        mGridView.setItemMargin(margin); // set the GridView margin
		
		mGridView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well
        mAdapter = new StaggeredAdapter(getActivity(), R.id.imageView1, mUrls);
        
        
        
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new StaggeredGridView.OnItemClickListener() {
            @Override
            public void onItemClick(StaggeredGridView parent, View view, int position, long id) {
                if (position < 0 || position >= mUrls.size()) {
                    return;
                }

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());

                Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                Feed feed = getFeed(position);
                intent.putExtra(ImageViewActivity.IMAGE_NAME, feed.getTitle());
                intent.putStringArrayListExtra(ImageViewActivity.IMAGE_URL, feed.getImgs());
                intent.putExtra(ImageViewActivity.IMAGE_ID, feed.getId().toString());
                intent.putExtra(ImageViewActivity.IMAGE_AUTHOR, new Gson().toJson(feed.getAuthor()));
                intent.putExtra(ImageViewActivity.IMAGE_DATE, feed.getDate());
                intent.putExtra(ImageViewActivity.IMAGE_ORIGINURL, feed.getUrl());
                intent.putExtra(ImageViewActivity.IMAGE_INDEX, feed.getImgs().indexOf(mAdapter.getItem(position)));
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }
        });
//        adapter.notifyDataSetChanged();
        
//        mSwipeLayout.setColorSchemeResources(R.color.material_700, R.color.material_500);
        mSwipeLayout.setEnabled(false);

        return contentView;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        CLog.d("onResume");
////        getLoaderManager().restartLoader(0, null, this);
//    }

    public Feed getFeed(int pos) {
        int i;
        for(i = 0; i < mIndexList.size(); i++) {
            if(mIndexList.get(i) > pos) break;
        }
        mCursor.moveToPosition(i);
        return Feed.fromCursor(mCursor);
    }

     @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return mDataHelper.getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//            mAdapter.changeCursor(data);
        if (data == null || data.getCount() == 0) {
            mUrls.clear();
            mAdapter.notifyDataSetChanged();
            return;
        }
        CLog.d("onLoadFinished begin");
        mUrls.clear();
        mIndexList.clear();
        mCursor = data;
        data.moveToFirst();
        int imgSize = 0;
        while (!data.isAfterLast()) {
            Feed feed = Feed.fromCursor(data);
            imgSize += feed.getImgs().size();
            mIndexList.add(imgSize);
            mUrls.addAll(feed.getImgs());
            data.moveToNext();
        }
        mAdapter.notifyDataSetChanged();
//
//        mGridView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mAdapter.notifyDataSetChanged();
//            }
//        }, 2000);
        CLog.d("onLoadFinished end");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//            mAdapter.changeCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppMainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
//        ((AppMainActivity) activity).disableFreshMenu();
    }
}
