package me.isming.meizitu.app;

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
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import me.isming.meizitu.App;
import me.isming.meizitu.adapter.CardsAnimationAdapter;
import me.isming.meizitu.adapter.FeedsAdapter;
import me.isming.meizitu.dao.DataProvider;
import me.isming.meizitu.dao.FeedsDataHelper;
import me.isming.meizitu.data.GsonRequest;
import me.isming.meizitu.model.Feed;
import me.isming.meizitu.util.ActionBarUtils;
import me.isming.meizitu.util.CLog;
import me.isming.meizitu.util.ListViewUtils;
import me.isming.meizitu.util.TaskUtils;
import me.isming.meizitu.view.LoadingFooter;
import me.isming.meizitu.view.PageListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Created by Sam on 14-3-25.
 */
public class FeedsFragment extends BaseFragment implements  LoaderManager.LoaderCallbacks<Cursor>,SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_SECTION_NUMBER = "section_number";

    SwipeRefreshLayout mSwipeLayout;

    PageListView mListView;
    private FeedsDataHelper mDataHelper;
    private FeedsAdapter mAdapter;
    private int mMaxId = 0;
    private int mSinceId = 0;
    private static int mSectionNumber = 0;
    private String mLatest = null;
    private String mString = "http://23.252.109.110:5000/results/dump/haixiuzu2.txt";
    private String mFileName;

    public static FeedsFragment newInstance(int sectionNumber) {
        mSectionNumber = sectionNumber;
        FeedsFragment fragment = new FeedsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_feed, container, false);

        mListView = (PageListView)contentView.findViewById(R.id.listView);
        mSwipeLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);

        mSwipeLayout.setSize(SwipeRefreshLayout.LARGE);

        Set<String> keys = AppMainActivity.mFeeds.keySet();
        ArrayList<String> feed_like =  new ArrayList<String>(keys);
        Collections.sort(feed_like);

        mString = AppMainActivity.mFeeds.get(feed_like.get(mSectionNumber));

        mFileName = mString.substring(mString.lastIndexOf("/") + 1, mString.lastIndexOf(".txt"));

        CLog.i("mString: " + mString);

        CLog.i("fileName: " + mFileName);

//        mFileName = "haixiuzu2";

        DataProvider.reInitArgs(mFileName);
        FeedsDataHelper.FeedsDBInfo.TABLE_NAME = "feeds" + mFileName;
        mDataHelper = new FeedsDataHelper(App.getContext());
        mDataHelper.setTableName(mFileName);
        CLog.i("content_uri: " + DataProvider.FEEDS_CONTENT_URI.getPath());
//        mDataHelper.notifyChange();
//        getLoaderManager().initLoader(0, null, this);
        mAdapter = new FeedsAdapter(getActivity(), mListView);
//        mAdapter.notifyDataSetChanged();
//        mListView.setEmptyView();
//        mAdapter.changeCursor(null);
        View header = new View(getActivity());
        mListView.addHeaderView(header);
        AnimationAdapter animationAdapter = new CardsAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(mListView);
        mListView.setAdapter(animationAdapter);
//        mListView.setLoadNextListener(new PageListView.OnLoadNextListener() {
//            @Override
//            public void onLoadNext() {
//                loadNextData();
//            }
//        });
//        getLoaderManager().initLoader(0, null, this);
        mListView.setLoadNextListener(null);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int actualPosition = position - mListView.getHeaderViewsCount();
                if(actualPosition<0) {
                    return;
                }

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());

                Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                Feed feed = mAdapter.getItem(position-mListView.getHeaderViewsCount());
                if (feed == null) {
                    return;
                }
                intent.putExtra(ImageViewActivity.IMAGE_NAME, feed.getTitle());
                intent.putStringArrayListExtra(ImageViewActivity.IMAGE_URL, feed.getImgs());
                intent.putExtra(ImageViewActivity.IMAGE_ID, feed.getId().toString());
                intent.putExtra(ImageViewActivity.IMAGE_AUTHOR, new Gson().toJson(feed.getAuthor()));
                intent.putExtra(ImageViewActivity.IMAGE_DATE, feed.getDate());
                intent.putExtra(ImageViewActivity.IMAGE_ORIGINURL, feed.getUrl());
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }
        });

        initActionBar();
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.material_700, R.color.material_500);

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSwipeLayout!=null) {
            mSwipeLayout.setRefreshing(false);
            mSwipeLayout.destroyDrawingCache();
            mSwipeLayout.clearAnimation();
        }
    }

    public FeedsDataHelper getDataHelper() {
        return mDataHelper;
    }

    private void initActionBar() {
        //View actionBarContainer = ActionBarUtils.findActionBarContainer(getActivity());
//        if(actionBarContainer == null) {
//            CLog.i("actionBarContainer为空，直接返回了");
//            return;
//        }
//
//        actionBarContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ListViewUtils.smoothScrollListViewToTop(mListView);
//            }
//        });
    }

    private String getRefreshUrl() {
        return mString + "?max_id=" + mMaxId;
    }

    private String getNextUrl() {
        return mString + "?since_id=" + mSinceId;
    }


    private void loadNextData() {
        if (!mSwipeLayout.isRefreshing()) {
            mSwipeLayout.setRefreshing(true);
        }
        CLog.d("NExt"+getNextUrl());
        executeRequest(new GsonRequest(getNextUrl(), Feed[].class, responseListener(), errorListener()));
    }

    private void refreshData() {
        if (!mSwipeLayout.isRefreshing()) {
            mSwipeLayout.setRefreshing(true);
        }
        CLog.d("Refresh:"+getRefreshUrl());
        executeRequest(new GsonRequest(getRefreshUrl(), Feed[].class, responseListener(), errorListener()));
    }

    private Response.Listener<Feed[]> responseListener() {
        return new Response.Listener<Feed[]>() {
            @Override
            public void onResponse(final Feed[] response) {
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        List<Feed> feeds = Arrays.asList(response);
                        if(feeds != null && feeds.size()>0) {
                            Collections.sort(feeds);
                            for(Feed feed : feeds) {
                                CLog.d("after sort " + feed.getDate());
                            }
                            if (mLatest == null) {
                                mDataHelper.bulkInsert(feeds);
                            } else {
                                CLog.d("current latest date " + mLatest);
                                List<Feed> feedFilter = new ArrayList<Feed>();
                                for(Feed feed : feeds) {
                                    if(feed.getDate().compareTo(mLatest) > 0) feedFilter.add(feed);
                                }
                                if(feedFilter.size() > 0)
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
                            mListView.setState(LoadingFooter.State.Idle, 3000);
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
                mListView.setState(LoadingFooter.State.Idle, 3000);
            }
        };
    }

    public void scrollTopAndRefresh() {
        if (mListView != null) {
            ListViewUtils.smoothScrollListViewToTop(mListView);
            refreshData();
        }
    }


    @Override
    public void onRefresh() {
        refreshData();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CLog.d("onCreateLoader");
        return mDataHelper.getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        if (data != null && data.getCount() == 0) {
            refreshData();
        } else {
            if(mLatest == null)
                mLatest = mAdapter.getItem(0).getDate();
            CLog.d("onLoadFinished " + mLatest);
//            int num1 = mAdapter.getItem(mAdapter.getCount() -1 ).getId();
//            int num2 = mAdapter.getItem(0).getId();
//            if(num1 > num2) {
//                mMaxId = num1;
//                mSinceId = num2;
//            } else {
//                mMaxId = num2;
//                mSinceId = num1;
//            }
//            CLog.d(num1+""+num2);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        if (mAdapter != null) mAdapter.changeCursor(null);
        ((AppMainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//       if (mAdapter != null) mAdapter.changeCursor(null);
//
//    }

//    private void initDBArgs(int sectionNumber) {
//        DataProvider.PATH_FEEDS += Integer.toString(sectionNumber);
////        DataProvider.FEEDS_CONTENT_URI
//    }
}
