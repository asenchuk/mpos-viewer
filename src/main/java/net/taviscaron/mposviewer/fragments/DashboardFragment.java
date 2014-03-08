package net.taviscaron.mposviewer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.taviscaron.mposviewer.R;
import net.taviscaron.mposviewer.rpc.RPC;
import net.taviscaron.mposviewer.rpc.result.GetPoolStatusResult;
import net.taviscaron.mposviewer.rpc.result.GetUserBalanceResult;
import net.taviscaron.mposviewer.rpc.result.GetUserStatusResult;

import java.io.Serializable;
import java.util.Map;

/**
 * Dashboard fragment
 * @author Andrei Senchuk
 */
public class DashboardFragment extends RPCDataPresenterFragment {
    private static final String DASHBOARD_STATE_KEY = "dashboardState";

    public static class DashboardState implements Serializable {
        private float poolHashrate;
        private float netHashrate;
        private float yourHashrate;
        private float yourSharerate;
        private double confirmedBalance;
        private double unconfirmedBalance;
    }

    private TextView yourHashrateTextView;
    private TextView yourSharerateTextView;
    private TextView poolHashrateTextView;
    private TextView netHashrateTextView;
    private TextView confirmedBalanceTextView;
    private TextView unconfirmedBalanceTextView;
    private DashboardState state;

    public DashboardFragment() {
        super(RPC.Method.GET_USER_BALANCE, RPC.Method.GET_USER_STATUS, RPC.Method.GET_POOL_STATUS);
    }

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
            yourHashrateTextView.setText(String.format("%.0f", state.yourHashrate));
            yourSharerateTextView.setText(String.format("%.2f", state.yourSharerate));
            poolHashrateTextView.setText(String.format("%.2f", state.poolHashrate));
            netHashrateTextView.setText(String.format("%.2f", state.netHashrate));
            confirmedBalanceTextView.setText(String.format("%.8f", state.confirmedBalance));
            unconfirmedBalanceTextView.setText(String.format("%.8f", state.unconfirmedBalance));
        } else {
            yourHashrateTextView.setText("-");
            yourSharerateTextView.setText("-");
            poolHashrateTextView.setText("-");
            netHashrateTextView.setText("-");
            confirmedBalanceTextView.setText("-");
            unconfirmedBalanceTextView.setText("-");
        }
    }

    @Override
    protected boolean hasData() {
        return (state != null);
    }

    @Override
    protected void onLoadFinished(Map<RPC.Method, RPC.RPCResult> results) {
        state = new DashboardState();

        RPC.RPCResult result = results.get(RPC.Method.GET_USER_STATUS);
        if(result.error == null && result.result != null) {
            GetUserStatusResult userStatusResult = (GetUserStatusResult)result.result;
            state.yourHashrate = userStatusResult.getHashrate();
            state.yourSharerate = userStatusResult.getSharerate();
        }

        result = results.get(RPC.Method.GET_POOL_STATUS);
        if(result.error == null && result.result != null) {
            GetPoolStatusResult poolStatusResult = (GetPoolStatusResult)result.result;
            state.poolHashrate = poolStatusResult.getHashrate() / 1e6F;
            state.netHashrate = poolStatusResult.getNetHashRate() / 1e9F;
        }

        result = results.get(RPC.Method.GET_USER_BALANCE);
        if(result.error == null && result.result != null) {
            GetUserBalanceResult userBalanceResult = (GetUserBalanceResult)result.result;
            state.confirmedBalance = userBalanceResult.getConfirmed();
            state.unconfirmedBalance = userBalanceResult.getUnconfirmed();
        }

        updateViews();
    }
}
