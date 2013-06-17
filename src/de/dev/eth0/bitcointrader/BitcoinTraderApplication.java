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
import android.text.TextUtils;
import android.util.Log;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;

public class BitcoinTraderApplication extends Application {

  public static final String UPDATE_ACTION = "de.dev.eth0.bitcointrader.UPDATE_ACTION";
  private static final String TAG = BitcoinTraderApplication.class.getSimpleName();
  private Exchange exchange;

  @Override
  public void onCreate() {
    Builder policy = new StrictMode.ThreadPolicy.Builder().detectNetwork();
    policy.penaltyLog();
    StrictMode.setThreadPolicy(policy.build());
    Log.d(TAG, ".onCreate()");
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String mtGoxAPIKey = prefs.getString(Constants.PREFS_KEY_MTGOX_APIKEY, null);
    String mtGoxSecretKey = prefs.getString(Constants.PREFS_KEY_MTGOX_SECRETKEY, null);
    if (!TextUtils.isEmpty(mtGoxAPIKey) && !TextUtils.isEmpty(mtGoxSecretKey)) {
      ExchangeSpecification exchangeSpec = new ExchangeSpecification(MtGoxExchange.class);
      exchangeSpec.setApiKey(mtGoxAPIKey);
      exchangeSpec.setSecretKey(mtGoxSecretKey);
      exchangeSpec.setSslUri(Constants.MTGOX_SSL_URI);
      exchangeSpec.setPlainTextUriStreaming(Constants.MTGOX_PLAIN_WEBSOCKET_URI);
      exchangeSpec.setSslUriStreaming(Constants.MTGOX_SSL_WEBSOCKET_URI);
      exchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpec);
    }
    // set auto update if enabled
    String autoUpdateInt = prefs.getString(Constants.PREFS_KEY_GENERAL_UPDATE, "0");
    int autoUpdateInterval = Integer.parseInt(autoUpdateInt);
    if (autoUpdateInterval > 0) {
      AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, autoUpdateInterval * 60 * 1000, PendingIntent.getBroadcast(this, 0, new Intent(UPDATE_ACTION), 0));
    }
    super.onCreate();
  }

  
  
  public final String applicationVersionName() {
    try {
      return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    } catch (NameNotFoundException x) {
      return "unknown";
    }
  }

  public Exchange getExchange() {
    return exchange;
  }
}
