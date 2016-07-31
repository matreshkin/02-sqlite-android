package com.yandex.minavegador.db;

import android.database.sqlite.SQLiteDatabase;

import com.yandex.minavegador.BuildConfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Created by user on 31/07/2016.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DbBackendTest implements DbContract {

    @Test
    public void testInsertHistoryItem() {
        DbOpenHelper helper = new DbOpenHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = helper.getWritableDatabase();

        DbBackend dbBackend = new DbBackend(helper);

        // No page, no history item.
        // Both rows must be inserted.
        dbBackend.insertHistoryItem("yandex.ru");
        Assert.assertEquals(1, getCount(db, HISTORY));
        Assert.assertEquals(1, getCount(db, PAGES));

        // Has page, no history item.
        // History item must be inserted.
        db.rawQuery("delete from " + HISTORY, null);
        dbBackend.insertHistoryItem("yandex.ru");
        Assert.assertEquals(1, getCount(db, HISTORY));
        Assert.assertEquals(1, getCount(db, PAGES));

        // Has page, has history item.
        // LastAccess must be updated (1 second precision).
        long oldLastAccess = getOneField(db, HISTORY, History.LAST_VISITED);
        dbBackend.insertHistoryItem("yandex.ru");
        Assert.assertEquals(1, getCount(db, HISTORY));
        Assert.assertEquals(1, getCount(db, PAGES));
        long newLastAccess = getOneField(db, HISTORY, History.LAST_VISITED);
        Assert.assertTrue(Math.abs(newLastAccess - oldLastAccess) < 1000);

        // Check the second history item and the value of last access.
        final long was = System.currentTimeMillis();
        dbBackend.insertHistoryItem("yandex.com");
        Assert.assertEquals(2, getCount(db, HISTORY));
        Assert.assertEquals(2, getCount(db, PAGES));
        final long lastAccess = getOneField(db, HISTORY, History.LAST_VISITED);
        Assert.assertTrue(Math.abs(was - lastAccess) < 1000);
        final long now = System.currentTimeMillis();
        Assert.assertTrue(Math.abs(now - lastAccess) < 1000);
    }

    private int getCount(SQLiteDatabase db, String table) {
        return DbUtils.getResultLongAndClose(
                db.rawQuery("select count(*) from " + table, null)).intValue();
    }

    private long getOneField(SQLiteDatabase db, String table, String field) {
        return DbUtils.getResultLongAndClose(
                db.rawQuery("select " + field + " from " + table, null));
    }
}
