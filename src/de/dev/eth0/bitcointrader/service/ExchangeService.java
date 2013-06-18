/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dev.eth0.bitcointrader.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import com.xeiam.xchange.mtgox.v2.dto.account.streaming.MtGoxWalletUpdate;
import com.xeiam.xchange.mtgox.v2.dto.trade.streaming.MtGoxOpenOrder;
import com.xeiam.xchange.mtgox.v2.dto.trade.streaming.MtGoxOrderCanceled;
import com.xeiam.xchange.mtgox.v2.dto.trade.streaming.MtGoxTradeLag;
import com.xeiam.xchange.mtgox.v2.service.streaming.MtGoxStreamingConfiguration;
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
import com.xeiam.xchange.service.streaming.ExchangeStreamingConfiguration;
import com.xeiam.xchange.service.streaming.StreamingExchangeService;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to cache all data from exchange to prevent multiple calls
 *
 * @author deveth0
 */
public class ExchangeService extends Service {

  private static final String TAG = ExchangeService.class.getSimpleName();
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, ".onReceive()");
      updateTask task = new updateTask();
      task.execute();
    }
  };
  private LocalBroadcastManager broadcastManager;

  public class LocalBinder extends Binder {

    public ExchangeService getService() {
      return ExchangeService.this;
    }
  }
  private Exchange exchange;
  private final Binder binder = new LocalBinder();
  private AccountInfo accountInfo;
  private List<LimitOrder> openOrders;
  private Date lastUpdate;

  private class updateTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      if (exchange != null) {
        accountInfo = exchange.getPollingAccountService().getAccountInfo();
        openOrders = exchange.getPollingTradeService().getOpenOrders().getOpenOrders();
        lastUpdate = new Date();
      }
      return null;
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    broadcastManager = LocalBroadcastManager.getInstance(this);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(BitcoinTraderApplication.UPDATE_ACTION));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
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
      ExchangeStreamingConfiguration exchangeStreamingConfiguration = new MtGoxStreamingConfiguration(10, 10000, 60000, true, null);
      StreamingExchangeService streamingExchangeService = exchange.getStreamingExchangeService(exchangeStreamingConfiguration);

      // Open the connections to the exchange
      streamingExchangeService.connect();
      ExecutorService executorService = Executors.newSingleThreadExecutor();
      executorService.submit(new TradeDataRunnable(streamingExchangeService, exchange));
    }
    return Service.START_NOT_STICKY;
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

  public AccountInfo getAccountInfo() {
    return accountInfo;
  }

  public List<LimitOrder> getOpenOrders() {
    return openOrders;
  }

  public Date getLastUpdate() {
    return lastUpdate;
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

              // subscribe to "lag" channel "85174711-be64-4de1-b783-0628995d7914"
              // streamingExchangeService.send(socketMsgFactory.subscribeWithType("lag"));

              streamingExchangeService.send(socketMsgFactory.idKey());
              streamingExchangeService.send(socketMsgFactory.privateOrders());
              streamingExchangeService.send(socketMsgFactory.privateInfo());
              break;

            case ACCOUNT_INFO:
              MtGoxAccountInfo mtGoxAccountInfo = (MtGoxAccountInfo) exchangeEvent.getPayload();
              accountInfo=  MtGoxAdapters.adaptAccountInfo(mtGoxAccountInfo);
              lastUpdate = new Date();
              break;
            case USER_ORDERS_LIST:
              MtGoxOpenOrder[] mtGoxOrders = (MtGoxOpenOrder[]) exchangeEvent.getPayload();
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
}
