package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.xeiam.xchange.dto.marketdata.Ticker;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.text.DateFormat;

public final class PriceInfoFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = PriceInfoFragment.class.getSimpleName();
  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  private CurrencyTextView viewPriceInfoMin;
  private CurrencyTextView viewPriceInfoCurrent;
  private CurrencyTextView viewPriceInfoMax;
  private CurrencyTextView viewPriceInfoAsk;
  private CurrencyTextView viewPriceInfoBid;
  private AmountTextView viewPriceInfoVolume;
  private TextView viewPriceInfoLastUpdate;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;
  private DateFormat dateFormat;
  private DateFormat timeFormat;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.price_info_fragment, container, false);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.application = (BitcoinTraderApplication) activity.getApplication();
    this.activity = (AbstractBitcoinTraderActivity) activity;
  }

  @Override
  public void onResume() {
    super.onResume();
    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, ".onReceive");
        updateView();
      }
    };
    broadcastManager = LocalBroadcastManager.getInstance(application);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(BitcoinTraderApplication.UPDATE_ACTION));
    updateView();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewPriceInfoMin = (CurrencyTextView) view.findViewById(R.id.price_info_min);
    viewPriceInfoCurrent = (CurrencyTextView) view.findViewById(R.id.price_info_current);
    viewPriceInfoMax = (CurrencyTextView) view.findViewById(R.id.price_info_max);
    viewPriceInfoAsk = (CurrencyTextView) view.findViewById(R.id.price_info_ask);
    viewPriceInfoBid = (CurrencyTextView) view.findViewById(R.id.price_info_bid);
    viewPriceInfoVolume = (AmountTextView) view.findViewById(R.id.price_info_volume);
    viewPriceInfoLastUpdate = (TextView) view.findViewById(R.id.price_info_lastupdate);
    dateFormat = android.text.format.DateFormat.getDateFormat(activity);
    timeFormat = android.text.format.DateFormat.getTimeFormat(activity);
  }

  private void updateView() {
    Log.d(TAG, ".updateView");
    if (getExchangeService() != null) {
      Ticker ticker = getExchangeService().getTicker();
      if (ticker != null) {
        viewPriceInfoMin.setAmount(ticker.getLow());
        viewPriceInfoMin.setShowCurrencyCode(false);
        viewPriceInfoMin.setPrefix(activity.getString(R.string.price_info_min_label));
        viewPriceInfoCurrent.setAmount(ticker.getLast());
        viewPriceInfoCurrent.setShowCurrencyCode(false);
        viewPriceInfoMax.setAmount(ticker.getHigh());
        viewPriceInfoMax.setShowCurrencyCode(false);
        viewPriceInfoMax.setPrefix(activity.getString(R.string.price_info_max_label));
        viewPriceInfoAsk.setAmount(ticker.getAsk());
        viewPriceInfoAsk.setShowCurrencyCode(false);
        viewPriceInfoAsk.setPrefix(activity.getString(R.string.price_info_ask_label));
        viewPriceInfoBid.setAmount(ticker.getBid());
        viewPriceInfoBid.setShowCurrencyCode(false);
        viewPriceInfoBid.setPrefix(activity.getString(R.string.price_info_bid_label));
        viewPriceInfoVolume.setAmount(ticker.getVolume());
        viewPriceInfoLastUpdate.setText(dateFormat.format(ticker.getTimestamp()) + ", " + timeFormat.format(ticker.getTimestamp()));
      }
    }
  }
}
