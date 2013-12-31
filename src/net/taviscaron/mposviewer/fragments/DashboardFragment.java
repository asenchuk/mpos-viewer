package net.taviscaron.mposviewer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.taviscaron.mposviewer.R;

import java.io.Serializable;

/**
 * Dashboard fragment
 * @author Andrei Senchuk
 */
public class DashboardFragment extends Fragment {
    private static final String DASHBOARD_STATE_KEY = "dashboardState";

    public static class DashboardState implements Serializable {
        private float poolHashrate;
        private float netHashrate;
        private float yourHashrate;
        private float yourSharerate;
        private double confirmedBalance;
        private double unconfirmedBalance;
        private boolean isUnconfirmedBalanceAvailable;

        public float getPoolHashrate() {
            return poolHashrate;
        }

        public void setPoolHashrate(float poolHashrate) {
            this.poolHashrate = poolHashrate;
        }

        public float getNetHashrate() {
            return netHashrate;
        }

        public void setNetHashrate(float netHashrate) {
            this.netHashrate = netHashrate;
        }

        public float getYourHashrate() {
            return yourHashrate;
        }

        public void setYourHashrate(float yourHashrate) {
            this.yourHashrate = yourHashrate;
        }

        public float getYourSharerate() {
            return yourSharerate;
        }

        public void setYourSharerate(float yourSharerate) {
            this.yourSharerate = yourSharerate;
        }

        public double getConfirmedBalance() {
            return confirmedBalance;
        }

        public void setConfirmedBalance(double confirmedBalance) {
            this.confirmedBalance = confirmedBalance;
        }

        public double getUnconfirmedBalance() {
            return unconfirmedBalance;
        }

        public void setUnconfirmedBalance(double unconfirmedBalance) {
            this.unconfirmedBalance = unconfirmedBalance;
        }

        public boolean isUnconfirmedBalanceAvailable() {
            return isUnconfirmedBalanceAvailable;
        }

        public void setUnconfirmedBalanceAvailable(boolean isUnconfirmedBalanceAvailable) {
            this.isUnconfirmedBalanceAvailable = isUnconfirmedBalanceAvailable;
        }
    }

    private TextView yourHashrateTextView;
    private TextView yourSharerateTextView;
    private TextView poolHashrateTextView;
    private TextView netHashrateTextView;
    private TextView confirmedBalanceTextView;
    private TextView unconfirmedBalanceTextView;
    private DashboardState state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            state = (DashboardState)savedInstanceState.getSerializable(DASHBOARD_STATE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dashboard_fragment, container, false);
        yourHashrateTextView = (TextView)v.findViewById(R.id.dashboard_your_hashrate_value);
        yourSharerateTextView = (TextView)v.findViewById(R.id.dashboard_your_sharerate_value);
        poolHashrateTextView = (TextView)v.findViewById(R.id.dashboard_pool_hashrate_value);
        netHashrateTextView = (TextView)v.findViewById(R.id.dashboard_net_hashrate_value);
        confirmedBalanceTextView = (TextView)v.findViewById(R.id.dashboard_confirmed_balance);
        unconfirmedBalanceTextView = (TextView)v.findViewById(R.id.dashboard_unconfirmed_balance);
        updateViews();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DASHBOARD_STATE_KEY, state);
    }

    private void updateViews() {
        if(state != null) {
            yourHashrateTextView.setText(String.format("%.0f", state.getYourHashrate()));
            yourSharerateTextView.setText(String.format("%.2f", state.getYourSharerate()));
            poolHashrateTextView.setText(String.format("%.2f", state.getPoolHashrate()));
            netHashrateTextView.setText(String.format("%.2f", state.getNetHashrate()));
            confirmedBalanceTextView.setText(Double.toString(state.getConfirmedBalance()));
            unconfirmedBalanceTextView.setText((state.isUnconfirmedBalanceAvailable()) ? Double.toString(state.getUnconfirmedBalance()) : "-");
        } else {
            yourHashrateTextView.setText("-");
            yourSharerateTextView.setText("-");
            poolHashrateTextView.setText("-");
            netHashrateTextView.setText("-");
            confirmedBalanceTextView.setText("-");
            unconfirmedBalanceTextView.setText("-");
        }
    }

    public DashboardState getState() {
        return state;
    }

    public void setState(DashboardState state) {
        this.state = state;
        updateViews();
    }
}
