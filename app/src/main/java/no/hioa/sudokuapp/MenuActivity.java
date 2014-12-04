package no.hioa.sudokuapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Sondre on 25.11.2014.
 *
 * Contains menu-methods and methods for init other Activities
 */
public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("EXIT", false)) finish();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String language  = sharedPref.getString("language_preferance", "");
        if(language.equals("")) {
            language = (getResources().getConfiguration().locale).getLanguage();

            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putString("language_preferance", language);
            edit.commit();
        }

        setLanguage(language);

        setContentView(R.layout.activity_menu);
        setTitle(getResources().getString(R.string.app_name));
        //restartActivity();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void onNewGameClick(View view) {
        Intent intent = new Intent(this, SudokuActivity.class);
        startActivity(intent);
    }

    public void onCreateGameClick(View view) {
        Intent intent = new Intent(this, SudokuCreateActivity.class);
        startActivity(intent);
    }

    public void onRulesClick(View view) {
        Intent intent = new Intent(this, RulesActivity.class);
        startActivity(intent);
    }

    public void onSettingsClick(View view) {
        Intent in = new Intent(this, SetPreferencesActivity.class);
        startActivity(in);
    }

    public void onExitClick(View view) {
        finish();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
