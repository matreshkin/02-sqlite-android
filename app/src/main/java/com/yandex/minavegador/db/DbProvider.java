package com.yandex.minavegador.db;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 31/07/2016.
 */
public class DbProvider {

    private final DbBackend mDbBackend;
    private final DbNotificationManager mDbNotificationManager;
    private final CustomExecutor mExecutor;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public interface ResultCallback<T> {
        void onFinished(T result);
    }

    DbProvider(Context context) {
        mDbBackend = new DbBackend(context);
        mDbNotificationManager = FakeContainer.getNotificationInstance(context);
        mExecutor = new CustomExecutor();
    }

    @VisibleForTesting
    DbProvider(DbBackend dbBackend,
               DbNotificationManager dbNotificationManager,
               CustomExecutor executor) {
        mDbBackend = dbBackend;
        mDbNotificationManager = dbNotificationManager;
        mExecutor = executor;
    }

    public void getHistorySuggestions(final String inputUserText,
                                      final ResultCallback<Cursor> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final Cursor c =  mDbBackend.getHistorySuggestions(inputUserText);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinished(c);
                    }
                });
            }
        });
    }

    public void insertHistoryItem(final String url) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbBackend.insertHistoryItem(url);
                mDbNotificationManager.notifyListeners();
            }
        });
    }

    public void getFavicon(final String pageUr,
                           final ResultCallback<Bitmap> callback) {
        // TODO: implement me!
    }

    public void insertFavicon(final String pageUrl, final String iconUrl, final Bitmap image) {
        // TODO: implement me!
    }

    // TODO: make me multi-threaded!
    class CustomExecutor extends ThreadPoolExecutor {
        CustomExecutor() {
            super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        }
    }
}
