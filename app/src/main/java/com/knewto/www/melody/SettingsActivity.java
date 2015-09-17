package com.knewto.www.melody;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Locale;

/**
 * Created by willwallis on 9/12/15.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // Add Country values to country settings
        ListPreference listPreferenceCategory = (ListPreference) findPreference(getString(R.string.pref_country_key));
        if (listPreferenceCategory != null) {
            String[] countryValues = Locale.getISOCountries();
            String[] countryEntries = new String[countryValues.length];

            int defaultValue = 0;
            String currentLocale = this.getResources().getConfiguration().locale.getCountry();
            Log.v("Current Locale", currentLocale);

            for (int i = 0; i < countryValues.length; i++){
                Locale locale = new Locale("", countryValues[i]);
                String countryName = locale.getDisplayCountry();
                countryEntries[i] = countryName;
                if (countryValues[i].equals(currentLocale)){
                    defaultValue = i;
                }
            }

            listPreferenceCategory.setEntries(countryEntries);
            listPreferenceCategory.setEntryValues(countryValues);
            if (listPreferenceCategory.getValue().length() > 2) // Value returns default if not set.
                listPreferenceCategory.setValueIndex(defaultValue);
        }


        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_country_key)));
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}