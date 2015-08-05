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
import android.widget.AbsListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;


import me.scola.picrawler.App;
import me.scola.picrawler.adapter.StaggeredAdapter;
import me.scola.picrawler.dao.DataProvider;
import me.scola.picrawler.dao.FeedsDataHelper;
import me.scola.picrawler.data.GsonRequest;
import me.scola.picrawler.model.Feed;
import me.scola.picrawler.util.CLog;
import me.scola.picrawler.util.TaskUtils;

import com.origamilabs.library.views.StaggeredGridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;



/**
 * Created by Sam on 14-3-25.
 */
public class FeedsGridFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_SECTION_NUMBER = "section_number";

    SwipeRefreshLayout mSwipeLayout;

    StaggeredGridView mGridView;

    private FeedsDataHelper mDataHelper;
    private String mString = "http://23.252.109.110:5000/results/dump/haixiuzu2.txt";
    private String mFileName;
    private static int mSectionNumber = 0;
    private String mLatest;

    private ArrayList<String> mUrls = new ArrayList<String>();
    private ArrayList<Integer> mIndexList = new ArrayList<Integer>();

    private StaggeredAdapter mAdapter;
    private Cursor mCursor;
    private boolean mFirstItemVisible;

    private AsyncTask<Object, Object, Object> mAsyncTask;


    public static FeedsGridFragment newInstance(int sectionNumber) {
        mSectionNumber = sectionNumber;
        FeedsGridFragment fragment = new FeedsGridFragment();
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

        Set<String> keys = AppMainActivity.mFeeds.keySet();
        ArrayList<String> feed_like =  new ArrayList<String>(keys);
        Collections.sort(feed_like);

        mString = AppMainActivity.mFeeds.get(feed_like.get(mSectionNumber));

        mFileName = mString.substring(mString.lastIndexOf("/") + 1, mString.lastIndexOf(".txt"));

        CLog.i("mString: " + mString);

        CLog.i("fileName: " + mFileName);

        DataProvider.reInitArgs(mFileName);
        FeedsDataHelper.FeedsDBInfo.TABLE_NAME = "feeds" + mFileName;
        mDataHelper = new FeedsDataHelper(App.getContext());
        mDataHelper.setTableName(mFileName);

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
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState){}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if(mGridView != null && mGridView.getChildCount() > 0){
                    // check if the first item of the list is visible
//                    boolean firstItemVisible = mGridView.getChildVisibleRect();
                    // check if the top of the first item is visible
                    mFirstItemVisible = mGridView.getFirstPosition() == 0;
//                    boolean topOfFirstItemVisible = mGridView.getChildAt(0).() <= mGridView.getPaddingTop();
                }
                mSwipeLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeLayout.setEnabled(mFirstItemVisible);
                    }
                }, 300);
//                mSwipeLayout.setEnabled(enable);
            }
        });
//        adapter.notifyDataSetChanged();
        
//        mSwipeLayout.setColorSchemeResources(R.color.material_700, R.color.material_500);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.material_700, R.color.material_500);

        return contentView;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSwipeLayout!=null) {
            mSwipeLayout.setRefreshing(false);
            mSwipeLayout.destroyDrawingCache();
            mSwipeLayout.clearAnimation();
        }

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
    }

    public FeedsDataHelper getDataHelper() {
        return mDataHelper;
    }

    public Feed getFeed(int pos) {
        int i;
        for(i = 0; i < mIndexList.size(); i++) {
            if(mIndexList.get(i) > pos) break;
        }
        mCursor.moveToPosition(i);
        return Feed.fromCursor(mCursor);
    }

    private void refreshData() {
        if (!mSwipeLayout.isRefreshing()) {
            mSwipeLayout.setRefreshing(true);
        }
//        CLog.d("Refresh:"+getRefreshUrl());
        executeRequest(new GsonRequest(mString, Feed[].class, responseListener(), errorListener()));
    }

    private Response.Listener<Feed[]> responseListener() {
        return new Response.Listener<Feed[]>() {
            @Override
            public void onResponse(final Feed[] response) {
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        List<Feed> feeds = Arrays.asList(response);
                        if (feeds != null && feeds.size() > 0) {
                            Collections.sort(feeds);
                            for (Feed feed : feeds) {
                                CLog.d("after sort " + feed.getDate());
                            }
                            if (mLatest == null) {
                                mDataHelper.bulkInsert(feeds);
                            } else {
                                CLog.d("current latest date " + mLatest);
                                List<Feed> feedFilter = new ArrayList<Feed>();
                                for (Feed feed : feeds) {
                                    if (feed.getDate().compareTo(mLatest) > 0) feedFilter.add(feed);
                                }
                                if (feedFilter.size() > 0)
                                    mDataHelper.bulkInsert(feedFilter);
                            }
                            mLatest = feeds.get(0).getDate();

//                            int num1 = feeds.get(0).getId();
//                            int num2 = feeds.get(feeds.size()-1).getId();
//                            if(num1>mMaxId) {
//                                mMaxId = num1;
//                            }
//                            if(mSinceId == 0|| num1<mSinceId) {
//                                mSinceId = num1;
//                            }
//                            if(num2>mMaxId) {
//                                mMaxId = num2;
//                            }
//                            if(mSinceId == 0|| num2<mSinceId) {
//                                mSinceId = num2;
//                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        mSwipeLayout.setRefreshing(false);
//                        mListView.setState(LoadingFooter.State.Idle, 3000);
                        getLoaderManager().restartLoader(0, null, FeedsGridFragment.this);
                    }
                });
            }
        };
    }

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

    public void scrollTopAndRefresh() {
        if (mGridView != null) {
//            ListViewUtils.smoothScrollListViewToTop(mListView);
            mGridView.setSelectionToTop();
            refreshData();
        }
    }


    @Override
    public void onRefresh() {
        refreshData();
    }

     @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return mDataHelper.getCursorLoader();
    }

    private void loadFeedFromDB(int count) {
        int imgSize = 0;
        if(mCursor == null) {
            return;
        }

        while (mCursor != null && !mCursor.isAfterLast()) {
            Feed feed = Feed.fromCursor(mCursor);
            imgSize += feed.getImgs().size();
            mIndexList.add(imgSize);
            mUrls.addAll(feed.getImgs());
            mCursor.moveToNext();
            if (mCursor.getPosition() == 0) {
                mLatest = feed.getDate();
            }
            if(count > 0 && mCursor.getPosition() == count) {
                break;
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//            mAdapter.changeCursor(data);
        CLog.d("onLoadFinished begin");
        if (data != null && data.getCount() == 0) {
            refreshData();
        } else {
            mUrls.clear();
            mIndexList.clear();
            mCursor = data;
            mCursor.moveToFirst();
            if (mCursor.getCount() > 50) {
                loadFeedFromDB(30);
                mAsyncTask = new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        loadFeedFromDB(0);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
//                        int index = mGridView.getFirstPosition();
//                        View v = mGridView.getChildAt(0);
//                        int top = (v == null) ? 0 : v.getTop();

                        mAdapter.notifyDataSetChanged();

//                        mGridView.setSelectionToTop();
                        mAsyncTask = null;
                    }
                };
                TaskUtils.executeAsyncTask(mAsyncTask);
            } else {
                loadFeedFromDB(0);
            }
            mAdapter.notifyDataSetChanged();
        }
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

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        if (mAsyncTask != null) {
//            mAsyncTask.cancel(true);
//        }
//    }
}
