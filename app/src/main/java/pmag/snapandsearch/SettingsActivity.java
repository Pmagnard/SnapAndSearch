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
    private CheckBoxPreference justvisualCheckBoxPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        // set and register the checkbox to this listener
        bingCheckBoxPref = (CheckBoxPreference) getPreferenceManager().findPreference("prefServiceBing");
        testCheckBoxPref = (CheckBoxPreference) getPreferenceManager().findPreference("prefServiceTest");
        justvisualCheckBoxPref = (CheckBoxPreference) getPreferenceManager().findPreference("prefServiceJustvisual");
        bingCheckBoxPref.setOnPreferenceClickListener(this);
        testCheckBoxPref.setOnPreferenceClickListener(this);
        justvisualCheckBoxPref.setOnPreferenceClickListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        // if a checkbox is checked then uncheck every other checkboxes
        if ((preference == testCheckBoxPref) && (testCheckBoxPref.isChecked())) {
            bingCheckBoxPref.setChecked(false);
            justvisualCheckBoxPref.setChecked(false);
        }
        else if ((preference == bingCheckBoxPref) && (bingCheckBoxPref.isChecked())) {
            testCheckBoxPref.setChecked(false);
            justvisualCheckBoxPref.setChecked(false);

        }
        else if ((preference == justvisualCheckBoxPref) && (justvisualCheckBoxPref.isChecked())) {
            testCheckBoxPref.setChecked(false);
            bingCheckBoxPref.setChecked(false);
        }
        // if not checkbox is checked then check test checkbox
        if ((!bingCheckBoxPref.isChecked()) && (!testCheckBoxPref.isChecked()) && (!justvisualCheckBoxPref.isChecked())) {
            testCheckBoxPref.setChecked(true);
        }
        return false;
    }
}
