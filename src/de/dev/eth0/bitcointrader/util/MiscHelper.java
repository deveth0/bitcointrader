//$URL$
//$Id$
package de.dev.eth0.bitcointrader.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Helper with static methods
 */
public class MiscHelper {

  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  private MiscHelper() {
  }
}
