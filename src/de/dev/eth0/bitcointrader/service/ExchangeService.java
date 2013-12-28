//$URL$
//$Id$
package de.dev.eth0.bitcointrader.service;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO;
import de.dev.eth0.bitcointrader.data.ExchangeOrderResult;
import de.dev.eth0.bitcointrader.data.ExchangeWalletHistory;
import de.dev.eth0.bitcointrader.exceptions.NetworkNotAvailableException;
import de.dev.eth0.bitcointrader.exchanges.ExchangeWrapper;
import de.dev.eth0.bitcointrader.exchanges.ExchangeWrapperFactory;
import de.dev.eth0.bitcointrader.ui.BitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.PlaceOrderActivity;
import de.dev.eth0.bitcointrader.ui.fragments.PlaceOrderFragment;
import de.dev.eth0.bitcointrader.ui.widgets.AccountInfoWidgetProvider;
import de.dev.eth0.bitcointrader.ui.widgets.PriceInfoWidgetProvider;
import de.dev.eth0.bitcointrader.util.FormatHelper;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import de.dev.eth0.bitcointrader.util.MiscHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.money.CurrencyUnit;

/**
 * Service to cache all data from exchange to prevent multiple calls
 *
 * @author Alexander Muthmann
 */
public class ExchangeService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
  
  private static final String TAG = ExchangeService.class.getSimpleName();
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, ".onReceive()");
      if (intent.getAction().equals(Constants.EXCHANGE_CHANGED)) {
        if (intent.hasExtra(Constants.EXTRA_EXCHANGE)) {
          setExchange(getExchangeConfigurationDAO().getExchangeConfiguration(intent.getStringExtra(Constants.EXTRA_EXCHANGE)));
        }
      }
      else {
        // only run if currently no running task
        executeTask(new UpdateTask(), (Void)null);
      }
    }
  };
  private LocalBroadcastManager broadcastManager;
  
  public class LocalBinder extends Binder {
    
    public ExchangeService getService() {
      return ExchangeService.this;
    }
  }
  private ExchangeWrapper exchange;
  private final Binder binder = new LocalBinder();
  private AccountInfo accountInfo;
  private List<LimitOrder> openOrders = new ArrayList<LimitOrder>();
  private ExchangeConfiguration.TrailingStopLossConfiguration trailingStopLossConfig;
  private BigDecimal[] trailingStopChecks = new BigDecimal[1];
  private boolean notifyOnUpdate;
  private int updateInterval;
  private Ticker ticker;
  private Date lastUpdate;
  private Date lastUpdateWalletHistory;
  private final Map<String, ExchangeWalletHistory> walletHistoryCache = new HashMap<String, ExchangeWalletHistory>();
  private boolean hasStarted = false;
  private ExchangeConfigurationDAO mExchangeConfigurationDAO;
  
  @Override
  public void onCreate() {
    super.onCreate();
    hasStarted = false;
    broadcastManager = LocalBroadcastManager.getInstance(this);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_SERVICE_ACTION));
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.EXCHANGE_CHANGED));
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (!hasStarted) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      prefs.registerOnSharedPreferenceChangeListener(this);
      notifyOnUpdate = prefs.getBoolean(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE, false);
      updateInterval = Integer.parseInt(prefs.getString(Constants.PREFS_KEY_GENERAL_UPDATE, "0"));
      // set the exchange to the primary one on startup
      setExchange(getExchangeConfigurationDAO().getPrimaryExchangeConfiguration());
      hasStarted = true;
    }
    return Service.START_STICKY;
  }
  
  public void setExchange(ExchangeConfiguration config) {
    Log.d(TAG, "Setting exchange to " + config.getName());
    exchange = ExchangeWrapperFactory.forExchangeConfiguration(config);
    accountInfo = null;
    ticker = null;
    if (config.getTrailingStopLossConfig() != null) {
      trailingStopLossConfig = config.getTrailingStopLossConfig();
      trailingStopChecks = new BigDecimal[trailingStopLossConfig.getNumberUpdates()];
    }
    broadcastExchangeChanged();
    broadcastUpdate();
  }
  
  public ExchangeConfiguration getExchangeConfig() {
    return exchange == null ? null : exchange.getConfig();
  }
  
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE)) {
      notifyOnUpdate = sharedPreferences.getBoolean(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE, false);
    }
    else if (key.equals(Constants.PREFS_KEY_GENERAL_UPDATE)) {
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
    if (!TextUtils.equals(prefs.getString(Constants.PREFS_KEY_CURRENCY, null), currency)) {
      deleteTrailingStopLoss();
      prefs.edit().putString(Constants.PREFS_KEY_CURRENCY, currency).apply();
      sendBroadcast(new Intent(Constants.CURRENCY_CHANGE_EVENT));
    }
  }
  
  public String getCurrency() {
    return PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREFS_KEY_CURRENCY, "USD");
  }
  
  private void deleteTrailingStopLoss() {
    try {
      getExchangeConfigurationDAO().setTrailingStopLossConfiguration(getExchangeConfig(), null);
    }
    catch (ExchangeConfigurationDAO.ExchangeConfigurationException ex) {
      Log.w(TAG, Log.getStackTraceString(ex));
    }
  }
  
  public ExchangeWrapper getExchange() {
    return exchange;
  }
  
  public ExchangeConfigurationDAO getExchangeConfigurationDAO() {
    if (mExchangeConfigurationDAO == null) {
      mExchangeConfigurationDAO = new ExchangeConfigurationDAO(getApplication());
    }
    return mExchangeConfigurationDAO;
  }
  
  public OrderBook getOrderBook() throws IOException {
    return getExchange().getOrderBook(getCurrency());
  }

  /**
   * Returns a the WalletHistory for the given exchange
   *
   * @param currency
   * @param forceUpdate
   * @return
   */
  public ExchangeWalletHistory getWalletHistory(String currency, boolean forceUpdate) {
    boolean update = forceUpdate;
    if (updateInterval > 0 && !forceUpdate) {
      // one minute has 60*1000 miliseconds
      Date now = new Date();
      if (lastUpdateWalletHistory != null && (now.getTime() - lastUpdateWalletHistory.getTime()) >= updateInterval * 60 * 1000) {
        update = true;
      }
    }
    // no update is required and cache contains wallethistory
    if (!update && walletHistoryCache.containsKey(currency)) {
      return walletHistoryCache.get(currency);
    }
    ExchangeWalletHistory ret = null;
    try {
      ret = exchange.getWalletHistory(currency);
      walletHistoryCache.put(currency, ret);
    }
    catch (IOException ex) {
      Log.i(TAG, Log.getStackTraceString(ex), ex);
      broadcastUpdateFailure(ex);
    }
    lastUpdateWalletHistory = new Date();
    return ret;
  }
  
  public AccountInfo getAccountInfo() {
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
  
  public void placeOrder(Order order, FragmentActivity activity) {
    Log.d(TAG, ".placeOrder()");
    executeTask(new PlaceOrderTask(activity), order);
  }
  
  public <S, T, U> void executeTask(ICSAsyncTask<S, T, U> task, S... params) {
    if (MiscHelper.isNetworkAvailable(this)) {
      task.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, params);
    }
    else {
      broadcastUpdateFailure(new NetworkNotAvailableException(getString(R.string.network_not_available)));
    }
  }
  
  private void broadcastExchangeChanged() {
    broadcastManager.sendBroadcast(new Intent(Constants.EXCHANGE_CHANGED));
  }

  private void broadcastUpdate() {
    broadcastManager.sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
  }
  
  private void broadcastUpdateSuccess() {
    sendBroadcast(new Intent(Constants.UPDATE_SUCCEDED));
  }
  
  private void broadcastUpdateFailure(Exception e) {
    Intent intent = new Intent(Constants.UPDATE_FAILED);
    if (e != null) {
      if (PreferenceManager.getDefaultSharedPreferences(ExchangeService.this).getBoolean(Constants.PREFS_KEY_DEBUG, false)
              || e instanceof NetworkNotAvailableException) {
        intent.putExtra(Constants.EXTRA_MESSAGE, e.getLocalizedMessage());
      }
    }
    sendBroadcast(intent);
  }
  
  private void broadcastTrailingStopEvent(BigDecimal trailingStopValue, BigDecimal currentPrice) {
    Intent intent = new Intent(Constants.TRAILING_LOSS_EVENT);
    intent.putExtra(Constants.EXTRA_TRAILING_LOSS_EVENT_VALUE, trailingStopValue.toString());
    intent.putExtra(Constants.EXTRA_TRAILING_LOSS_EVENT_CURRENTPRICE, currentPrice.toString());
    sendBroadcast(intent);
  }
  
  private void broadcastTrailingStopAlignmentEvent(BigDecimal trailingStopValue, BigDecimal currentPrice) {
    Intent intent = new Intent(Constants.TRAILING_LOSS_ALIGNMENT_EVENT);
    intent.putExtra(Constants.EXTRA_TRAILING_LOSS_ALIGNMENT_OLDVALUE, trailingStopValue.toString());
    intent.putExtra(Constants.EXTRA_TRAILING_LOSS_ALIGNMENT_NEWVALUE, currentPrice.toString());
    sendBroadcast(intent);
  }
  
  private <T extends Order> void broadcastOrderExecuted(Collection<T> openOrders) {
    Intent intent = new Intent(Constants.ORDER_EXECUTED);
    List<Parcelable> extras = new ArrayList<Parcelable>();
    for (Order lo : openOrders) {
      try {
        ExchangeOrderResult result = exchange.getOrderResult(lo);
        if (result != null) {
          Bundle bundle = new Bundle();
          bundle.putString(Constants.EXTRA_ORDERRESULT_AVGCOST, FormatHelper.formatBigMoney(
                  FormatHelper.DISPLAY_MODE.CURRENCY_CODE, result.getAvgCost(), Constants.PRECISION_BITCOIN).toString());
          bundle.putString(Constants.EXTRA_ORDERRESULT_TOTALAMOUNT, FormatHelper.formatBigMoney(
                  FormatHelper.DISPLAY_MODE.CURRENCY_CODE, result.getTotalAmount(), Constants.PRECISION_BITCOIN).toString());
          bundle.putString(Constants.EXTRA_ORDERRESULT_TOTALSPENT, FormatHelper.formatBigMoney(
                  FormatHelper.DISPLAY_MODE.CURRENCY_CODE, result.getTotalSpent(), Constants.PRECISION_CURRENCY).toString());
          extras.add(bundle);
        }
      }
      catch (Exception ee) {
        Log.d(TAG, "getting OrderResult failed", ee);
      }
    }
    Log.d(TAG, "Sending out order executed intent");
    if (!extras.isEmpty()) {
      intent.putExtra(Constants.EXTRA_ORDERRESULT, extras.toArray(new Parcelable[extras.size()]));
      sendBroadcast(intent);
    }
  }
  
  private class UpdateTask extends ICSAsyncTask<Void, Void, Boolean> {
    
    @Override
    protected Boolean doInBackground(Void... params) {
      Log.d(TAG, "performing update...");
      try {
        accountInfo = exchange.getAccountInfo();
        ticker = exchange.getTicker(getCurrency());
        checkTrailingStop();
        
        if (TextUtils.isEmpty(getCurrency())) {
          setCurrency(accountInfo.getWallets().get(1).getCurrency());
        }
        List<LimitOrder> orders = exchange.getOpenOrders();
        openOrders.removeAll(orders);
        // Order executed
        if (!openOrders.isEmpty()) {
          broadcastOrderExecuted(openOrders);
        }
        openOrders = orders;
        lastUpdate = new Date();
        broadcastUpdateSuccess();
      }
      catch (IOException ex) {
        Log.i(TAG, Log.getStackTraceString(ex), ex);
        broadcastUpdateFailure(ex);
        return false;
      }
      catch (ExchangeException ee) {
        Log.i(TAG, Log.getStackTraceString(ee), ee);
        broadcastUpdateFailure(ee);
        return false;
      }
      catch (RuntimeException iae) {
        Log.e(TAG, Log.getStackTraceString(iae), iae);
        broadcastUpdateFailure(iae);
        return false;
      }
      return true;
    }
    
    private void checkTrailingStop() {
      // Check trailing stop loss
      if (trailingStopLossConfig != null) {
        Log.d(TAG, "checking trailing stop loss");
        // compare current price from array with last updates
        // first move all items one step to the left
        for (int i = 0; i < trailingStopChecks.length - 1; i++) {
          trailingStopChecks[i] = trailingStopChecks[i + 1];
        }
        trailingStopChecks[trailingStopChecks.length - 1] = ticker.getLast().getAmount();
        // now calculate average
        BigDecimal currentPrice = BigDecimal.ZERO;
        for (int i = trailingStopChecks.length - 1; i >= 0; i--) {
          BigDecimal bd = trailingStopChecks[i];
          if (bd == null) {
            Log.d(TAG, "not enough updates yet...");
            return;  // if a single value is not set yet, we need more updates
          }
          currentPrice = currentPrice.add(bd);
        }
        currentPrice = currentPrice.divide(new BigDecimal(trailingStopChecks.length));
        BigDecimal trailingStopValue = trailingStopLossConfig.getPrice();
        // check if price has fallen below the limit
        if (currentPrice.compareTo(trailingStopValue) < 0) {
          Log.d(TAG, "selling btc as the price has fallen from " + trailingStopValue.toString() + " to " + currentPrice.toString());
          broadcastTrailingStopEvent(trailingStopValue, currentPrice);
          deleteTrailingStopLoss();
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ExchangeService.this);
          if (prefs.getBoolean(Constants.PREFS_KEY_TRAILING_STOP_SELLING_ENABLED, false)) {
            Log.d(TAG, "selling is enabled, selling btc");
            Order marketOrder = new MarketOrder(Order.OrderType.ASK, getAccountInfo().getBalance(CurrencyUnit.of("BTC")).getAmount(), "BTC", getCurrency());
            placeOrder(marketOrder, null);
          }
        }
        if (currentPrice.compareTo(trailingStopValue) > 0) {
          // check if price has risen and a alignment is required
          BigDecimal threshold = new BigDecimal(trailingStopLossConfig.getThreshold()).divide(new BigDecimal(100));
          BigDecimal newTrailingStopValue = currentPrice.subtract(currentPrice.multiply(threshold));
          if (newTrailingStopValue.compareTo(currentPrice) < 0 && newTrailingStopValue.compareTo(trailingStopValue) > 0) {
            Log.d(TAG, "updating trailing stop value from " + trailingStopValue.toString() + " to " + newTrailingStopValue.toString());
            broadcastTrailingStopAlignmentEvent(trailingStopValue, newTrailingStopValue);
            
            trailingStopLossConfig.setPrice(newTrailingStopValue);
            try {
              getExchangeConfigurationDAO().setTrailingStopLossConfiguration(getExchangeConfig(), trailingStopLossConfig);
            }
            catch (ExchangeConfigurationDAO.ExchangeConfigurationException ex) {
              Log.w(TAG, Log.getStackTraceString(ex));
            }
          }
        }
      }
    }
    
    @Override
    protected void onPostExecute(Boolean success) {
      if (success) {
        // update widgets
        AppWidgetManager gm = AppWidgetManager.getInstance(ExchangeService.this);
        int[] ids = gm.getAppWidgetIds(new ComponentName(ExchangeService.this, AccountInfoWidgetProvider.class));
        AccountInfoWidgetProvider.updateWidgets(ExchangeService.this, gm, ids, ExchangeService.this);
        ids = gm.getAppWidgetIds(new ComponentName(ExchangeService.this, PriceInfoWidgetProvider.class));
        PriceInfoWidgetProvider.updateWidgets(ExchangeService.this, gm, ids, ExchangeService.this);
        
        if (notifyOnUpdate) {
          Toast.makeText(ExchangeService.this,
                  R.string.notify_update_success_text, Toast.LENGTH_LONG).show();
          NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                  .setSmallIcon(R.drawable.ic_action_bitcoin)
                  .setContentTitle(getApplicationContext().getString(R.string.notify_update_success_text))
                  .setContentText(getApplicationContext().getString(R.string.notify_update_success_text));
          Intent resultIntent = new Intent(getApplicationContext(), BitcoinTraderActivity.class);
          TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
          stackBuilder.addParentStack(BitcoinTraderActivity.class);
          stackBuilder.addNextIntent(resultIntent);
          PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
          mBuilder.setContentIntent(resultPendingIntent);
          mBuilder.setAutoCancel(true);
          NotificationManager mNotificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
          mNotificationManager.notify(1234, mBuilder.build());
        }
      }
      else if (!success) {
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
          boolean ret = exchange.cancelOrder(params[0].getId());
          lastUpdate = new Date();
          broadcastUpdate();
          return ret;
        }
      }
      catch (IOException ex) {
        Log.i(TAG, Log.getStackTraceString(ex), ex);
        broadcastUpdateFailure(ex);
      }
      catch (ExchangeException ee) {
        Log.i(TAG, Log.getStackTraceString(ee), ee);
        broadcastUpdateFailure(ee);
      }
      return false;
    }
    
    @Override
    protected void onPostExecute(Boolean success) {
      Toast.makeText(ExchangeService.this, (success ? R.string.order_deleted : R.string.order_delete_failed), Toast.LENGTH_LONG).show();
    }
  };
  
  private class PlaceOrderTask extends ICSAsyncTask<Order, Void, Boolean> {
    
    private ProgressDialog mDialog;
    private final FragmentActivity activity;
    
    public PlaceOrderTask(FragmentActivity activity) {
      super();
      this.activity = activity;
    }
    
    @Override
    protected void onPreExecute() {
      if (activity != null) {
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(getString(R.string.place_order_submitting));
        mDialog.setCancelable(false);
        mDialog.setOwnerActivity(activity);
        mDialog.show();
      }
    }
    
    @Override
    protected void onPostExecute(Boolean success) {
      if (success) {
        Toast.makeText(ExchangeService.this,
                ExchangeService.this.getString(R.string.place_order_success), Toast.LENGTH_LONG).show();
        if (activity != null) {
          PlaceOrderFragment placeOrderFragment = (PlaceOrderFragment)activity.getSupportFragmentManager().findFragmentById(R.id.place_order_fragment);
          if (placeOrderFragment != null) {
            placeOrderFragment.resetValues();
          }
        }
      }
      else {
        Toast.makeText(ExchangeService.this, R.string.place_order_failed, Toast.LENGTH_LONG).show();
      }
      if (mDialog != null && mDialog.isShowing()) {
        try {
          mDialog.dismiss();
        }
        catch (IllegalArgumentException iae) {
          // not really nice, but works for this case. (#140)
          Log.w(TAG, iae);
        }
      }
    }
    
    @Override
    protected Boolean doInBackground(Order... params) {
      String orderId = null;
      try {
        if (params.length == 1) {
          Order order = params[0];
          if (order instanceof MarketOrder) {
            MarketOrder mo = (MarketOrder)order;
            orderId = exchange.placeMarketOrder(mo);
            List<MarketOrder> list = new ArrayList<MarketOrder>();
            list.add(mo);
            broadcastOrderExecuted(list);
          }
          else if (order instanceof LimitOrder) {
            LimitOrder lo = (LimitOrder)order;
            orderId = exchange.placeLimitOrder(lo);
          }
          lastUpdate = new Date();
          broadcastUpdateSuccess();
        }
      }
      catch (IOException ex) {
        Log.i(TAG, Log.getStackTraceString(ex), ex);
        broadcastUpdateFailure(ex);
        return false;
      }
      catch (ExchangeException ee) {
        Log.e(TAG, Log.getStackTraceString(ee), ee);
        broadcastUpdateFailure(ee);
        return false;
      }
      // only finish activity if the order has been created in a PlaceOrderActivity
      if (activity != null && activity instanceof PlaceOrderActivity) {
        if (!TextUtils.isEmpty(orderId)) {
          activity.setResult(Activity.RESULT_OK);
        }
        activity.finish();
      }
      return !TextUtils.isEmpty(orderId);
    }
  };
}
