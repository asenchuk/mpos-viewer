package net.taviscaron.mposviewer.rpc.result;

/**
 * getuserbalance RPC call result
 * @author Andrei Senchuk
 */
public class GetUserBalanceResult {
    private float confirmed;
    private float unconfirmed;
    private float orphaned;

    public float getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(float confirmed) {
        this.confirmed = confirmed;
    }

    public float getUnconfirmed() {
        return unconfirmed;
    }

    public void setUnconfirmed(float unconfirmed) {
        this.unconfirmed = unconfirmed;
    }

    public float getOrphaned() {
        return orphaned;
    }

    public void setOrphaned(float orphaned) {
        this.orphaned = orphaned;
    }
}
