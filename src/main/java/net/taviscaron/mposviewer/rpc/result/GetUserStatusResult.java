package net.taviscaron.mposviewer.rpc.result;

/**
 * getuserstatus RPC response
 * @author Andrei Senchuk
 */
public class GetUserStatusResult {
    public static class Shares {
        public int valid;
        public int invalid;

        public int getInvalid() {
            return invalid;
        }

        public void setInvalid(int invalid) {
            this.invalid = invalid;
        }

        public int getValid() {
            return valid;
        }

        public void setValid(int valid) {
            this.valid = valid;
        }
    }

    private String username;
    private Shares shares;
    private float hashrate;
    private float sharerate;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Shares getShares() {
        return shares;
    }

    public void setShares(Shares shares) {
        this.shares = shares;
    }

    public float getHashrate() {
        return hashrate;
    }

    public void setHashrate(float hashrate) {
        this.hashrate = hashrate;
    }

    public float getSharerate() {
        return sharerate;
    }

    public void setSharerate(float sharerate) {
        this.sharerate = sharerate;
    }
}
