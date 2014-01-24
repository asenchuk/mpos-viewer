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

        // id type changed from int to long so this code is for automated migration
        long id = -1;
        try {
            id = sp.getLong(Constants.CURRENT_ACCOUNT_ID_PREF_KEY, -1);
        } catch(ClassCastException e) {
            Object obj = sp.getAll().get(Constants.CURRENT_ACCOUNT_ID_PREF_KEY);
            if(obj instanceof Integer) {
                id = (Integer)obj;
                sp.edit().putLong(Constants.CURRENT_ACCOUNT_ID_PREF_KEY, id).commit();
            }
        }

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
