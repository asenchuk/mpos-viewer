package net.taviscaron.mposviewer.util;

import android.database.Cursor;

/**
 * Different db/cursor utils
 * @author Andrei Senchuk
 */
public class DBUtils {
    public static String getString(Cursor cursor, String name) {
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }

    public static int getInt(Cursor cursor, String name) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(name));
    }
}
