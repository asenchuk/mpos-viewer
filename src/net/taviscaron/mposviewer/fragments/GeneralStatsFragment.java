package net.taviscaron.mposviewer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.taviscaron.mposviewer.R;
import net.taviscaron.mposviewer.rpc.RPC;
import net.taviscaron.mposviewer.rpc.result.GetPoolStatusResult;

import java.io.Serializable;
import java.util.Map;

/**
 * General stats fragment
 * @author Andrei Senchuk
 */
public class GeneralStatsFragment extends RPCDataPresenterFragment {
    private static final String STATS_BUNDLE_KEY = "stats";

    private class Stats implements Serializable {
        private float poolHashRate;
        private float poolEfficiency;
        private int activeWorkers;
        private long nextBlock;
        private long lastBlock;
        private int estAvgTimePerRound;
        private int estShares;
        private int timeSinceLastBlock;
    }

    private TextView poolHashRateTextView;
    private TextView poolEfficiencyTextView;
    private TextView activeWorkersTextView;
    private TextView nextNetworkBlockTextView;
    private TextView lastBlockFoundTextView;
    private TextView estAvgTimePerRoundPoolTextView;
    private TextView estSharesThisRoundTextView;
    private TextView timeSinceLastBlockTextView;
    private Stats stats;

    public GeneralStatsFragment() {
        super(RPC.Method.GET_POOL_STATUS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            stats = (Stats)savedInstanceState.getSerializable(STATS_BUNDLE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.general_stats_fragment, container, false);
        poolHashRateTextView = (TextView)v.findViewById(R.id.gen_stats_pool_hashrate);
        poolEfficiencyTextView = (TextView)v.findViewById(R.id.gen_stats_pool_efficiency);
        activeWorkersTextView = (TextView)v.findViewById(R.id.gen_stats_active_workers);
        nextNetworkBlockTextView = (TextView)v.findViewById(R.id.gen_stats_next_block);
        lastBlockFoundTextView = (TextView)v.findViewById(R.id.gen_stats_last_block);
        estAvgTimePerRoundPoolTextView = (TextView)v.findViewById(R.id.gen_stats_est_avg_round_time_pool);
        estSharesThisRoundTextView = (TextView)v.findViewById(R.id.gen_stats_est_shares);
        timeSinceLastBlockTextView = (TextView)v.findViewById(R.id.gen_stats_time_since_last_block);
        updateView();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(stats != null) {
            outState.putSerializable(STATS_BUNDLE_KEY, stats);
        }
    }

    private void updateView() {
        if(stats != null) {
            poolHashRateTextView.setText(String.format("%.3f %s", stats.poolHashRate, getActivity().getString(R.string.measure_ghash_per_s)));
            poolEfficiencyTextView.setText(String.format("%.2f%%", stats.poolEfficiency));
            activeWorkersTextView.setText(Integer.toString(stats.activeWorkers));
            nextNetworkBlockTextView.setText(Long.toString(stats.nextBlock));
            lastBlockFoundTextView.setText(Long.toString(stats.lastBlock));
            estAvgTimePerRoundPoolTextView.setText(String.format(formatSeconds(stats.estAvgTimePerRound)));
            estSharesThisRoundTextView.setText(Integer.toString(stats.estShares));
            timeSinceLastBlockTextView.setText(String.format(formatSeconds(stats.timeSinceLastBlock)));
        } else {
            poolHashRateTextView.setText("-");
            poolEfficiencyTextView.setText("-");
            activeWorkersTextView.setText("-");
            nextNetworkBlockTextView.setText("-");
            lastBlockFoundTextView.setText("-");
            estAvgTimePerRoundPoolTextView.setText("-");
            estSharesThisRoundTextView.setText("-");
            timeSinceLastBlockTextView.setText("-");
        }
    }

    @Override
    protected void onLoadFinished(Map<RPC.Method, RPC.RPCResult> results) {
        RPC.RPCResult result = results.get(RPC.Method.GET_POOL_STATUS);
        if(result != null && result.result != null) {
            GetPoolStatusResult statusResult = (GetPoolStatusResult)result.result;

            stats = new Stats();
            stats.poolHashRate = statusResult.getHashrate() / 1e6f;
            stats.poolEfficiency = statusResult.getEfficiency();
            stats.activeWorkers = statusResult.getWorkers();
            stats.nextBlock = statusResult.getNextnetworkblock();
            stats.lastBlock = statusResult.getLastblock();
            stats.estAvgTimePerRound = (int)statusResult.getEstimatedTime();
            stats.estShares = (int)statusResult.getEstimatedShares();
            stats.timeSinceLastBlock = statusResult.getTimeSinceLastBlock();

            updateView();
        }
    }

    @Override
    protected boolean hasData() {
        return (stats != null);
    }

    private String formatSeconds(int seconds) {
        int min = seconds / 60;
        if(min > 0) {
            int sec = seconds % 60;
            return getActivity().getString(R.string.format_time_mins_secs, min, sec);
        } else {
            return getActivity().getString(R.string.format_time_secs, seconds);
        }
    }
}
