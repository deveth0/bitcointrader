package de.dev.eth0.bitcointrader;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.xeiam.xchange.dto.account.AccountInfo;
import de.dev.eth0.bitcointrader.service.ExchangeService;

public class BitcoinTraderApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String UPDATE_ACTION = "de.dev.eth0.bitcointrader.UPDATE_ACTION";
  private PendingIntent updateActionIntent;
  private Intent exchangeServiceIntent;
  private AccountInfo accountInfo;
  private static final String TAG = BitcoinTraderApplication.class.getSimpleName();

  @Override
  public void onCreate() {
    Log.d(TAG, ".onCreate()");
    Builder policy = new StrictMode.ThreadPolicy.Builder().detectNetwork();
    policy.penaltyLog();
    StrictMode.setThreadPolicy(policy.build());
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
    updateActionIntent = PendingIntent.getBroadcast(this, 0, new Intent(UPDATE_ACTION), 0);
    exchangeServiceIntent = new Intent(this, ExchangeService.class);
    createDataFromPreferences(prefs);
    super.onCreate();
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Log.d(TAG, ".onSharedPreferenceChanged(" + key + ")");
    if (Constants.PREFS_KEY_GENERAL_UPDATE.equals(key)) {
      AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      alarmManager.cancel(updateActionIntent);
      createAutoUpdater(sharedPreferences);
    } 
  }

  public final String applicationVersionName() {
    try {
      return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    } catch (NameNotFoundException x) {
      return "unknown";
    }
  }

  private void createDataFromPreferences(SharedPreferences prefs) {
    createAutoUpdater(prefs);
  }

  private void createAutoUpdater(SharedPreferences prefs) {
    // set auto update if enabled
    String autoUpdateInt = prefs.getString(Constants.PREFS_KEY_GENERAL_UPDATE, "0");
    int autoUpdateInterval = Integer.parseInt(autoUpdateInt);
    if (autoUpdateInterval > 0) {
      AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, autoUpdateInterval * 60 * 1000, updateActionIntent);
    }
  }

  public AccountInfo getAccountInfo() {
    return accountInfo;
  }
  
  public void startExchangeService() {
    startService(exchangeServiceIntent);
  }

  public void stopExchangeService() {
    stopService(exchangeServiceIntent);
  }
}
