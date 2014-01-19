package net.taviscaron.mposviewer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import net.taviscaron.mposviewer.R;
import net.taviscaron.mposviewer.rpc.RPC;
import net.taviscaron.mposviewer.model.Worker;

import java.util.Map;

/**
 * Workers list fragment
 * @author Andrei Senchuk
 */
public class WorkersFragment extends RPCDataPresenterFragment {
    private static final String WORKERS_BUNDLE_KEY = "workers";
    private Worker[] workers;
    private WorkersListAdapter adapter;

    private class WorkersListAdapter extends BaseAdapter {
        private Context context;

        public WorkersListAdapter(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        public int getCount() {
            return (workers != null) ? workers.length : 0;
        }

        @Override
        public Worker getItem(int i) {
            return workers[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.workers_list_item, viewGroup, false);
            }

            Worker worker = getItem(i);

            ((TextView)view.findViewById(R.id.worker_item_username)).setText(worker.getUsername());
            ((TextView)view.findViewById(R.id.worker_item_password)).setText(worker.getPassword());
            ((TextView)view.findViewById(R.id.worker_item_hashrate)).setText(Integer.toString(worker.getHashrate()));
            ((TextView)view.findViewById(R.id.worker_item_difficulty)).setText(String.format("%.0f", worker.getDifficulty()));

            return view;
        }
    }

    public WorkersFragment() {
        super(RPC.Method.GET_USER_WORKERS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            workers = (Worker[])savedInstanceState.getSerializable(WORKERS_BUNDLE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.workers_fragment, container, false);
        ListView list = (ListView)v.findViewById(R.id.workers_fragment_list);

        adapter = new WorkersListAdapter(getActivity());
        list.setAdapter(adapter);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(workers != null) {
            outState.putSerializable(WORKERS_BUNDLE_KEY, workers);
        }
    }

    @Override
    protected void onLoadFinished(Map<RPC.Method, RPC.RPCResult> results) {
        RPC.RPCResult result = results.get(RPC.Method.GET_USER_WORKERS);
        if(result != null && result.result != null) {
            workers = (Worker[])result.result;
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected boolean hasData() {
        return (workers != null);
    }
}
