package com.yandex.minavegador.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.support.annotation.VisibleForTesting;

/**
 * Created by user on 31/07/2016.
 */
class DbBackend implements DbContract {

    private final DbOpenHelper mDbOpenHelper;

    DbBackend(Context context) {
        mDbOpenHelper = new DbOpenHelper(context);
    }

    @VisibleForTesting
    DbBackend(DbOpenHelper dbOpenHelper) {
        mDbOpenHelper = dbOpenHelper;
    }

    public Cursor getHistorySuggestions(String inputUserText) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        String tables = HISTORY + " LEFT JOIN " + PAGES + " ON " +
                HISTORY + "." + History.PAGE_ID + "=" + PAGES + "." + Pages.PAGE_ID;
        String[] columns = new String[] {HISTORY + "." + History.ID + " AS _id", Pages.PAGE_URL};
        String where = inputUserText == null
                ? null : Pages.PAGE_URL + " LIKE ?";
        String[] whereArgs = inputUserText == null
                ? null : new String[] {"%" + inputUserText + "%"};
        String orderBy = History.LAST_VISITED + " DESC";

        Cursor c = db.query(tables, columns,
                where, whereArgs, null, null, orderBy);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public void insertHistoryItem(String url) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final long timeMs = System.currentTimeMillis();
            Long pageId = queryPageByUrl(db, url);
            if (pageId == null) {
                pageId = insertPage(db, url);
                insertHistoryItem(db, pageId, timeMs);
            } else {
                Long historyItemId = queryHistoryItemByPageId(db, pageId);
                if (historyItemId == null) {
                    insertHistoryItem(db, pageId, timeMs);
                } else {
                    updateHistoryItemLastAccess(db, historyItemId, timeMs);
                }
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    // GOOD example
    private Long queryPageByUrl(SQLiteDatabase db, String url) {
        Cursor c = db.query(
                PAGES, new String[] { Pages.PAGE_ID },  // => SELECT page_id FROM pages
                Pages.PAGE_URL + "=?", new String[] { url },  // => WHERE page_url='url'
                null, null, null);
        return DbUtils.getResultLongAndClose(c);
    }

    // GOOD example
    private long insertPage(SQLiteDatabase db, String url) {
        ContentValues values = new ContentValues();
        values.put(Pages.PAGE_URL, url);
        return db.insert(PAGES, null, values);
    }

    // BAD example
    private Long queryHistoryItemByPageId(SQLiteDatabase db, long pageId) {
        Cursor c = db.rawQuery("SELECT " + History.PAGE_ID + " FROM " + HISTORY +
                               " WHERE " + History.PAGE_ID + "=" + pageId,
                               null);  // bind args
        return DbUtils.getResultLongAndClose(c);
    }

    // GOOD or BAD example?
    private long insertHistoryItem(SQLiteDatabase db, long pageId, long ms) {
        String sql =
                "INSERT INTO " + HISTORY +
                        "(" + History.PAGE_ID + "," + History.LAST_VISITED + ")" +
                        " VALUES (?,?)";
        SQLiteStatement stm = db.compileStatement(sql);
        // 1-based index !!!
        stm.bindLong(1, pageId);
        stm.bindLong(2, ms);
        return stm.executeInsert();
    }

    // BAD example
    private int updateHistoryItemLastAccess(SQLiteDatabase db, long historyItemId, long ms) {
        ContentValues values = new ContentValues();
        values.put(History.LAST_VISITED, ms);
        return db.update(
                HISTORY,       // table
                values,         // column-value map
                History.ID + "=?",  // WHERE rowid=?
                new String[] { String.valueOf(historyItemId) });  // bind arguments
    }

    public Bitmap getFavicon(String pageUrl) {
        // TODO: implement me!
        return null;
    }

    public void insertFavicon(String pageUrl, String iconUrl) {
        // TODO: implement me!
    }
}
