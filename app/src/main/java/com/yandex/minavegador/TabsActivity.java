package com.yandex.minavegador;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yandex.minavegador.utils.Utils;

import java.util.ArrayList;

public class TabsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_NEW_TAB = 0;

    private ArrayList<Tab> mTabs = new ArrayList<>();
    private Tab mCurrentTab = null;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewTab();
            }
        });

        mWebView = (WebView) findViewById(R.id.webveiw);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                onUrlLoaded(url);
            }
        });
        if (savedInstanceState == null) {
            openNewTab();
        }
    }

    private void openNewTab() {
        startActivityForResult(new Intent(this, NewTabActivity.class), REQUEST_CODE_NEW_TAB);
    }

    private void onNewTabOpened(String url, String searchQuery) {
        Tab tab = new Tab();
        tab.backStackUrls.add(new MyUrl(url, searchQuery, true));
        mTabs.add(tab);
        mCurrentTab = tab;
        loadCurrentContent();
        updateTabCounter();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentTab == null) {
            finish();
            return;
        }
        mCurrentTab.removeLast();
        if (!mCurrentTab.backStackUrls.isEmpty()) {
            loadCurrentContent();
            return;
        }
        if (closeCurrentTab()) {
            finish();
            return;
        }
    }

    private void onUrlLoaded(String url) {
        if (mCurrentTab == null) {
            return;
        }
        if (mCurrentTab.getCurrentUrl().pendingNavigation) {
            mCurrentTab.removeLast();
        }
        MyUrl urla = new MyUrl(url, null, false);
        mCurrentTab.backStackUrls.add(urla);

        if (urla.searchQuery != null) {
            getSupportActionBar().setTitle(urla.searchQuery);
        } else {
            getSupportActionBar().setTitle(mWebView.getTitle());
        }
    }

    private boolean closeCurrentTab() {
        if (mCurrentTab == null) {
            return true;
        }
        mTabs.remove(mCurrentTab);
        mCurrentTab = null;
        if (mTabs.isEmpty()) {
            return true;
        }
        mCurrentTab = mTabs.get(mTabs.size() - 1);
        loadCurrentContent();
        updateTabCounter();
        return false;
    }

    private void loadIcon() {
        if (mCurrentTab == null) {
            return;
        }
        final String url = mCurrentTab.getCurrentUrl().url;
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return Utils.getFavicon(getBaseContext(), url);
            }
            @Override
            protected void onPostExecute(Bitmap result) {
                if (isCancelled()) return;
                onIconLoaded(result);
            }
        }.execute();
    }

    private void onIconLoaded(Bitmap image) {
        setBarIcon(image);
    }

    private void setBarIcon(Bitmap image) {
        getSupportActionBar().setLogo(new BitmapDrawable(image));
    }

    private void loadCurrentContent() {
        assert !mTabs.isEmpty();
        assert mCurrentTab != null;
        mCurrentTab.getCurrentUrl().pendingNavigation = true;
        String url = mCurrentTab.getCurrentUrl().url;
        mWebView.loadUrl(url);
        loadIcon();
    }

    private void updateTabCounter() {
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_tabs, menu);
        menu.findItem(R.id.action_show_tabs).setTitle("[" + mTabs.size() + "]");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_new_tab:
                openNewTab();
                break;
            case R.id.action_close_tab:
                if (closeCurrentTab()) {
                    openNewTab();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_TAB:
                if (resultCode != RESULT_OK) {
                    if (mTabs.isEmpty())
                        finish();
                    return;
                };
                String url = data.getStringExtra(NewTabActivity.RESULT_URL);
                String searchQuery = data.getStringExtra(NewTabActivity.RESULT_QUERY);
                onNewTabOpened(url, searchQuery);
                break;
        }
    }

    private class MyUrl {
        final String url;
        final String searchQuery;
        boolean pendingNavigation = false;

        private MyUrl(String url, String searchQuery, boolean pendingNavigation) {
            if (searchQuery == null && url != null) {
                Uri uri = Uri.parse(url);
                if (uri.getHost() != null && uri.getHost().equalsIgnoreCase("yandex.ru")) {
                    searchQuery = uri.getQueryParameter("text");
                }
            } else if (url == null) {
                url = "https://yandex.ru/search/?text=" + searchQuery;
            }
            this.searchQuery = searchQuery;
            this.url = url;
            this.pendingNavigation = pendingNavigation;
        }
    }

    private class Tab {
        public ArrayList<MyUrl> backStackUrls = new ArrayList<>();

        public MyUrl getCurrentUrl() {
            return backStackUrls.get(backStackUrls.size() - 1);
        }

        public void removeLast() {
            if (backStackUrls.isEmpty()) return;
            backStackUrls.remove(backStackUrls.size() - 1);
        }
    }
}
