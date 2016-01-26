package pmag.snapandsearch;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import pmag.snapandsearch.search.R;

/**
 * Created by FR067458 on 26/01/2016.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {
    private CheckBoxPreference bingCheckBoxPref = null;
    private CheckBoxPreference testCheckBoxPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        // set and register the checkbox to this listener
        bingCheckBoxPref = (CheckBoxPreference) getPreferenceManager().findPreference("prefServiceBing");
        testCheckBoxPref = (CheckBoxPreference) getPreferenceManager().findPreference("prefServiceTest");
        bingCheckBoxPref.setOnPreferenceClickListener(this);
        testCheckBoxPref.setOnPreferenceClickListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        // if a checkbox is checked then uncheck every other checkboxes
        if ((preference == testCheckBoxPref) && (testCheckBoxPref.isChecked())) {
            bingCheckBoxPref.setChecked(false);
        }
        else if ((preference == bingCheckBoxPref) && (bingCheckBoxPref.isChecked())) {
            testCheckBoxPref.setChecked(false);
        }
        // if not checkbox is checked then check test checkbox
        if ((!bingCheckBoxPref.isChecked()) && (!testCheckBoxPref.isChecked())) {
            testCheckBoxPref.setChecked(true);
        }
        return false;
    }
}
