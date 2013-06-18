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
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import org.joda.money.CurrencyUnit;

public final class AccountInfoFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = AccountInfoFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private TextView viewName;
  private CurrencyTextView viewDollar;
  private CurrencyTextView viewBtc;
  private AmountTextView viewTradeFee;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.application = (BitcoinTraderApplication) activity.getApplication();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.account_info_fragment, container, false);
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
    viewName = (TextView) view.findViewById(R.id.your_wallet_name);
    viewDollar = (CurrencyTextView) view.findViewById(R.id.your_wallet_dollar);
    viewBtc = (CurrencyTextView) view.findViewById(R.id.your_wallet_btc);
    viewTradeFee = (AmountTextView) view.findViewById(R.id.your_wallet_tradefee);
  }

  private void updateView() {
    Log.d(TAG, ".updateView");
    if (getExchangeService() != null) {
      MtGoxAccountInfo mtgoxaccountInfo = getExchangeService().getAccountInfo();
      if (mtgoxaccountInfo != null) {
        AccountInfo accountInfo = MtGoxAdapters.adaptAccountInfo(mtgoxaccountInfo);
        viewName.setText(accountInfo.getUsername());
        viewDollar.setAmount(accountInfo.getBalance(CurrencyUnit.USD));
        viewBtc.setAmount(accountInfo.getBalance(CurrencyUnit.of("BTC")));
        viewTradeFee.setAmount(mtgoxaccountInfo.getTradeFee());
      }
    }
  }
}
