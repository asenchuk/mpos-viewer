package net.taviscaron.mposviewer.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import net.taviscaron.mposviewer.R;

/**
 * Application preferences activity
 * @author Andrei Senchuk
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
