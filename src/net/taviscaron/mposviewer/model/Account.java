package net.taviscaron.mposviewer.model;

import android.content.ContentValues;

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

    private int id;
    private String poolName;
    private String userName;
    private int userId;
    private String token;
    private String url;

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

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(POOL_NAME_ATTR, poolName);
        cv.put(USER_NAME_ATTR, userName);
        cv.put(USER_ID_ATTR, userId);
        cv.put(TOKEN_ATTR, token);
        cv.put(URL_ATTR, url);
        return cv;
    }
}
