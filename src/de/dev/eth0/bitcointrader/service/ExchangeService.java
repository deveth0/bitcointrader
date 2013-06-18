/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import com.xeiam.xchange.mtgox.v2.dto.account.streaming.MtGoxWalletUpdate;
import com.xeiam.xchange.mtgox.v2.dto.trade.polling.MtGoxOpenOrder;
import com.xeiam.xchange.mtgox.v2.dto.trade.streaming.MtGoxOrderCanceled;
import com.xeiam.xchange.mtgox.v2.dto.trade.streaming.MtGoxTradeLag;
import com.xeiam.xchange.mtgox.v2.service.streaming.SocketMessageFactory;
import com.xeiam.xchange.service.streaming.ExchangeEvent;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.ACCOUNT_INFO;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.CONNECT;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.DEPTH;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.MESSAGE;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.PRIVATE_ID_KEY;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.TICKER;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.TRADE;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.TRADE_LAG;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.USER_ORDER;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.USER_ORDERS_LIST;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.USER_ORDER_ADDED;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.USER_ORDER_CANCELED;
import static com.xeiam.xchange.service.streaming.ExchangeEventType.USER_WALLET_UPDATE;
import com.xeiam.xchange.service.streaming.StreamingExchangeService;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
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
  private MtGoxExchange exchange;
  private final Binder binder = new LocalBinder();
  private MtGoxAccountInfo accountInfo;
  private List<LimitOrder> openOrders;
  private Ticker ticker;
  private Date lastUpdate;

  @Override
  public void onCreate() {
    super.onCreate();
    broadcastManager = LocalBroadcastManager.getInstance(this);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(BitcoinTraderApplication.UPDATE_SERVICE_ACTION));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
    createExchange(prefs);
//      ExchangeStreamingConfiguration exchangeStreamingConfiguration = new MtGoxStreamingConfiguration(10, 10000, 60000, true, null);
//      StreamingExchangeService streamingExchangeService = exchange.getStreamingExchangeService(exchangeStreamingConfiguration);
//
//      // Open the connections to the exchange
//      streamingExchangeService.connect();
//      ExecutorService executorService = Executors.newSingleThreadExecutor();
//      executorService.submit(new TradeDataRunnable(streamingExchangeService, exchange));
    return Service.START_NOT_STICKY;
  }

  private void createExchange(SharedPreferences prefs) {
    //@TODO: remove default strings
    String mtGoxAPIKey = prefs.getString(Constants.PREFS_KEY_MTGOX_APIKEY, "75f65d26-dbfa-4acc-9f00-d9be5d78907c");
    String mtGoxSecretKey = prefs.getString(Constants.PREFS_KEY_MTGOX_SECRETKEY, "wCDgB1vWG9na7SuiqIikCOG3TFb1Q0r66Kt64w0TL7LKCJVJ9klpQZH266hibEDrCPmLzscPJwSqvQuG74/D1A==");
    if (!TextUtils.isEmpty(mtGoxAPIKey) && !TextUtils.isEmpty(mtGoxSecretKey)) {
      ExchangeSpecification exchangeSpec = new ExchangeSpecification(MtGoxExchange.class);
      exchangeSpec.setApiKey(mtGoxAPIKey);
      exchangeSpec.setSecretKey(mtGoxSecretKey);
      exchangeSpec.setSslUri(Constants.MTGOX_SSL_URI);
      exchangeSpec.setPlainTextUriStreaming(Constants.MTGOX_PLAIN_WEBSOCKET_URI);
      exchangeSpec.setSslUriStreaming(Constants.MTGOX_SSL_WEBSOCKET_URI);
      exchange = (MtGoxExchange) ExchangeFactory.INSTANCE.createExchange(exchangeSpec);
      broadcastManager.sendBroadcast(new Intent(BitcoinTraderApplication.UPDATE_SERVICE_ACTION));
    }
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(Constants.PREFS_KEY_MTGOX_APIKEY) || key.equals(Constants.PREFS_KEY_MTGOX_SECRETKEY)) {
      createExchange(sharedPreferences);
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

  class TradeDataRunnable implements Runnable {

    private final StreamingExchangeService streamingExchangeService;
    private final Exchange exchange;

    public TradeDataRunnable(StreamingExchangeService streamingExchangeService, Exchange exchange) {

      this.streamingExchangeService = streamingExchangeService;
      this.exchange = exchange;
    }

    @Override
    public void run() {

      SocketMessageFactory socketMsgFactory = new SocketMessageFactory(exchange.getExchangeSpecification().getApiKey(), exchange.getExchangeSpecification().getSecretKey());

      try {
        while (true) {

          ExchangeEvent exchangeEvent = streamingExchangeService.getNextEvent();
          switch (exchangeEvent.getEventType()) {
            case CONNECT:
              //streamingExchangeService.send(socketMsgFactory.idKey());
              streamingExchangeService.send(socketMsgFactory.privateOrders());
              streamingExchangeService.send(socketMsgFactory.privateInfo());
              break;

            case ACCOUNT_INFO:
              MtGoxAccountInfo mtGoxAccountInfo = (MtGoxAccountInfo) exchangeEvent.getPayload();
              accountInfo = mtGoxAccountInfo;
              lastUpdate = new Date();
              break;
            case USER_ORDERS_LIST:
              MtGoxOpenOrder[] mtGoxOrders = (MtGoxOpenOrder[]) exchangeEvent.getPayload();
              openOrders = MtGoxAdapters.adaptOrders(mtGoxOrders);
              lastUpdate = new Date();
              break;
            case PRIVATE_ID_KEY:
              String keyId = (String) exchangeEvent.getPayload();
              String msgToSend = socketMsgFactory.subscribeWithKey(keyId);
              streamingExchangeService.send(msgToSend);
              Log.d(TAG, "ID KEY: " + keyId);
              break;

            case TRADE_LAG:
              MtGoxTradeLag lag = (MtGoxTradeLag) exchangeEvent.getPayload();
              Log.d(TAG, "TRADE LAG: " + lag.toStringShort());
              break;

            case TRADE:
              Log.d(TAG, "TRADE! " + exchangeEvent.getData().toString());
              break;

            case TICKER:
              Log.d(TAG, "TICKER! " + exchangeEvent.getData().toString());
              break;

            case DEPTH:
              Log.d(TAG, "DEPTH! " + exchangeEvent.getData().toString());
              break;

            // only occurs when order placed via streaming API
            case USER_ORDER_ADDED:
              String orderAdded = (String) exchangeEvent.getPayload();
              Log.d(TAG, "ADDED USER ORDER: " + orderAdded);
              break;

            // only occurs when order placed via streaming API
            case USER_ORDER_CANCELED:
              MtGoxOrderCanceled orderCanceled = (MtGoxOrderCanceled) exchangeEvent.getPayload();
              Log.d(TAG, "CANCELED USER ORDER: " + orderCanceled + "\nfrom: " + exchangeEvent.getData());
              break;

            case USER_WALLET_UPDATE:
              MtGoxWalletUpdate walletUpdate = (MtGoxWalletUpdate) exchangeEvent.getPayload();
              Log.d(TAG, "USER WALLET UPDATE: " + walletUpdate + "\nfrom: " + exchangeEvent.getData());
              break;

            case USER_ORDER:
              MtGoxOpenOrder order = (MtGoxOpenOrder) exchangeEvent.getPayload();
              Log.d(TAG, "USER ORDER: " + order + "\nfrom: " + exchangeEvent.getData());
              break;
            case MESSAGE:
              Log.d(TAG, "MSG not parsed :(");
              break;

            default:
              break;

          }
        }
      } catch (InterruptedException e) {
        Log.d(TAG, "ERROR in Runnable!!!", e);
      } catch (JsonProcessingException e) {
        Log.d(TAG, "Error", e);
      } catch (UnsupportedEncodingException e) {
        Log.d(TAG, "Error", e);
      }
    }
  }

  private class UpdateTask extends ICSAsyncTask<Void, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Void... params) {
      Log.d(TAG, "performing update...");
      try {
        accountInfo = exchange.getPollingAccountService().getMtGoxAccountInfo();
        openOrders = exchange.getPollingTradeService().getOpenOrders().getOpenOrders();
        ticker = exchange.getPollingMarketDataService().getTicker(Currencies.BTC, Currencies.USD);
        lastUpdate = new Date();
        broadcastManager.sendBroadcast(new Intent(BitcoinTraderApplication.UPDATE_ACTION));
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
        return false;
      } catch (HttpException uhe) {
        Log.e(TAG, "HttpException", uhe);
        return false;
      }
      return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
      Toast.makeText(ExchangeService.this, "Update performed " + (success ? "successfully" : "unsuccessfully"), Toast.LENGTH_LONG).show();
    }
  };

  private class DeleteOrderTask extends ICSAsyncTask<Order, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Order... params) {
      Log.d(TAG, "performing update...");
      try {
        if (params.length == 1) {
          boolean ret = exchange.getPollingTradeService().cancelOrder(params[0].getId());
          lastUpdate = new Date();
          broadcastManager.sendBroadcast(new Intent(BitcoinTraderApplication.UPDATE_SERVICE_ACTION));
          return ret;
        }
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
      }
      return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
      Toast.makeText(ExchangeService.this, "Order deleted " + (success ? "successfully" : "unsuccessfully"), Toast.LENGTH_LONG).show();
    }
  };

  private class PlaceOrderTask extends ICSAsyncTask<Order, Void, String> {

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
      mDialog.show();
    }

    @Override
    protected void onPostExecute(String orderId) {
      Toast.makeText(ExchangeService.this, "Order created:" + orderId, Toast.LENGTH_LONG).show();
      mDialog.dismiss();
    }

    @Override
    protected String doInBackground(Order... params) {
      String ret = null;
      try {
        if (params.length == 1) {
          Order order = params[0];
          if (order instanceof MarketOrder) {
            MarketOrder mo = (MarketOrder) order;
            ret = exchange.getPollingTradeService().placeMarketOrder(mo);
          } else if (order instanceof LimitOrder) {
            LimitOrder lo = (LimitOrder) order;
            ret = exchange.getPollingTradeService().placeLimitOrder(lo);
          }
          lastUpdate = new Date();
          broadcastManager.sendBroadcast(new Intent(BitcoinTraderApplication.UPDATE_SERVICE_ACTION));
        }
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
      }
      activity.setResult(TextUtils.isEmpty(ret) ? Activity.RESULT_CANCELED : Activity.RESULT_OK);
      activity.finish();
      return ret;
    }
  };
}
