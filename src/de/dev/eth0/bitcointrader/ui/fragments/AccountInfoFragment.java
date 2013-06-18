package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.xeiam.xchange.dto.account.AccountInfo;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import org.joda.money.CurrencyUnit;

public final class AccountInfoFragment extends SherlockFragment {

  private static final String TAG = AccountInfoFragment.class.getSimpleName();
  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  private TextView viewName;
  private CurrencyTextView viewDollar;
  private CurrencyTextView viewBtc;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;
  private ExchangeService exchangeService;
  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder binder) {
      exchangeService = ((ExchangeService.LocalBinder) binder).getService();
    }

    public void onServiceDisconnected(ComponentName className) {
      exchangeService = null;
    }
  };

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
    this.activity = (AbstractBitcoinTraderActivity) activity;
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.account_info_fragment, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();
    activity.bindService(new Intent(activity, ExchangeService.class), mConnection, Context.BIND_AUTO_CREATE);
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
    activity.unbindService(mConnection);
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Resources resources = getResources();
    int textColor = resources.getColor(R.color.fg_significant);
    viewName = (TextView) view.findViewById(R.id.your_wallet_name);
    viewName.setTextColor(textColor);
    viewDollar = (CurrencyTextView) view.findViewById(R.id.your_wallet_dollar);
    viewDollar.setTextColor(textColor);
    viewBtc = (CurrencyTextView) view.findViewById(R.id.your_wallet_btc);
    viewBtc.setTextColor(textColor);
  }

  private void updateView() {
    Log.d(TAG, ".updateView");
    if (exchangeService != null) {
      AccountInfo accountInfo = exchangeService.getAccountInfo();
      if (accountInfo != null) {
        viewName.setText(accountInfo.getUsername());
        viewDollar.setAmount(accountInfo.getBalance(CurrencyUnit.USD));
        viewBtc.setAmount(accountInfo.getBalance(CurrencyUnit.of("BTC")));
      }
    }
  }
}
