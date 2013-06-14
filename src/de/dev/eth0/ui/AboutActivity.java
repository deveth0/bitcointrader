package de.dev.eth0.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.BitcoinTraderApplication;
import de.dev.eth0.Constants;
import de.dev.eth0.R;

public class AboutActivity extends SherlockPreferenceActivity {

  private static final String KEY_ABOUT_VERSION = "about_version";
  private static final String KEY_ABOUT_AUTHOR = "about_author";
  private static final String KEY_ABOUT_AUTHOR_TWITTER = "about_author_twitter";
  private static final String KEY_ABOUT_CREDITS_BITCOINWALLET = "about_credits_bitcoinwallet";

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.about);

    final ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    findPreference(KEY_ABOUT_VERSION).setSummary(((BitcoinTraderApplication) getApplication()).applicationVersionName());
    findPreference(KEY_ABOUT_CREDITS_BITCOINWALLET).setSummary(Constants.CREDITS_BITCOINWALLET_URL);

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

  @Override
  public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference) {
    final String key = preference.getKey();
    if (KEY_ABOUT_AUTHOR_TWITTER.equals(key)) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.AUTHOR_TWITTER_URL)));
      finish();
    } else if (KEY_ABOUT_CREDITS_BITCOINWALLET.equals(key)) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.CREDITS_BITCOINWALLET_URL)));
      finish();
    } else if (KEY_ABOUT_AUTHOR.equals(key)) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.AUTHOR_URL)));
      finish();
    }
    return false;
  }
}
