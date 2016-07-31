package com.yandex.minavegador.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 31/07/2016.
 */
class DbOpenHelper extends SQLiteOpenHelper implements DbContract {

    private static final int DB_VERSION = 1;

    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + PAGES + "(" +
                Pages.PAGE_ID + " INTEGER PRIMARY KEY, " +
                Pages.PAGE_URL + " TEXT UNIQUE NOT NULL, " +
                Pages.FAVICON_ID + " INTEGER" +
            ")");
        db.execSQL(
            "CREATE TABLE " + HISTORY + "(" +
                History.PAGE_ID + " INTEGER NOT NULL UNIQUE, " +
                History.LAST_VISITED + " INTEGER NOT NULL " +
            ")");
        db.execSQL(
            "CREATE TABLE " + FAVICONS + "(" +
                Favicons.ICON_ID + " INTEGER PRIMARY KEY, " +
                Favicons.ICON_URL + " TEXT, " +
                Favicons.LAST_UPDATED + " TEXT " +
            ")");
        db.execSQL("CREATE INDEX idx_" + History.IDX_LAST_VISITED +
                   " ON " + HISTORY + "(" + History.LAST_VISITED + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Updating for dev versions!
        db.execSQL("DROP TABLE " + PAGES);
        db.execSQL("DROP TABLE " + HISTORY);
        db.execSQL("DROP TABLE " + FAVICONS);
        onCreate(db);
    }
}
