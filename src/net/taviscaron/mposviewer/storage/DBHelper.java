package net.taviscaron.mposviewer.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import net.taviscaron.mposviewer.BuildConfig;
import net.taviscaron.mposviewer.core.Constants;
import net.taviscaron.mposviewer.model.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Database helper
 * @author Andrei Senchuk
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DB_NAME = "storage";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();

        db.execSQL("CREATE TABLE " + Account.TABLE_NAME + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Account.POOL_NAME_ATTR +  " TEXT, " + Account.USER_NAME_ATTR + " TEXT, " + Account.USER_ID_ATTR + " INTEGER, " + Account.TOKEN_ATTR + " TEXT, " + Account.URL_ATTR + " TEXT)");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();

        db.execSQL("DROP TABLE " + Account.TABLE_NAME);

        db.setTransactionSuccessful();
        db.endTransaction();

        onCreate(db);
    }

    public synchronized Cursor findAllAccounts() {
        return getReadableDatabase().query(Account.TABLE_NAME, null, null, null, null, null, null);
    }

    public synchronized boolean isAccountExists(Account account) {
        return isAccountExists(account.getUrl(), account.getToken(), account.getUserId());
    }

    public synchronized boolean isAccountExists(String url, String token, int userId) {
        Cursor cursor = getReadableDatabase().query(Account.TABLE_NAME, null, String.format("%s = '%s' AND %s = %d", Account.TOKEN_ATTR, token, Account.USER_ID_ATTR, userId), null, null, null, null);
        boolean result = (cursor.getCount() > 0);
        cursor.close();
        return result;
    }

    public synchronized void addAccount(Account account) {
        getWritableDatabase().insert(Account.TABLE_NAME, null, account.toContentValues());
    }
}
