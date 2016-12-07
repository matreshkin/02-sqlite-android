package com.yandex.minavegador.db;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 31/07/2016.
 */
class DbUtils {
    public static Long getResultLongAndClose(Cursor c) {
        Long id = getResultLong(c, 0);
        closeCursor(c);
        return id;
    }

    public static List<String> getResultStringListAndClose(Cursor c, String columnName) {
        final List<String> resultStringList = getResultStringList(c, c.getColumnIndex(columnName));
        closeCursor(c);
        return resultStringList;
    }

    private static List<String> getResultStringList(Cursor c, int column) {
        List<String> lists = new ArrayList<>();
        if(c != null && (c.isFirst() || c.moveToFirst())) {
            do {
                if (!c.isNull(column)) {
                    lists.add(c.getString(column));
                }
            }
            while (c.moveToNext());
        }
        return lists;
    }

    public static Long getResultLong(Cursor c) {
        return getResultLong(c, 0);
    }

    public static Long getResultLong(Cursor c, int column) {
        if (c != null && c.moveToFirst()) {
            return c.isNull(column) ? null : c.getLong(column);
        }
        return null;
    }

    public static void closeCursor(Cursor c) {
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }
}
