package net.taviscaron.mposviewer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import net.taviscaron.mposviewer.R;
import net.taviscaron.mposviewer.core.Constants;
import net.taviscaron.mposviewer.fragments.*;
import net.taviscaron.mposviewer.model.Account;
import net.taviscaron.mposviewer.rpc.RPC;
import net.taviscaron.mposviewer.rpc.result.GetPoolStatusResult;
import net.taviscaron.mposviewer.rpc.result.GetUserStatusResult;
import net.taviscaron.mposviewer.storage.DBHelper;
import net.taviscaron.mposviewer.util.IOUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shows MPOS account information
 * @author Andrei Senchuk
 */
public class AccountViewActivity extends SherlockFragmentActivity implements RPCDataLoaderFragment.RPCDataLoaderFragmentListener {
    private static final String TAG = "AccountViewActivity";
    private static final String IS_ONCE_DATA_LOADED_KEY = "isObceDataLoaded";

    public static final String ACCOUNT_ID_KEY = "accountId";

    private enum Page {
        DASHBOARD(DashboardFragment.class, R.string.account_info_tab_dashboard),
        WORKERS(WorkersFragment.class, R.string.account_info_tab_workers),
        GENERAL_STATS(GeneralStatsFragment.class, R.string.account_info_tab_general_stats),
        LAST_BLOCKS(LastBlocksFragment.class, R.string.account_info_tab_last_blocks),
        USER_HASHRATES(UserHashratesFragment.class, R.string.account_info_tab_user_hashrates),
        USER_SHARERATES(UserShareratesFragment.class, R.string.account_info_tab_user_shares);

        final int titleId;
        final Class<? extends Fragment> clazz;
        final String tag;

        Page(Class<? extends Fragment> clazz, int titleId) {
            this.clazz = clazz;
            this.titleId = titleId;
            this.tag = UUID.randomUUID().toString();
        }
    }

    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getSupportActionBar().setSelectedNavigationItem(position);
        }
    };

    private final ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // noop
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // noop
        }
    };

    private final List<Fragment> fragments = new ArrayList<Fragment>(Page.values().length);
    private RPCDataLoaderFragment loaderFragment;
    private Account account;
    private ViewPager viewPager;
    private boolean isOnceDataLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_info);

        final FragmentManager fm = getSupportFragmentManager();

        for(Page page : Page.values()) {
            Fragment fragment = Fragment.instantiate(this, page.clazz.getName());
            fragments.add(fragment);
        }

        viewPager = (ViewPager)findViewById(R.id.account_info_pager);
        viewPager.setOnPageChangeListener(pageChangeListener);
        viewPager.setAdapter(new FragmentPagerAdapter(fm) {
            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return Page.values().length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getString(Page.values()[position].titleId);
            }
        });

        // create tabs
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        for(Page page : Page.values()) {
            actionBar.addTab(actionBar.newTab().setText(page.titleId).setTabListener(tabListener));
        }

        // data loader fragment
        loaderFragment = (RPCDataLoaderFragment)fm.findFragmentByTag(RPCDataLoaderFragment.DEFAULT_FRAGMENT_TAG);
        if(loaderFragment == null) {
            loaderFragment = new RPCDataLoaderFragment();
            fm.beginTransaction().add(loaderFragment, RPCDataLoaderFragment.DEFAULT_FRAGMENT_TAG).commit();
        }
   }

    protected void onStart() {
        super.onStart();

        // getting args
        int accountId = -1;

        Intent intent = getIntent();
        if(intent == null || (accountId = intent.getIntExtra(ACCOUNT_ID_KEY, -1)) == -1) {
            throw new RuntimeException(getClass().getCanonicalName() + " activity should be started with '" + ACCOUNT_ID_KEY + "' intent extra");
        }

        // load account
        DBHelper dbHelper = new DBHelper(this);
        account = dbHelper.findAccountById(accountId);
        dbHelper.close();

        if(account == null) {
            Log.w(TAG, "Account with id " + accountId + " is not found.");
            exitAccount();
        }

        loaderFragment.setUrl(account.getUrl());
        loaderFragment.setToken(account.getToken());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!isOnceDataLoaded) {
            loadData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.account_info, menu);
        menu.findItem(R.id.account_info_refresh).setEnabled(!loaderFragment.isLoading());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        switch (item.getItemId()) {
            case R.id.account_info_refresh:
                loadData();
                break;
            case R.id.account_info_exit:
                exitAccount();
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_ONCE_DATA_LOADED_KEY, isOnceDataLoaded);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isOnceDataLoaded = savedInstanceState.getBoolean(IS_ONCE_DATA_LOADED_KEY, isOnceDataLoaded);
    }

    private void loadData() {
        if(!loaderFragment.isLoading() && IOUtils.isNetworkAvailable(this, true)) {
            loaderFragment.load(RPC.Method.GET_USER_STATUS, RPC.Method.GET_USER_WORKERS, RPC.Method.GET_POOL_STATUS);
        }
    }

    private void exitAccount() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove(Constants.CURRENT_ACCOUNT_ID_PREF_KEY).commit();
        startActivity(new Intent(this, AccountsManagementActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        finish();
    }

    @Override
    public void onDataLoadStarted() {
        if(!isOnceDataLoaded) {
            ProgressDialogFragment pdf = new ProgressDialogFragment();
            pdf.show(getSupportFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
        } else {
            Toast.makeText(this, R.string.message_refreshing, Toast.LENGTH_SHORT).show();
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onDataLoadFinished(Map<RPC.Method, RPC.RPCResult> results) {
        ProgressDialogFragment pdf = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG);
        if(pdf != null) {
            pdf.dismiss();
        }

        isOnceDataLoaded = true;
        invalidateOptionsMenu();

        int failedResults = 0;
        for(RPC.RPCResult r : results.values()) {
            if(r.error != null || r.result == null) {
                failedResults++;
            }
        }

        if(failedResults < results.size()) {
            DashboardFragment.DashboardState dashboardState = new DashboardFragment.DashboardState();

            RPC.RPCResult result = results.get(RPC.Method.GET_USER_STATUS);
            if(result.error == null && result.result != null) {
                GetUserStatusResult userStatusResult = (GetUserStatusResult)result.result;
                dashboardState.setYourHashrate(userStatusResult.getHashrate());
                dashboardState.setYourSharerate(userStatusResult.getSharerate());

                GetUserStatusResult.Transactions transactions = userStatusResult.getTransactions();
                dashboardState.setConfirmedBalance(transactions.getCredit() - transactions.getDebit() - transactions.getManualDebit() - transactions.getDonation() - transactions.getTxFee());
                dashboardState.setUnconfirmedBalanceAvailable(false);
            }

            result = results.get(RPC.Method.GET_POOL_STATUS);
            if(result.error == null && result.result != null) {
                GetPoolStatusResult poolStatusResult = (GetPoolStatusResult)result.result;
                dashboardState.setPoolHashrate(poolStatusResult.getHashrate() / 1e6F);
                dashboardState.setNetHashrate(poolStatusResult.getNetHashRate() / 1e9F);
            }

            DashboardFragment dashboardFragment = (DashboardFragment)fragments.get(Page.DASHBOARD.ordinal());
            dashboardFragment.setState(dashboardState);

            if(failedResults > 0) {
                Toast.makeText(this, R.string.error_partially_failed_to_load_try_again, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.error_failed_to_load_try_again, Toast.LENGTH_SHORT).show();
        }
    }
}
