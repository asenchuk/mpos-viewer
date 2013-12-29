package net.taviscaron.mposviewer.rpc.model;

/**
 * Network info
 * @author Andrei Senchuk
 */
public class NetworkInfo {
    private double hashrate;
    private double difficulty;
    private int block;

    public double getHashrate() {
        return hashrate;
    }

    public void setHashrate(double hashrate) {
        this.hashrate = hashrate;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }
}
