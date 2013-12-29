package net.taviscaron.mposviewer.rpc.model;

/**
 * Balance json obj
 * @author Andrei Senchuk
 */
public class Balance {
    private double confirmed;
    private double unconfirmed;
    private double orphaned;

    public double getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(double confirmed) {
        this.confirmed = confirmed;
    }

    public double getUnconfirmed() {
        return unconfirmed;
    }

    public void setUnconfirmed(double unconfirmed) {
        this.unconfirmed = unconfirmed;
    }

    public double getOrphaned() {
        return orphaned;
    }

    public void setOrphaned(double orphaned) {
        this.orphaned = orphaned;
    }
}
