package net.taviscaron.mposviewer.rpc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Worker json obj
 * @author Andrei Senchuk
 */
public class Worker {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("monitor")
    private int monitor;

    @SerializedName("hashrate")
    private int hashrate;

    @SerializedName("difficulty")
    private int difficulty;

    @SerializedName("count_all")
    private int countAll;

    @SerializedName("count_all_archive")
    private int countAllArchive;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMonitor() {
        return monitor;
    }

    public void setMonitor(int monitor) {
        this.monitor = monitor;
    }

    public int getHashrate() {
        return hashrate;
    }

    public void setHashrate(int hashrate) {
        this.hashrate = hashrate;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getCountAll() {
        return countAll;
    }

    public void setCountAll(int countAll) {
        this.countAll = countAll;
    }

    public int getCountAllArchive() {
        return countAllArchive;
    }

    public void setCountAllArchive(int countAllArchive) {
        this.countAllArchive = countAllArchive;
    }
}
