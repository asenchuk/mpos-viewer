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
import net.taviscaron.mposviewer.rpc.result.GetDashboardDataResult;
import net.taviscaron.mposviewer.rpc.RPC;
import net.taviscaron.mposviewer.storage.DBHelper;
import net.taviscaron.mposviewer.util.IOUtils;

/**
 * Shows MPOS account information
 * @author Andrei Senchuk
 */
public class AccountViewActivity extends SherlockFragmentActivity implements RPCDataLoaderFragment.RPCDataLoaderFragmentListener {
    private static final String TAG = "AccountViewActivity";
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

        Page(Class<? extends Fragment> clazz, int titleId) {
            this.clazz = clazz;
            this.titleId = titleId;
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

    private RPCDataLoaderFragment loaderFragment;
    private Account account;
    private ViewPager viewPager;
    private GetDashboardDataResult dashboardData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_info);

        viewPager = (ViewPager)findViewById(R.id.account_info_pager);
        viewPager.setOnPageChangeListener(pageChangeListener);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return Fragment.instantiate(getApplicationContext(), Page.values()[i].clazz.getName());
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
        loaderFragment = (RPCDataLoaderFragment)getSupportFragmentManager().findFragmentByTag(RPCDataLoaderFragment.DEFAULT_FRAGMENT_TAG);
        if(loaderFragment == null) {
            loaderFragment = new RPCDataLoaderFragment();
            getSupportFragmentManager().beginTransaction().add(loaderFragment, RPCDataLoaderFragment.DEFAULT_FRAGMENT_TAG).commit();
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

        loadData();
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

    private void loadData() {
        if(!loaderFragment.isLoading() && IOUtils.isNetworkAvailable(this, true)) {
            loaderFragment.load(RPC.Method.GET_DASHBOARD_DATA);
        }
    }

    private void exitAccount() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove(Constants.CURRENT_ACCOUNT_ID_PREF_KEY).commit();
        startActivity(new Intent(this, AccountsManagementActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        finish();
    }

    private void updateViews() {

    }

    @Override
    public void onDataLoadStarted() {
        if(dashboardData == null) {
            ProgressDialogFragment pdf = new ProgressDialogFragment();
            pdf.show(getSupportFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onDataLoadFinished(Object result, RPC.Error error) {
        ProgressDialogFragment pdf = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG);
        if(pdf != null) {
            pdf.dismiss();
        }

        invalidateOptionsMenu();

        if(error != null || result == null) {
            Toast.makeText(this, R.string.error_failed_to_load_try_again, Toast.LENGTH_SHORT).show();
        } else {
            try {
                dashboardData = (GetDashboardDataResult)result;
                updateViews();
            } catch(ClassCastException e) {
                Log.w(TAG, "Can't cast dashboarddata result", e);
                Toast.makeText(this, R.string.error_failed_to_load_try_again, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
