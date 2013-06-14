package de.dev.eth0;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.util.Log;

public class BitcoinTraderApplication extends Application {

  private static final String TAG = BitcoinTraderApplication.class.getSimpleName();

  @Override
  public void onCreate() {
    final Builder policy = new StrictMode.ThreadPolicy.Builder().detectNetwork();
    policy.penaltyLog();
    StrictMode.setThreadPolicy(policy.build());
    Log.d(TAG, ".onCreate()");

    super.onCreate();

  }

  public final int applicationVersionCode() {
    try {
      return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
    } catch (NameNotFoundException x) {
      return 0;
    }
  }

  public final String applicationVersionName() {
    try {
      return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    } catch (NameNotFoundException x) {
      return "unknown";
    }
  }
}
