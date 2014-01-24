package net.taviscaron.mposviewer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import net.taviscaron.mposviewer.core.Constants;

/**
 * Main dispatching activity
 * @author Andrei Senchuk
 */
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long id = sp.getLong(Constants.CURRENT_ACCOUNT_ID_PREF_KEY, -1);

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if(id != -1) {
            intent.setClass(this, AccountViewActivity.class);
            intent.putExtra(AccountViewActivity.ACCOUNT_ID_KEY, id);
        } else {
            intent.setClass(this, AccountsManagementActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
