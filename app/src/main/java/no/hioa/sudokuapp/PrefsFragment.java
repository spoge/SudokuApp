package no.hioa.sudokuapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Sondre on 27.11.2014.
 *
 * Preference-fragment.
 */
public class PrefsFragment extends PreferenceFragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String lang = sharedPref.getString("language_preferance", "");
        if(lang.equals("")) {
            lang = (getResources().getConfiguration().locale).getLanguage();
        }

        ListPreference languagelist = (ListPreference) getPreferenceScreen().findPreference("language_preferance");

        languagelist.setValueIndex(getLanguageIndex(lang));
        languagelist.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setLanguage(newValue.toString());
                restartActivity();
                return true;
            }
        });
    }

    private int getLanguageIndex(String lang) {
        int index = 0;
        if(lang.equals("no")) index = 0;
        else if(lang.equals("en")) index = 1;
        return index;
    }

    private void setLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());
    }

    private void restartActivity() {
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        startActivity(intent);
    }
}
