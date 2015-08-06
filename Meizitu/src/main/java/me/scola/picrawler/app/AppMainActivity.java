package me.scola.picrawler.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.scola.picrawler.util.CLog;
import me.scola.picrawler.util.DeviceUtil;


public class AppMainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public static final String PREF_FEED_NAME = "allFeeds";
    public static final String PREF_GRID = "grid";
    private static final int REQUEST_CODE = 10;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private BaseFragment mContentFragment;

    private MenuItem mMenu;

    private boolean mGrid;
    private boolean mNewFeedAdd;

    private int mPosition;

    public static Map<String, String> mFeeds = new HashMap<String, String>();

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

//    public static Map<String, String> getFeeds() {
//        return mFeeds;
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        MobclickAgent.updateOnlineConfig(this);
        UmengUpdateAgent.update(this);
        CLog.i(DeviceUtil.getDeviceInfo(this));
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences feeds = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = feeds.edit();
        editor.putString(PREF_FEED_NAME, new Gson().toJson(mFeeds));
        editor.putBoolean(PREF_GRID, mGrid);
        editor.commit();
    }

    @Override
    public void setGrid(boolean mGrid) {
        this.mGrid = mGrid;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        mPosition = position;
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
//        if (position == 0) {
//            mContentFragment = FeedsFragment.newInstance(position + 1);
//        }

        if (position == mFeeds.size()) {
            mContentFragment = mGrid ? LikesGridFragment.newInstance(position) : LikesFragment.newInstance(position);
//            mMenu.setVisible(false);
        } else {
            mContentFragment = mGrid ? FeedsGridFragment.newInstance(position) : FeedsFragment.newInstance(position);
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, mContentFragment)
                .commit();
    }

    @Override
    public void deleteTable() {
        if(mContentFragment == null) return;

        if(mContentFragment instanceof FeedsGridFragment) {
            ((FeedsGridFragment)mContentFragment).getDataHelper().deleteAll();
        } else if(mContentFragment instanceof FeedsFragment){
            ((FeedsFragment)mContentFragment).getDataHelper().deleteAll();
        }
    }

//    public void disableFreshMenu() {
//        mMenu.setVisible(false);
//    }
    public int getPosition() {
        return mPosition;
    }

    public void onSectionAttached(int number) {
        if(number > mFeeds.size()) return;
        mPosition = number;
        if(number == mFeeds.size()) mTitle = getString(R.string.title_like);
        else {
            ArrayList<String> keys = new ArrayList<String>(mFeeds.keySet());
            Collections.sort(keys);
            mTitle = keys.get(number);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);

//        switch (number) {
//            case 1:
//
//                break;
//            case 2:
//                mTitle = getString(R.string.title_like);
//                break;
//        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        mMenu = menu;
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            mMenu = menu.findItem(R.id.action_refresh);
            if (mContentFragment != null && mContentFragment instanceof LikesGridFragment) {
                mMenu.setVisible(false);
            }
//
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data.hasExtra("feed_name") && data.hasExtra("feed_url")) {
                String feed_name = data.getExtras().getString("feed_name");
                String feed_url = data.getExtras().getString("feed_url");

                mFeeds.put(feed_name, feed_url);
                mNavigationDrawerFragment.addFeedAndUpdate(feed_name);

                Set<String> keys = AppMainActivity.mFeeds.keySet();
                ArrayList<String> feeds =  new ArrayList<String>(keys);
                Collections.sort(feeds);

                FragmentManager fragmentManager = getSupportFragmentManager();
                mContentFragment = mGrid ? FeedsGridFragment.newInstance(feeds.indexOf(feed_name)) : FeedsFragment.newInstance(feeds.indexOf(feed_name));
                mNewFeedAdd = true;
                mTitle = feed_name;
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle(mTitle);


//                fragmentManager.beginTransaction()
//                        .replace(R.id.container, mContentFragment)
//                        .commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("关于").setMessage("本程序图片来自网上，" +
                    "程序用于个人娱乐，严禁用于商业目的。所有图片等资源的版权归原作者所有。");
            builder.create().show();
            return true;
        } else if (id == R.id.action_feedback) {
            FeedbackAgent agent = new FeedbackAgent(this);
            agent.startFeedbackActivity();
            return true;
        } else if(id == R.id.action_refresh) {
            if (mContentFragment != null) {
                mContentFragment.scrollTopAndRefresh();
            }
        } else if(id == R.id.action_add) {
            Intent i = new Intent(this, AddFeedActivity.class);
            i.putStringArrayListExtra(AddFeedActivity.FEED_KEY, new ArrayList<String>(mFeeds.keySet()));
            startActivityForResult(i, REQUEST_CODE);


//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle(getString(R.string.action_add));
//            LinearLayout layout = new LinearLayout(this);
//            layout.setOrientation(LinearLayout.VERTICAL);
//
//            final EditText titleBox = new EditText(this);
//            titleBox.setHint("Title");
//            layout.addView(titleBox);
//
//            final EditText descriptionBox = new EditText(this);
//            descriptionBox.setHint("URL");
//            layout.addView(descriptionBox);
//
//            builder.setView(layout);
//
//            builder.setPositiveButton(getString(android.R.string.ok),
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            // get user input and set it to result
//                            // edit text
////                            result.setText(userInput.getText());
//                            CLog.d(titleBox.getText());
//                            String title = titleBox.getText().toString().trim();
//                            if (title != "" && Patterns.WEB_URL.matcher(descriptionBox.getText().toString().trim()).matches()) {
//                                mFeeds.put(title, descriptionBox.getText().toString().trim());
//                                mNavigationDrawerFragment.addFeedAndUpdate(title);
//
//                                Set<String> keys = AppMainActivity.mFeeds.keySet();
//                                ArrayList<String> feeds =  new ArrayList<String>(keys);
//                                Collections.sort(feeds);
//
//                                FragmentManager fragmentManager = getSupportFragmentManager();
//                                mContentFragment = FeedsGridFragment.newInstance(feeds.indexOf(title));
//
//
//                                fragmentManager.beginTransaction()
//                                        .replace(R.id.container, mContentFragment)
//                                        .commit();
//                            }
////                            Toast.makeText(AppMainActivity.this, "Get URL", Toast.LENGTH_LONG);
//                        }
//                    })
//                    .setNegativeButton(getString(android.R.string.cancel),
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    dialog.cancel();
//                                }
//                            });
//            builder.create().show();
        } else if(id == R.id.action_switch) {
            mGrid = !mGrid;
            if(mContentFragment == null)
                return super.onOptionsItemSelected(item);
            FragmentManager fragmentManager = getSupportFragmentManager();
//            mContentFragment = FeedsGridFragment.newInstance(feeds.indexOf(title));
            boolean grid = mContentFragment.getClass().getSimpleName().contains("Grid");
            boolean like = mContentFragment.getClass().getSimpleName().contains("Like");
            if (like == true && grid == false) {
                mContentFragment = LikesGridFragment.newInstance(mPosition);
            } else if (like == true && grid == true) {
                mContentFragment = LikesFragment.newInstance(mPosition);
            } else if (like == false && grid == true) {
                mContentFragment = FeedsFragment.newInstance(mPosition);
            } else if (like == false && grid == false) {
                mContentFragment = FeedsGridFragment.newInstance(mPosition);
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.container, mContentFragment)
                    .commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContentFragment != null && mNewFeedAdd) {          //当在AddFeedActivity press home key, 很长时间之后回来可能会FC
            mNewFeedAdd = false;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mContentFragment)
                    .commit();
        }

        MobclickAgent.onPageStart("Main"); //统计页面
        MobclickAgent.onResume(this);          //统计时长
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Main"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

}
