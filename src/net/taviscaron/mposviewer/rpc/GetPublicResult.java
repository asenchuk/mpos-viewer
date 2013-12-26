package net.taviscaron.mposviewer.rpc;

import com.google.gson.annotations.SerializedName;

/**
 * public RPC call result
 * @author Andrei Senchuk
 */
public class GetPublicResult {
    @SerializedName("pool_name")
    private String poolName;

    @SerializedName("hashrate")
    private long hashrate;

    @SerializedName("network_hashrate")
    private long networkHashrate;

    @SerializedName("workers")
    private int workers;

    @SerializedName("shares_this_round")
    private int sharesInThisRound;

    @SerializedName("last_block")
    private int lastBlock;

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public long getHashrate() {
        return hashrate;
    }

    public void setHashrate(long hashrate) {
        this.hashrate = hashrate;
    }

    public long getNetworkHashrate() {
        return networkHashrate;
    }

    public void setNetworkHashrate(long networkHashrate) {
        this.networkHashrate = networkHashrate;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public int getSharesInThisRound() {
        return sharesInThisRound;
    }

    public void setSharesInThisRound(int sharesInThisRound) {
        this.sharesInThisRound = sharesInThisRound;
    }
}
