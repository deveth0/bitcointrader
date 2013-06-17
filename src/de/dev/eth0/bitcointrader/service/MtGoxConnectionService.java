package de.dev.eth0.bitcointrader.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;
import de.dev.eth0.bitcointrader.Constants;

public class MtGoxConnectionService extends Service implements ExchangeConnectionService {

  private static final String TAG = MtGoxConnectionService.class.getSimpleName();
  private Exchange exchange;

  public class LocalBinder extends Binder {

    public ExchangeConnectionService getService() {
      return MtGoxConnectionService.this;
    }
  }
  private final IBinder mBinder = new LocalBinder();

  @Override
  public IBinder onBind(final Intent intent) {
    Log.d(TAG, ".onBind()");
    return mBinder;
  }

  @Override
  public boolean onUnbind(final Intent intent) {
    Log.d(TAG, ".onUnbind()");
    return super.onUnbind(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "Received start id " + startId + ": " + intent);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String mtGoxAPIKey = prefs.getString(Constants.PREFS_KEY_MTGOX_APIKEY, null);
    String mtGoxSecretKey = prefs.getString(Constants.PREFS_KEY_MTGOX_SECRETKEY, null);
    if (!TextUtils.isEmpty(mtGoxAPIKey) && !TextUtils.isEmpty(mtGoxSecretKey)) {
      ExchangeSpecification exSpec = new ExchangeSpecification(MtGoxExchange.class);
      exSpec.setApiKey(mtGoxAPIKey);
      exSpec.setSecretKey(mtGoxSecretKey);
      exSpec.setSslUri(Constants.MTGOX_SSL_URI);
      exSpec.setPlainTextUriStreaming(Constants.MTGOX_PLAIN_WEBSOCKET_URI);
      exSpec.setSslUriStreaming(Constants.MTGOX_SSL_WEBSOCKET_URI);
      exchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
    }
    return START_NOT_STICKY;
  }

  @Override
  public Exchange getExchange() {
    return exchange;
  }
}
