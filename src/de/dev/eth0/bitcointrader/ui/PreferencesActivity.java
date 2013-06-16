package de.dev.eth0.bitcointrader.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.R;

public final class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    final ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
    sp.registerOnSharedPreferenceChangeListener(this);

    //@TODO remove mtgox settings if not activated

    // if api key has not been set yet, add the summary, otherwise the current value
    String mtgoxApiKey = sp.getString(Constants.PREFS_KEY_MTGOX_ACTIVATIONKEY, null);
    if (TextUtils.isEmpty(mtgoxApiKey)) {
      findPreference(Constants.PREFS_KEY_MTGOX_ACTIVATIONKEY).setSummary(R.string.preferences_mtgox_activation_key_summary);
    }
    else {
      findPreference(Constants.PREFS_KEY_MTGOX_ACTIVATIONKEY).setSummary(mtgoxApiKey);
    }

    final ListPreference listPreference = (ListPreference)findPreference(Constants.PREFS_KEY_GENERAL_UPDATE);
    if (listPreference.getValue() == null) {
      listPreference.setValueIndex(0);
    }
    listPreference.setSummary(listPreference.getEntry());
    listPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        listPreference.setValue(newValue.toString());
        // Get the entry which corresponds to the current value and set as summary
        preference.setSummary(listPreference.getEntry());
        return false;
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    // Update summary for api key changed
    if (TextUtils.equals(key, Constants.PREFS_KEY_MTGOX_ACTIVATIONKEY)) {
      Preference pref = findPreference(key);
      EditTextPreference etp = (EditTextPreference)pref;
      if (TextUtils.isEmpty(etp.getText())) {
        pref.setSummary(R.string.preferences_mtgox_activation_key_summary);
      }
      else {
        pref.setSummary(etp.getText());
      }
    }
  }
}
