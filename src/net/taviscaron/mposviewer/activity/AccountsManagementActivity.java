package net.taviscaron.mposviewer.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import net.taviscaron.mposviewer.R;
import net.taviscaron.mposviewer.fragments.AccountAddFragment;
import net.taviscaron.mposviewer.fragments.ProgressDialogFragment;
import net.taviscaron.mposviewer.model.Account;
import net.taviscaron.mposviewer.storage.DBHelper;
import net.taviscaron.mposviewer.util.IOUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Accounts / pool management activity
 * @author Andrei Senchuk
 */
public class AccountsManagementActivity extends SherlockFragmentActivity implements AccountAddFragment.AccountAddFragmentListener {
    private static final String TAG = "AccountsManagementActivity";
    private static final String ACCOUNT_ADD_FRAGMENT_TAG = "accountAddFragment";
    private static final String PROGRESS_DIALOG_FRAGMENT_TAG = "progressDialogFragment";

    private SimpleCursorAdapter listAdapter;
    private DBHelper dbHelper;
    private AccountAddFragment fragment;
    private String pendingAccountStringCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts_management);

        // map user name to main title and pool name to subtitle of the simple_list_item_2
        listAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, new String[] {
                Account.USER_NAME_ATTR,
                Account.POOL_NAME_ATTR
        }, new int[] {
                android.R.id.text1,
                android.R.id.text2
        }, 0);

        ((ListView)findViewById(R.id.accounts_management_list)).setAdapter(listAdapter);

        // get the fragment (it's not null in case retained instance exists)
        FragmentManager fm = getSupportFragmentManager();
        fragment = (AccountAddFragment)fm.findFragmentByTag(ACCOUNT_ADD_FRAGMENT_TAG);

        if (fragment == null) {
            fragment = new AccountAddFragment();
            fm.beginTransaction().add(fragment, ACCOUNT_ADD_FRAGMENT_TAG).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbHelper = new DBHelper(this);
        refreshAccounts();
    }

    @Override
    protected void onStop() {
        super.onStop();

        listAdapter.changeCursor(null);
        dbHelper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(pendingAccountStringCode != null) {
            addAccountFromStringCode(pendingAccountStringCode);
            pendingAccountStringCode = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.accounts_management, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        switch (item.getItemId()) {
            case R.id.accounts_management_add:
                // use com.google.zxing.android-integration
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if(result != null) {
                    pendingAccountStringCode = result.getContents();
                } else {
                    Log.w(TAG, "Don't get result from zxing barcode reader");
                }
                break;
            default:
                break;
        }
    }

    public void addAccountFromStringCode(String code) {
        Matcher matcher = Pattern.compile("^\\|(.+)\\|([a-f0-9]+)\\|(\\d+)\\|$").matcher(code);
        if(matcher.find()) {
            String url = matcher.group(1);
            String token = matcher.group(2);
            int userId = Integer.parseInt(matcher.group(3));

            if(IOUtils.isNetworkAvailable(this, true)) {
                fragment.addAccount(url, token, userId);
            }
        } else {
            Toast.makeText(this, R.string.accounts_management_unknown_qr_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshAccounts() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                return dbHelper.findAllAccounts();
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                listAdapter.changeCursor(cursor);
            }
        }.execute();
    }

    @Override
    public void onAccountAddingStarted() {
        ProgressDialogFragment pdf = new ProgressDialogFragment();
        pdf.show(getSupportFragmentManager(), PROGRESS_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void onAccountAddingFinished(AccountAddFragment.Result result) {
        ProgressDialogFragment pdf = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_FRAGMENT_TAG);
        if(pdf != null) {
            pdf.dismiss();
        } else {
            Log.w(TAG, "WTF? Progress dialog should be showed.");
        }

        refreshAccounts();

        switch(result) {
            case ALREADY_EXISTS:
                Toast.makeText(this, R.string.accounts_management_account_already_added, Toast.LENGTH_SHORT).show();
                break;
            case SUCCESSFULLY_ADDED:
                Toast.makeText(this, R.string.accounts_management_account_added, Toast.LENGTH_SHORT).show();
                break;
            case ADDING_FAILED:
                Toast.makeText(this, R.string.accounts_management_account_add_failed, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
