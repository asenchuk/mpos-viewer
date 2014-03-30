package net.taviscaron.mposviewer.rpc.result;

import com.google.gson.annotations.SerializedName;

/**
 * getpoolstatus RPC response wrapper
 * @author Andrei Senchuk
 */
public class GetPoolStatusResult {
    @SerializedName("pool_name")
    private String poolName;

    private float hashrate;
    private float efficiency;
    private int workers;
    private int currentnetworkblock;
    private int nextnetworkblock;
    private int lastblock;

    @SerializedName("networkdiff")
    private double difficulty;

    @SerializedName("esttime")
    private double estimatedTime;

    @SerializedName("estshares")
    private double estimatedShares;

    @SerializedName("timesincelast")
    private int timeSinceLastBlock;

    @SerializedName("nethashrate")
    private long netHashRate;

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public float getHashrate() {
        return hashrate;
    }

    public void setHashrate(float hashrate) {
        this.hashrate = hashrate;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(float efficiency) {
        this.efficiency = efficiency;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public int getCurrentnetworkblock() {
        return currentnetworkblock;
    }

    public void setCurrentnetworkblock(int currentnetworkblock) {
        this.currentnetworkblock = currentnetworkblock;
    }

    public int getNextnetworkblock() {
        return nextnetworkblock;
    }

    public void setNextnetworkblock(int nextnetworkblock) {
        this.nextnetworkblock = nextnetworkblock;
    }

    public int getLastblock() {
        return lastblock;
    }

    public void setLastblock(int lastblock) {
        this.lastblock = lastblock;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public double getEstimatedShares() {
        return estimatedShares;
    }

    public void setEstimatedShares(double estimatedShares) {
        this.estimatedShares = estimatedShares;
    }

    public int getTimeSinceLastBlock() {
        return timeSinceLastBlock;
    }

    public void setTimeSinceLastBlock(int timeSinceLastBlock) {
        this.timeSinceLastBlock = timeSinceLastBlock;
    }

    public long getNetHashRate() {
        return netHashRate;
    }

    public void setNetHashRate(long netHashRate) {
        this.netHashRate = netHashRate;
    }
}
