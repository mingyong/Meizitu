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
import android.widget.AdapterView;

import com.google.gson.Gson;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;


import me.scola.picrawler.App;
import me.scola.picrawler.adapter.CardsAnimationAdapter;
import me.scola.picrawler.adapter.FeedsAdapter;
import me.scola.picrawler.dao.LikesDataHelper;
import me.scola.picrawler.model.Feed;
import me.scola.picrawler.util.ListViewUtils;
import me.scola.picrawler.view.PageListView;


/**
 * Created by Sam on 14-3-25.
 */
public class LikesFragment extends BaseFragment implements  LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_SECTION_NUMBER = "section_number";

    SwipeRefreshLayout mSwipeLayout;

    PageListView mListView;

    private LikesDataHelper mDataHelper;
    private FeedsAdapter mAdapter;


    public static LikesFragment newInstance(int sectionNumber) {
        LikesFragment fragment = new LikesFragment();
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

        mDataHelper = new LikesDataHelper(App.getContext());
        getLoaderManager().initLoader(0, null, this);
        mAdapter = new FeedsAdapter(getActivity(), mListView);
        View header = new View(getActivity());
        mListView.addHeaderView(header);
        AnimationAdapter animationAdapter = new CardsAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(mListView);
        mListView.setAdapter(animationAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int actualPosition = position - mListView.getHeaderViewsCount();
                if (actualPosition < 0) {
                    return;
                }

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());

                Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                Feed feed = mAdapter.getItem(position - mListView.getHeaderViewsCount());
                intent.putExtra(ImageViewActivity.IMAGE_NAME, feed.getTitle());
                intent.putStringArrayListExtra(ImageViewActivity.IMAGE_URL, feed.getImgs());
                intent.putExtra(ImageViewActivity.IMAGE_ID, feed.getId().toString());
                intent.putExtra(ImageViewActivity.IMAGE_AUTHOR, new Gson().toJson(feed.getAuthor()));
                intent.putExtra(ImageViewActivity.IMAGE_DATE, feed.getDate());
                intent.putExtra(ImageViewActivity.IMAGE_ORIGINURL, feed.getUrl());
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }
        });
        mListView.setLoadNextListener(null);

        initActionBar();
//        mSwipeLayout.setColorSchemeResources(R.color.material_700, R.color.material_500);
        mSwipeLayout.setEnabled(false);

        return contentView;
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

    public void scrollTopAndRefresh() {
        if (mListView != null) {
            ListViewUtils.smoothScrollListViewToTop(mListView);
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        getLoaderManager().restartLoader(0, null, this);
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mDataHelper.getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
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
}
