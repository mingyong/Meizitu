package me.isming.meizitu.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import me.isming.meizitu.util.CLog;
import me.isming.meizitu.util.DeviceUtil;


public class AppMainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private static final String PREF_FEED_NAME = "allFeeds";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private BaseFragment mContentFragment;

    private MenuItem mMenu;

    private Map<String, String> mFeeds = new HashMap<String, String>();

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        SharedPreferences feeds = getPreferences(Context.MODE_PRIVATE);
        String json_feeds = feeds.getString(PREF_FEED_NAME, null);
        if (json_feeds == null) {
            mFeeds.put("haixiu", "http://23.252.109.110:5000/results/dump/haixiuzu2.txt");
        } else {
            mFeeds = new Gson().fromJson(json_feeds, new TypeToken<Map<String, String>>() {
            }.getType());
        }

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
        editor.commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (position == 0) {
            mContentFragment = FeedsFragment.newInstance(position + 1);
        }

        if (position == 1) {
            mContentFragment = LikesFragment.newInstance(position + 1);
//            mMenu.setVisible(false);
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, mContentFragment)
                .commit();
    }

//    public void disableFreshMenu() {
//        mMenu.setVisible(false);
//    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_new);
                break;
            case 2:
                mTitle = getString(R.string.title_like);
                break;
        }
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
            if (mContentFragment != null && mContentFragment instanceof LikesFragment) {
                mMenu.setVisible(false);
            }
//
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("关于").setMessage("本程序图片来自网上，包括但不限于妹子图（www.meizitu.com）," +
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.action_add));
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText titleBox = new EditText(this);
            titleBox.setHint("Title");
            layout.addView(titleBox);

            final EditText descriptionBox = new EditText(this);
            descriptionBox.setHint("URL");
            layout.addView(descriptionBox);

            builder.setView(layout);

            builder.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // get user input and set it to result
                            // edit text
//                            result.setText(userInput.getText());
                            CLog.d(titleBox.getText());
                            if (titleBox.getText().toString().trim() != "" && Patterns.WEB_URL.matcher(descriptionBox.getText().toString().trim()).matches()) {
                                mFeeds.put(titleBox.getText().toString().trim(), descriptionBox.getText().toString().trim());
                            }
//                            Toast.makeText(AppMainActivity.this, "Get URL", Toast.LENGTH_LONG);
                        }
                    })
                    .setNegativeButton(getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("Main"); //统计页面
        MobclickAgent.onResume(this);          //统计时长
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Main"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

}
