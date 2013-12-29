package net.taviscaron.mposviewer.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import net.taviscaron.mposviewer.model.Account;
import net.taviscaron.mposviewer.rpc.result.GetPublicResult;
import net.taviscaron.mposviewer.rpc.result.GetUserStatusResult;
import net.taviscaron.mposviewer.rpc.RPC;
import net.taviscaron.mposviewer.storage.DBHelper;

/**
 * Account adding behavior fragment
 * @author Andrei Senchuk
 */
public class AccountAddFragment extends Fragment {
    public enum Result {
        ALREADY_EXISTS,
        SUCCESSFULLY_ADDED,
        ADDING_FAILED
    }

    public interface AccountAddFragmentListener {
        public void onAccountAddingStarted();
        public void onAccountAddingFinished(Result result);
    }

    private AccountAddFragmentListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (AccountAddFragmentListener)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void addAccount(String url, String token, int userId) {
        Account account = new Account();
        account.setUrl(url);
        account.setToken(token);
        account.setUserId(userId);
        new AccountInitTask().execute(account);
    }

    private class AccountInitTask extends AsyncTask<Account, Void, Result> {
        @Override
        protected void onPreExecute() {
            listener.onAccountAddingStarted();
        }

        @Override
        protected Result doInBackground(Account... params) {
            Account account = params[0];

            DBHelper db = new DBHelper(getActivity());
            try {
                if(db.isAccountExists(account)) {
                    return Result.ALREADY_EXISTS;
                }

                // init RPC; token and url is required
                RPC rpc = new RPC();
                rpc.setUrl(account.getUrl());
                rpc.setToken(account.getToken());

                // stage #1 - pool name getting
                RPC.RPCResult rpcResult = rpc.call(RPC.Method.GET_PUBLIC);
                if(rpcResult.error != null) {
                    // concrete error ?
                    return Result.ADDING_FAILED;
                }

                GetPublicResult publicResult = (GetPublicResult)rpcResult.result;
                account.setPoolName(publicResult.getPoolName());

                // stage #2 - getting username
                rpcResult = rpc.call(RPC.Method.GET_USER_STATUS);
                if(rpcResult.error != null) {
                    // concrete error ?
                    return Result.ADDING_FAILED;
                }

                GetUserStatusResult userStatusResult = (GetUserStatusResult)rpcResult.result;
                account.setUserName(userStatusResult.getUsername());

                db.addAccount(account);

                return Result.SUCCESSFULLY_ADDED;
            } finally {
                db.close();
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            listener.onAccountAddingFinished(result);
        }
    }
}
