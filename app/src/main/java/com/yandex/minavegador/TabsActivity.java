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

import com.yandex.minavegador.db.DbNotificationManager;
import com.yandex.minavegador.db.DbProvider;
import com.yandex.minavegador.db.FakeContainer;
import com.yandex.minavegador.utils.Utils;

import java.util.ArrayList;

public class TabsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_NEW_TAB = 0;

    private ArrayList<Tab> mTabs = new ArrayList<>();
    private Tab mCurrentTab = null;
    private WebView mWebView;
    private AsyncTask<Void, Void, Bitmap> mIconLoadWebTask;
    private DbProvider.ResultCallback<Bitmap> mIconLoadDbCallback;

    private DbProvider mDbProvider;
    private DbNotificationManager mNotifier;
    private DbNotificationManager.Listener mDbListener = new DbNotificationManager.Listener() {
        @Override
        public void onDataUpdated() {
            // TODO
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbProvider = FakeContainer.getProviderInstance(this);
        mNotifier = FakeContainer.getNotificationInstance(this);

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
                String faviconUrl = Utils.getFaviconUrlForPage(url);
                onUrlLoaded(url, faviconUrl);
            }
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                onUrlStarted(url);
        }
        });
        if (savedInstanceState == null) {
            openNewTab();
        }

        mNotifier.addListener(mDbListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelLoadingFaviconFromWeb();
        mNotifier.removeListener(mDbListener);
    }

    private void openNewTab() {
        startActivityForResult(new Intent(this, NewTabActivity.class), REQUEST_CODE_NEW_TAB);
    }

    private void showHistory() {}

    private void onNewTabOpened(String url, String searchQuery) {
        Tab tab = new Tab();
        tab.backStackUrls.add(new MyUrl(url, searchQuery, true));
        mTabs.add(tab);
        mCurrentTab = tab;
        cancelLoadingFaviconFromWeb();
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

    private void onUrlStarted(String url) {
        cancelLoadingFaviconFromDb();
        loadIconFromDb(url);
    }

    private void onUrlLoaded(String url, String iconUrl) {
        if (mCurrentTab == null) {
            return;
        }

        cancelLoadingFaviconFromWeb();

        loadIconFromWeb(url, iconUrl);

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
        mDbProvider.insertHistoryItem(urla.url);
    }

    private boolean closeCurrentTab() {
        if (mCurrentTab == null) {
            return true;
        }
        cancelLoadingFaviconFromWeb();
        cancelLoadingFaviconFromDb();

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

    private void loadIconFromDb(final String pageUrl) {
        if (mCurrentTab == null) {
            return;
        }
        cancelLoadingFaviconFromDb();

        mIconLoadDbCallback = new DbProvider.ResultCallback<Bitmap>() {
            @Override
            public void onFinished(Bitmap result) {
                if (mIconLoadDbCallback != this) {
                    return;
                }
                onIconLoadedFromDb(pageUrl, result);
            }
        };
        mDbProvider.getFavicon(pageUrl, mIconLoadDbCallback);
    }

    private void cancelLoadingFaviconFromDb() {
        mIconLoadDbCallback = null;
    }

    private void onIconLoadedFromDb(String pageUrl, Bitmap image) {
        cancelLoadingFaviconFromDb();
        if (image == null)
            return;
        if (mCurrentTab.getCurrentUrl().url.equalsIgnoreCase(pageUrl)) {
            setBarIcon(image);
        }
    }

    private void loadIconFromWeb(final String pageUrl, final String iconUrl) {
        if (mCurrentTab == null) {
            return;
        }
        cancelLoadingFaviconFromWeb();

        mIconLoadWebTask = new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return Utils.loadBitmap(getBaseContext(), iconUrl);
            }
            @Override
            protected void onPostExecute(Bitmap result) {
                if (isCancelled()) return;
                onIconLoadedFromWeb(pageUrl, iconUrl, result);
            }
        };
        mIconLoadWebTask.execute();
    }

    private void cancelLoadingFaviconFromWeb() {
        if (mIconLoadWebTask != null)
            mIconLoadWebTask.cancel(false);
        mIconLoadWebTask = null;
    }

    private void onIconLoadedFromWeb(String pageUrl, String iconUrl, Bitmap image) {
        cancelLoadingFaviconFromWeb();
        if (image == null)
            return;
        cancelLoadingFaviconFromDb();  // no need after a new icon fetched
        if (mCurrentTab.getCurrentUrl().url.equalsIgnoreCase(pageUrl)) {
            setBarIcon(image);
        }
        mDbProvider.insertFavicon(pageUrl, iconUrl, image);
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
            case R.id.action_history:
                showHistory();
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
