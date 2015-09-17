package me.scola.picrawler.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.etsy.android.grid.StaggeredGridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import me.scola.picrawler.App;
import me.scola.picrawler.adapter.CardsAnimationAdapter;
import me.scola.picrawler.adapter.StaggeredCursorAdapter;
import me.scola.picrawler.dao.BaseDataHelper;
import me.scola.picrawler.dao.DataProvider;
import me.scola.picrawler.dao.FeedsDataHelper;
import me.scola.picrawler.dao.LikesDataHelper;
import me.scola.picrawler.data.GsonRequest;
import me.scola.picrawler.model.Feed;
import me.scola.picrawler.util.CLog;
import me.scola.picrawler.util.ListViewUtils;
import me.scola.picrawler.util.TaskUtils;
import me.scola.picrawler.view.LoadingFooter;
import me.scola.picrawler.view.PageListView;


/**
 * Created by Sam on 14-3-25.
 */
public class LikesGridCursorFragment extends BaseFragment implements  LoaderManager.LoaderCallbacks<Cursor> , SwipeRefreshLayout.OnRefreshListener{

    private static final String ARG_SECTION_NUMBER = "section_number";

    SwipeRefreshLayout mSwipeLayout;

    StaggeredGridView mGridView;

    private BaseDataHelper mDataHelper;
    private StaggeredCursorAdapter mAdapter;

    private static boolean mLike;
    private static int mSectionNumber;
    private String mString;
    private String mLatest;
    private boolean mAsyncTaskInsert;

    public static LikesGridCursorFragment newInstance(int sectionNumber) {
        mSectionNumber = sectionNumber;
        LikesGridCursorFragment fragment = new LikesGridCursorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
//        mLike = (sectionNumber == AppMainActivity.mFeeds.size());
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_feed_pinterest, container, false);

        mGridView = (StaggeredGridView)contentView.findViewById(R.id.grid_view);
        mSwipeLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
        mSwipeLayout.setSize(SwipeRefreshLayout.LARGE);

        if (mSectionNumber == AppMainActivity.mFeeds.size()) {
            mDataHelper = new LikesDataHelper(App.getContext());
            mLike = true;
            mSwipeLayout.setEnabled(false);
        } else {
            Set<String> keys = AppMainActivity.mFeeds.keySet();
            ArrayList<String> feed_like =  new ArrayList<String>(keys);
            Collections.sort(feed_like);
            mString = AppMainActivity.mFeeds.get(feed_like.get(mSectionNumber));
            String fileName = mString.substring(mString.lastIndexOf("/") + 1, mString.lastIndexOf(".txt"));

            DataProvider.reInitArgs(fileName);
            FeedsDataHelper.FeedsDBInfo.TABLE_NAME = "feeds" + fileName;
            mDataHelper = new FeedsDataHelper(App.getContext());
            ((FeedsDataHelper)mDataHelper).setTableName(fileName);

            mSwipeLayout.setOnRefreshListener(this);
            mSwipeLayout.setColorSchemeResources(R.color.material_700, R.color.material_500);
        }
        getLoaderManager().initLoader(0, null, this);

        int margin = getResources().getDimensionPixelSize(R.dimen.margin);
//        mGridView.setColumnCount(3);
//        mGridView.
//        mGridView.setGridPadding(margin, margin, margin, margin);

//        mGridView.setPadding(margin, margin, 0, margin); // have the margin on the sides as well

        mAdapter = new StaggeredCursorAdapter(getActivity());
        AnimationAdapter animationAdapter = new CardsAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(mGridView);
        mGridView.setAdapter(animationAdapter);
//        mGridView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();

//        mListView.setLoadNextListener(null);


//        mSwipeLayout.setEnabled(false);

        return contentView;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!mLike && mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(false);
            mSwipeLayout.destroyDrawingCache();
            mSwipeLayout.clearAnimation();
        }

    }

    public void scrollTopAndRefresh() {
        if (mGridView != null) {
            mGridView.resetToTop();
            refreshData();
        }
    }
    public FeedsDataHelper getDataHelper() {
        return (FeedsDataHelper)mDataHelper;
    }
//    @Override
//    public void onResume() {
//        super.onResume();
//        getLoaderManager().restartLoader(0, null, this);
//    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(App.getContext(), R.string.loading_failed, Toast.LENGTH_SHORT).show();
                mSwipeLayout.setRefreshing(false);
//                mListView.setState(LoadingFooter.State.Idle, 3000);
            }
        };
    }

    @Override
    public void onRefresh() {
        executeRequest(new GsonRequest(mString, Feed[].class, responseListener(), errorListener()));
    }

    private void refreshData() {
        if (mSwipeLayout.isRefreshing()) {
            return;
        }
        mSwipeLayout.setRefreshing(true);
        CLog.d("Refresh:"+ mString);
        executeRequest(new GsonRequest(mString, Feed[].class, responseListener(), errorListener()));
    }

    private Response.Listener<Feed[]> responseListener() {
        return new Response.Listener<Feed[]>() {
            @Override
            public void onResponse(final Feed[] response) {
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        CLog.d("recv data from sever");
                        mAsyncTaskInsert = true;
                        List<Feed> feeds = Arrays.asList(response);
                        if(feeds != null && feeds.size()>0) {
                            if (mLatest == null) {
                                Collections.sort(feeds);
                                mLatest = feeds.get(0).getDate();
                                CLog.d("bulkInsert begin");
                                if (feeds.size() > BULK_INSERT_MAX_LENGHT) {
                                    ((FeedsDataHelper)mDataHelper).bulkInsert(feeds.subList(0, BULK_INSERT_MAX_LENGHT));
                                } else {
                                    ((FeedsDataHelper)mDataHelper).bulkInsert(feeds);
                                }
                                CLog.d("bulkInsert end");

                            } else {
                                CLog.d("current latest date " + mLatest);
                                List<Feed> feedFilter = new ArrayList<Feed>();
                                for(Feed feed : feeds) {
                                    if(feed.getDate().compareTo(mLatest) > 0) feedFilter.add(feed);
                                }
                                if(feedFilter.size() > 0) {
                                    Collections.sort(feedFilter);
                                    mLatest = feedFilter.get(0).getDate();
                                    CLog.d("update bulkInsert begin");
                                    ((FeedsDataHelper)mDataHelper).bulkInsert(feedFilter);
                                    CLog.d("update bulkInsert end");
                                }
                            }

                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        CLog.d("onPostExecute");
                        mSwipeLayout.setRefreshing(false);
                        mAsyncTaskInsert = false;
//                        mListView.setState(LoadingFooter.State.Idle, 3000);
                    }
                });
            }
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mDataHelper.getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        CLog.d("onLoadFinished " + data.getCount());
        if (mLike) return;
        if (data != null && data.getCount() == 0) {
            refreshData();
        } else {
            if(mLatest == null)
                mLatest = mAdapter.getItem(0).getDate();
            CLog.d("onLoadFinished " + mLatest);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppMainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
//        ((AppMainActivity) activity).disableFreshMenu();
    }

    @Override
    protected boolean isInserting() {
        return mAsyncTaskInsert;
    }
}
