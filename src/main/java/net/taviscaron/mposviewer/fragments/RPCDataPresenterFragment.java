package net.taviscaron.mposviewer.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;
import net.taviscaron.mposviewer.R;
import net.taviscaron.mposviewer.model.Account;
import net.taviscaron.mposviewer.rpc.RPC;
import net.taviscaron.mposviewer.rpc.RPCFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RPC Data presenter fragment
 * @author Andrei Senchuk
 */
public abstract class RPCDataPresenterFragment extends Fragment {
    private static final String TAG = "RPCDataPresenterFragment";

    public interface RPCDataAccountProvider {
        public Account getAccount();
    }

    public interface RPCDataSynchronousLoadCallback {
        public void onSynchronousLoadStarted();
        public void onSynchronousLoadFinished();
    }

    public interface RPCDataLoadCallback {
        public void onLoadStarted();
        public void onLoadFinished();
    }

    protected final RPC.Method[] methods;
    protected final AtomicBoolean isLoading = new AtomicBoolean();
    private RPCDataSynchronousLoadCallback synchronousLoadCallback;
    private RPCDataLoadCallback loadCallback;
    private boolean isSynchronousLoading;

    public RPCDataPresenterFragment(RPC.Method... methods) {
        this.methods = methods;

        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!hasData()) {
            refreshData();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof RPCDataSynchronousLoadCallback) {
            synchronousLoadCallback = (RPCDataSynchronousLoadCallback)activity;
        }

        if(activity instanceof RPCDataLoadCallback) {
            loadCallback = (RPCDataLoadCallback)activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        synchronousLoadCallback = null;
        loadCallback = null;
    }

    protected void onLoadStarted() {

    }

    protected void onLoadFinished(Map<RPC.Method, RPC.RPCResult> results) {

    }

    protected boolean hasData() {
        return true;
    }

    protected boolean isSyncLoaderRequired() {
        return !hasData();
    }

    private void onLoadStartedInternal() {
        onLoadStarted();

        if(synchronousLoadCallback != null && isSyncLoaderRequired()) {
            isSynchronousLoading = true;
            synchronousLoadCallback.onSynchronousLoadStarted();
        } else {
            isSynchronousLoading = false;
        }

        if(loadCallback != null) {
            loadCallback.onLoadStarted();
        }
    }

    private void onLoadFinishedInternal(Map<RPC.Method, RPC.RPCResult> results) {
        Activity activity = getActivity();
        if(activity != null) {
            int failedResults = 0;
            for(RPC.RPCResult r : results.values()) {
                if(r.error != null || r.result == null) {
                    failedResults++;
                }
            }

            if(failedResults < results.size()) {
                if(failedResults > 0) {
                    Toast.makeText(activity, R.string.error_partially_failed_to_load_try_again, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, R.string.error_failed_to_load_try_again, Toast.LENGTH_SHORT).show();
            }
        }

        if(loadCallback != null) {
            loadCallback.onLoadFinished();
        }

        if(synchronousLoadCallback != null && isSynchronousLoading) {
            synchronousLoadCallback.onSynchronousLoadFinished();
        }

        isSynchronousLoading = false;

        onLoadFinished(results);
    }

    public void refreshData() {
        Account account = null;

        Activity activity = getActivity();
        if(activity instanceof RPCDataAccountProvider) {
            account = ((RPCDataAccountProvider)activity).getAccount();
        }

        if(account == null) {
            Log.w(TAG, "account is not specified");
            return;
        }

        if(!isLoading.compareAndSet(false, true)) {
            Log.w(TAG, "loader is loading now");
            return;
        }

        final Account finalAccount = account;

        new AsyncTask<RPC.Method, Void, Map<RPC.Method, RPC.RPCResult>>() {
            @Override
            protected void onPreExecute() {
                onLoadStartedInternal();
            }

            @Override
            protected Map<RPC.Method, RPC.RPCResult> doInBackground(RPC.Method... methods) {
                RPC rpc = RPCFactory.createRPC(getActivity());
                rpc.setUrl(finalAccount.getUrl());
                rpc.setToken(finalAccount.getToken());

                Map<RPC.Method, RPC.RPCResult> results = new HashMap<RPC.Method, RPC.RPCResult>();

                for(RPC.Method m : methods) {
                    results.put(m, rpc.call(m));
                }

                return results;
            }

            @Override
            protected void onPostExecute(Map<RPC.Method, RPC.RPCResult> results) {
                isLoading.set(false);
                onLoadFinishedInternal(results);
            }
        }.execute(methods);
    }

    public boolean isLoading() {
        return isLoading.get();
    }
}
