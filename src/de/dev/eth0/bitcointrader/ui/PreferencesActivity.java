//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.TextUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
/**
 * @author Alexander Muthmann
 */
public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
    sp.registerOnSharedPreferenceChangeListener(this);

    ListPreference listPreference = (ListPreference)findPreference(Constants.PREFS_KEY_GENERAL_UPDATE);
    if (listPreference.getValue() == null) {
      listPreference.setValueIndex(0);
    }
    listPreference.setSummary(listPreference.getEntry());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    // Update summary for api key changed
    if (TextUtils.equals(key, Constants.PREFS_KEY_GENERAL_UPDATE)) {
      Preference pref = findPreference(key);
      ListPreference lp = (ListPreference)pref;
      pref.setSummary(lp.getEntry());
    }
  }

}
