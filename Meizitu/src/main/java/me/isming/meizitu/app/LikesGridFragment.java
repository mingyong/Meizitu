package me.isming.meizitu.app;

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


import me.isming.meizitu.App;
import me.isming.meizitu.adapter.CardsAnimationAdapter;
import me.isming.meizitu.adapter.FeedsAdapter;
import me.isming.meizitu.adapter.StaggeredAdapter;
import me.isming.meizitu.dao.LikesDataHelper;
import me.isming.meizitu.model.Feed;
import me.isming.meizitu.util.CLog;
import me.isming.meizitu.util.ListViewUtils;
import me.isming.meizitu.view.PageListView;

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

    private StaggeredAdapter mAdapter;
    
    private String urls[] = { 
			"http://farm7.staticflickr.com/6101/6853156632_6374976d38_c.jpg",
			"http://farm8.staticflickr.com/7232/6913504132_a0fce67a0e_c.jpg",
			"http://farm5.staticflickr.com/4133/5096108108_df62764fcc_b.jpg",
			"http://farm5.staticflickr.com/4074/4789681330_2e30dfcacb_b.jpg",
			"http://farm9.staticflickr.com/8208/8219397252_a04e2184b2.jpg",
			"http://farm9.staticflickr.com/8483/8218023445_02037c8fda.jpg",
			"http://farm9.staticflickr.com/8335/8144074340_38a4c622ab.jpg",
			"http://farm9.staticflickr.com/8060/8173387478_a117990661.jpg",
			"http://farm9.staticflickr.com/8056/8144042175_28c3564cd3.jpg",
			"http://farm9.staticflickr.com/8183/8088373701_c9281fc202.jpg",
			"http://farm9.staticflickr.com/8185/8081514424_270630b7a5.jpg",
			"http://farm9.staticflickr.com/8462/8005636463_0cb4ea6be2.jpg",
			"http://farm9.staticflickr.com/8306/7987149886_6535bf7055.jpg",
			"http://farm9.staticflickr.com/8444/7947923460_18ffdce3a5.jpg",
			"http://farm9.staticflickr.com/8182/7941954368_3c88ba4a28.jpg",
			"http://farm9.staticflickr.com/8304/7832284992_244762c43d.jpg",
			"http://farm9.staticflickr.com/8163/7709112696_3c7149a90a.jpg",
			"http://farm8.staticflickr.com/7127/7675112872_e92b1dbe35.jpg",
			"http://farm8.staticflickr.com/7111/7429651528_a23ebb0b8c.jpg",
			"http://farm9.staticflickr.com/8288/7525381378_aa2917fa0e.jpg",
			"http://farm6.staticflickr.com/5336/7384863678_5ef87814fe.jpg",
			"http://farm8.staticflickr.com/7102/7179457127_36e1cbaab7.jpg",
			"http://farm8.staticflickr.com/7086/7238812536_1334d78c05.jpg",
			"http://farm8.staticflickr.com/7243/7193236466_33a37765a4.jpg",
			"http://farm8.staticflickr.com/7251/7059629417_e0e96a4c46.jpg",
			"http://farm8.staticflickr.com/7084/6885444694_6272874cfc.jpg"
	};


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
//        adapter.notifyDataSetChanged();
        
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

//    public void scrollTopAndRefresh() {
//        if (mListView != null) {
//            ListViewUtils.smoothScrollListViewToTop(mListView);
//        }
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return mDataHelper.getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//            mAdapter.changeCursor(data);
        data.moveToFirst();
        while (!data.isAfterLast()) {
            Feed feed = Feed.fromCursor(data);
            mUrls.addAll(feed.getImgs());
            data.moveToNext();
        }

        for(String url : mUrls) {
            CLog.d("img url: " + url);
        }

        mAdapter.notifyDataSetChanged();
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
