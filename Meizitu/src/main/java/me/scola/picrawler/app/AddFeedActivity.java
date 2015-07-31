package me.scola.picrawler.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;


public class AddFeedActivity extends BaseActivity {
    public static final String FEED_KEY = "feed_key";
    private ArrayList<String> feedKey;
    private EditText mNameTextView;
    private EditText mURLTextView;

    private Button mAddButton;

    private final static String ADD_FEED_DETAIL = "https://github.com/scola/Meizitu/blob/master/pyspider/pyspider.md";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mNameTextView = (EditText)findViewById(R.id.feed_name);
        mURLTextView = (EditText)findViewById(R.id.feed_addr);
        mAddButton = (Button)findViewById(R.id.add_feed);

        feedKey = getIntent().getStringArrayListExtra(FEED_KEY);

        setTitle(getString(R.string.action_add));

        mNameTextView.addTextChangedListener(TextWatcherNewInstance());
        mURLTextView.addTextChangedListener(TextWatcherNewInstance());

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("feed_name", mNameTextView.getText().toString().trim());
                intent.putExtra("feed_url", mURLTextView.getText().toString().trim());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    private TextWatcher TextWatcherNewInstance() {
        return new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String feedURL = mURLTextView.getText().toString().trim();
                String feedName = mNameTextView.getText().toString().trim();
                if(feedName.length() != 0 && !feedKey.contains(feedName) && feedURL.endsWith(".txt") && Patterns.WEB_URL.matcher(feedURL).matches()) {
                    mAddButton.setEnabled(true);
                } else {
                    mAddButton.setEnabled(false);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("AddFeed"); //统计页面
        MobclickAgent.onResume(this);          //统计时长
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("AddFeed"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_detail) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(ADD_FEED_DETAIL));
            startActivity(i);
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
