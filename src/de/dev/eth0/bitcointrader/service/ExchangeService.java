//$URL$
//$Id$
package de.dev.eth0.bitcointrader.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import com.xeiam.xchange.mtgox.v2.dto.trade.polling.MtGoxOrderResult;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.PlaceOrderActivity;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import si.mazi.rescu.HttpException;

/**
 * Service to cache all data from exchange to prevent multiple calls
 *
 * @author deveth0
 */
public class ExchangeService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String TAG = ExchangeService.class.getSimpleName();
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, ".onReceive()");
      // only run if currently no running task
      if (exchange != null) {
        executeTask(new UpdateTask(), (Void) null);
      }
    }
  };
  private LocalBroadcastManager broadcastManager;


  public class LocalBinder extends Binder {

    public ExchangeService getService() {
      return ExchangeService.this;
    }
  }
  private MtGoxExchangeWrapper exchange;
  private final Binder binder = new LocalBinder();
  private MtGoxAccountInfo accountInfo;
  private List<LimitOrder> openOrders = new ArrayList<LimitOrder>();
  private boolean notifyOnUpdate;
  private int updateInterval;
  private Ticker ticker;
  private Date lastUpdate;
  private Date lastUpdateWalletHistory;
  private Map<String, MtGoxWalletHistory> walletHistoryCache = new HashMap<String, MtGoxWalletHistory>();

  @Override
  public void onCreate() {
    super.onCreate();
    broadcastManager = LocalBroadcastManager.getInstance(this);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_SERVICE_ACTION));

  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
    notifyOnUpdate = prefs.getBoolean(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE, false);
    updateInterval = Integer.parseInt(prefs.getString(Constants.PREFS_KEY_GENERAL_UPDATE, "0"));
    createExchange(prefs);
    return Service.START_NOT_STICKY;
  }

  private void createExchange(SharedPreferences prefs) {
    String mtGoxAPIKey = prefs.getString(Constants.PREFS_KEY_MTGOX_APIKEY, null);
    String mtGoxSecretKey = prefs.getString(Constants.PREFS_KEY_MTGOX_SECRETKEY, null);
    if (!TextUtils.isEmpty(mtGoxAPIKey) && !TextUtils.isEmpty(mtGoxSecretKey)) {
      ExchangeSpecification exchangeSpec = new ExchangeSpecification(MtGoxExchangeWrapper.class);
      exchangeSpec.setApiKey(mtGoxAPIKey);
      exchangeSpec.setSecretKey(mtGoxSecretKey);
      exchangeSpec.setSslUri(Constants.MTGOX_SSL_URI);
      exchangeSpec.setPlainTextUriStreaming(Constants.MTGOX_PLAIN_WEBSOCKET_URI);
      exchangeSpec.setSslUriStreaming(Constants.MTGOX_SSL_WEBSOCKET_URI);
      exchange = (MtGoxExchangeWrapper) ExchangeFactory.INSTANCE.createExchange(exchangeSpec);
      broadcastUpdate();
    }
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(Constants.PREFS_KEY_MTGOX_APIKEY) || key.equals(Constants.PREFS_KEY_MTGOX_SECRETKEY)) {
      createExchange(sharedPreferences);
    }
    if (key.equals(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE)) {
      notifyOnUpdate = sharedPreferences.getBoolean(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE, false);
    }
    if (key.equals(Constants.PREFS_KEY_GENERAL_UPDATE)) {
      updateInterval = Integer.parseInt(sharedPreferences.getString(Constants.PREFS_KEY_GENERAL_UPDATE, "0"));
    }
  }

  @Override
  public void onDestroy() {
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return binder;
  }

  public void setCurrency(String currency) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(Constants.PREFS_KEY_CURRENCY, currency).apply();
  }

  public String getCurrency() {
    return PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREFS_KEY_CURRENCY, null);
  }
  
  
  public MtGoxExchangeWrapper getExchange() {
    return exchange;
  }

  public Map<String, MtGoxWalletHistory> getMtGoxWalletHistory(String[] currencies, boolean forceUpdate) {
    boolean update = forceUpdate;
    if (updateInterval > 0 && !forceUpdate) {
      // one minute has 60*1000 miliseconds
      Date now = new Date();
      if(lastUpdateWalletHistory != null && (now.getTime() - lastUpdateWalletHistory.getTime()) >= updateInterval*60*1000){
        update = true;
      }
    }
    // no update is required and cache contains wallethistory
    if (!update) {
      return Collections.unmodifiableMap(walletHistoryCache);
    }
    walletHistoryCache.clear();
    for (String currency : currencies) {
      MtGoxWalletHistory walletHistory = exchange.getPollingAccountService().getMtGoxWalletHistory(currency);
      if(walletHistory != null){
      walletHistoryCache.put(currency, walletHistory);
      }
    }
    lastUpdateWalletHistory = new Date();
    return Collections.unmodifiableMap(walletHistoryCache);
  }

  public MtGoxAccountInfo getAccountInfo() {
    return accountInfo;
  }

  public List<LimitOrder> getOpenOrders() {
    return openOrders;
  }

  public Ticker getTicker() {
    return ticker;
  }

  public Date getLastUpdate() {
    return lastUpdate;
  }

  public void deleteOrder(Order order) {
    Log.d(TAG, ".deleteOrder()");
    executeTask(new DeleteOrderTask(), order);
  }

  public void placeOrder(Order order, Activity activity) {
    Log.d(TAG, ".placeOrder()");
    executeTask(new PlaceOrderTask(activity), order);
  }

  private <S, T, U> void executeTask(ICSAsyncTask<S, T, U> task, S... params) {
    task.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, params);
  }

  private void broadcastUpdate() {
    broadcastManager.sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
  }

  private void broadcastUpdateSuccess() {
    sendBroadcast(new Intent(Constants.UPDATE_SUCCEDED));
  }

  private void broadcastUpdateFailure() {
    sendBroadcast(new Intent(Constants.UPDATE_FAILED));
  }

  private class UpdateTask extends ICSAsyncTask<Void, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Void... params) {
      Log.d(TAG, "performing update...");
      try {
        accountInfo = exchange.getPollingAccountService().getMtGoxAccountInfo();
        if(TextUtils.isEmpty(getCurrency())){
          setCurrency(accountInfo.getWallets().getMtGoxWallets().get(1).getBalance().getCurrency());
        }
        //@TODO: don't trigger order executed notification on order delete
        List<LimitOrder> orders = exchange.getPollingTradeService().getOpenOrders().getOpenOrders();
        openOrders.removeAll(orders);
        // Order executed
        if (!openOrders.isEmpty()) {
          Intent intent = new Intent(Constants.ORDER_EXECUTED);
          List<Parcelable> extras = new ArrayList<Parcelable>();
          for (LimitOrder lo : orders) {
            try {
              MtGoxOrderResult result = exchange.getPollingTradeService().getOrderResult(lo);
              Bundle bundle = new Bundle();
              bundle.putString(Constants.EXTRA_ORDERRESULT_ID, result.getOrderId());
              bundle.putString(Constants.EXTRA_ORDERRESULT_AVGCOST, result.getAvgCost().getValue().toString());
              bundle.putString(Constants.EXTRA_ORDERRESULT_TOTALAMOUNT, result.getTotalAmount().getValue().toString());
              bundle.putString(Constants.EXTRA_ORDERRESULT_TOTALSPENT, result.getTotalSpent().getValue().toString());
              extras.add(bundle);
            } catch (Exception ee) {
              Log.d(TAG, "getting OrderResult failed", ee);
            }
          }
          if (!extras.isEmpty()) {
            intent.putExtra(Constants.EXTRA_ORDERRESULT, extras.toArray(new Parcelable[0]));
            sendBroadcast(intent);
          }
        }
        openOrders = orders;
        ticker = exchange.getPollingMarketDataService().getTicker(Currencies.BTC, getCurrency());
        lastUpdate = new Date();
        broadcastUpdateSuccess();
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
        broadcastUpdateFailure();
        return false;
      } catch (HttpException uhe) {
        Log.e(TAG, "HttpException", uhe);
        broadcastUpdateFailure();
        return false;
      } catch (RuntimeException iae) {
        Log.e(TAG, "RuntimeException", iae);
        broadcastUpdateFailure();
        return false;
      }
      return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
      if (success && notifyOnUpdate) {
        Toast.makeText(ExchangeService.this,
                R.string.notify_update_success_text, Toast.LENGTH_LONG).show();
      } else if (!success) {
        Toast.makeText(ExchangeService.this,
                R.string.notify_update_failed_title, Toast.LENGTH_LONG).show();
      }
    }
  };

  private class DeleteOrderTask extends ICSAsyncTask<Order, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Order... params) {
      Log.d(TAG, "Deleting order");
      try {
        if (params.length == 1) {
          boolean ret = exchange.getPollingTradeService().cancelOrder(params[0].getId());
          lastUpdate = new Date();
          broadcastUpdate();
          return ret;
        }
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
        broadcastUpdateFailure();
      } catch (HttpException uhe) {
        Log.e(TAG, "HttpException", uhe);
        broadcastUpdateFailure();
        return false;
      }
      return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
      Toast.makeText(ExchangeService.this, "Order deleted " + (success ? "successfully" : "unsuccessfully"), Toast.LENGTH_LONG).show();
    }
  };

  private class PlaceOrderTask extends ICSAsyncTask<Order, Void, Boolean> {

    private ProgressDialog mDialog;
    private Activity activity;

    public PlaceOrderTask(Activity activity) {
      super();
      this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
      mDialog = new ProgressDialog(activity);
      mDialog.setMessage(getString(R.string.place_order_submitting));
      mDialog.setCancelable(false);
      mDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean success) {
      if (success) {
        Toast.makeText(ExchangeService.this,
                ExchangeService.this.getString(R.string.place_order_success), Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(ExchangeService.this,
                ExchangeService.this.getString(R.string.place_order_failed), Toast.LENGTH_LONG).show();
      }
      mDialog.dismiss();

    }

    @Override
    protected Boolean doInBackground(Order... params) {
      String orderId = null;
      try {
        if (params.length == 1) {
          Order order = params[0];
          if (order instanceof MarketOrder) {
            MarketOrder mo = (MarketOrder) order;
            orderId = exchange.getPollingTradeService().placeMarketOrder(mo);
          } else if (order instanceof LimitOrder) {
            LimitOrder lo = (LimitOrder) order;
            orderId = exchange.getPollingTradeService().placeLimitOrder(lo);
          }
          lastUpdate = new Date();
          broadcastUpdateSuccess();
        }
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
        broadcastUpdateFailure();
      } catch (HttpException uhe) {
        Log.e(TAG, "HttpException", uhe);
        broadcastUpdateFailure();
      }
      // only finish activity if the order has been created in a PlaceOrderActivity
      if (activity instanceof PlaceOrderActivity) {
        if (!TextUtils.isEmpty(orderId)) {
          activity.setResult(Activity.RESULT_OK);
        }
        activity.finish();
      }
      return !TextUtils.isEmpty(orderId);
    }
  };
}
