package net.taviscaron.mposviewer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import net.taviscaron.mposviewer.R;
import net.taviscaron.mposviewer.core.Constants;
import net.taviscaron.mposviewer.fragments.*;
import net.taviscaron.mposviewer.model.Account;
import net.taviscaron.mposviewer.storage.DBHelper;

/**
 * Shows MPOS account information
 * @author Andrei Senchuk
 */
public class AccountViewActivity extends SherlockFragmentActivity implements RPCDataPresenterFragment.RPCDataAccountProvider, RPCDataPresenterFragment.RPCDataSynchronousLoadCallback, RPCDataPresenterFragment.RPCDataLoadCallback {
    private static final String TAG = "AccountViewActivity";
    private static final String SYNC_LOADERS_BUNDLE_KEY = "syncLoaders";

    public static final String ACCOUNT_ID_KEY = "accountId";

    private enum Page {
        DASHBOARD(DashboardFragment.class, R.string.account_info_tab_dashboard),
        WORKERS(WorkersFragment.class, R.string.account_info_tab_workers),
        GENERAL_STATS(GeneralStatsFragment.class, R.string.account_info_tab_general_stats);

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
            invalidateOptionsMenu();
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

    private final SparseArray<Fragment> fragments = new SparseArray<Fragment>(Page.values().length);
    private ViewPager viewPager;
    private Account account;
    private int syncLoaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_info);

        final FragmentManager fm = getSupportFragmentManager();

        viewPager = (ViewPager)findViewById(R.id.account_info_pager);
        viewPager.setOnPageChangeListener(pageChangeListener);
        viewPager.setOffscreenPageLimit(Page.values().length);
        viewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int i) {
                return Fragment.instantiate(getApplicationContext(), Page.values()[i].clazz.getName());
            }

            @Override
            public Fragment instantiateItem(ViewGroup container, int position) {
                Fragment fragment = (Fragment)super.instantiateItem(container, position);
                fragments.put(position, fragment);
                return fragment;
            }

            @Override
            public int getCount() {
                return Page.values().length;
            }
        });

        // create tabs
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        for(Page page : Page.values()) {
            actionBar.addTab(actionBar.newTab().setText(page.titleId).setTabListener(tabListener));
        }

        if(savedInstanceState != null) {
            syncLoaders = savedInstanceState.getInt(SYNC_LOADERS_BUNDLE_KEY);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.account_info, menu);

        Fragment fragment = fragments.get(viewPager.getCurrentItem());
        if(fragment instanceof RPCDataPresenterFragment) {
            RPCDataPresenterFragment dataPresenterFragment = (RPCDataPresenterFragment)fragment;
            menu.findItem(R.id.account_info_refresh).setEnabled(!dataPresenterFragment.isLoading());
        } else {
            menu.findItem(R.id.account_info_refresh).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        switch (item.getItemId()) {
            case R.id.account_info_refresh:
                Fragment fragment = fragments.get(viewPager.getCurrentItem());
                if(fragment instanceof RPCDataPresenterFragment) {
                    ((RPCDataPresenterFragment)fragment).refreshData();
                    Toast.makeText(this, R.string.message_refreshing, Toast.LENGTH_LONG).show();
                }
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
        outState.putInt(SYNC_LOADERS_BUNDLE_KEY, syncLoaders);
    }

    private void exitAccount() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove(Constants.CURRENT_ACCOUNT_ID_PREF_KEY).commit();
        startActivity(new Intent(this, AccountsManagementActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        finish();
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public void onSynchronousLoadStarted() {
        if(syncLoaders++ == 0) {
            ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
            progressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
        }
    }

    @Override
    public void onSynchronousLoadFinished() {
        if(--syncLoaders == 0) {
            ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG);
            if(progressDialogFragment != null) {
                progressDialogFragment.dismiss();
            }
        }
    }

    @Override
    public void onLoadStarted() {
        invalidateOptionsMenu();
    }

    @Override
    public void onLoadFinished() {
        invalidateOptionsMenu();
    }
}
