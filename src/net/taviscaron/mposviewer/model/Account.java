package net.taviscaron.mposviewer.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import net.taviscaron.mposviewer.util.DBUtils;

import java.io.Serializable;

/**
 * Account entity class
 * @author Andrei Senchuk
 */
public class Account implements Serializable {
    public static final String TABLE_NAME = "Account";
    public static final String POOL_NAME_ATTR = "pool_name";
    public static final String USER_NAME_ATTR = "user_name";
    public static final String USER_ID_ATTR = "user_id";
    public static final String TOKEN_ATTR = "token";
    public static final String URL_ATTR = "url";
    public static final String COIN_ATTR = "coin";

    private int id;
    private String poolName;
    private String userName;
    private int userId;
    private String token;
    private String url;
    private String coin;

    public Account() {
        // default constructor
    }

    public Account(Cursor cursor) {
        id = DBUtils.getInt(cursor, BaseColumns._ID);
        poolName = DBUtils.getString(cursor, POOL_NAME_ATTR);
        userName = DBUtils.getString(cursor, USER_NAME_ATTR);
        userId = DBUtils.getInt(cursor, USER_ID_ATTR);
        token = DBUtils.getString(cursor, TOKEN_ATTR);
        url = DBUtils.getString(cursor, URL_ATTR);
        coin = DBUtils.getString(cursor, COIN_ATTR);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCoin() { return coin; }

    public void setCoin(String coin) { this.coin = coin; }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(POOL_NAME_ATTR, poolName);
        cv.put(USER_NAME_ATTR, userName);
        cv.put(USER_ID_ATTR, userId);
        cv.put(TOKEN_ATTR, token);
        cv.put(URL_ATTR, url);
        cv.put(COIN_ATTR, coin);
        return cv;
    }
}
