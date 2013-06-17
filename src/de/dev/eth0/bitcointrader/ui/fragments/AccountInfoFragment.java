package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.dto.account.AccountInfo;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import org.joda.money.CurrencyUnit;

public final class AccountInfoFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<AccountInfo> {

  private static final String TAG = AccountInfoFragment.class.getSimpleName();
  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  private LoaderManager loaderManager;
  private CurrencyTextView viewDollar;
  private CurrencyTextView viewBtc;
  private BroadcastReceiver broadcastReceiver;
private LocalBroadcastManager broadcastManager;
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.application = (BitcoinTraderApplication) activity.getApplication();
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.loaderManager = getLoaderManager();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.account_info_fragment, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();
    loaderManager.initLoader(0, null, this);
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
    loaderManager.destroyLoader(0);
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Resources resources = getResources();
    int textColor = resources.getColor(R.color.fg_significant);
    viewDollar = (CurrencyTextView) view.findViewById(R.id.your_wallet_dollar);
    viewDollar.setTextColor(textColor);
    viewBtc = (CurrencyTextView) view.findViewById(R.id.your_wallet_btc);
    viewBtc.setTextColor(textColor);
  }

  protected void updateView() {
    Log.d(TAG, ".updateView");
    loaderManager.restartLoader(0, null, this);
  }

  public Loader<AccountInfo> onCreateLoader(int i, Bundle bundle) {
    return new AccountInfoLoader(activity, application);
  }

  public void onLoadFinished(Loader<AccountInfo> loader, AccountInfo accountInfo) {
    viewDollar.setAmount(accountInfo.getBalance(CurrencyUnit.USD));
    viewBtc.setAmount(accountInfo.getBalance(CurrencyUnit.of("BTC")));
  }

  public void onLoaderReset(Loader<AccountInfo> loader) {
  }

  private static class AccountInfoLoader extends AsyncTaskLoader<AccountInfo> {

    private BitcoinTraderApplication application;

    private AccountInfoLoader(Context context, BitcoinTraderApplication application) {
      super(context);
      this.application = application;
    }

    @Override
    protected void onStartLoading() {
      super.onStartLoading();
      forceLoad();
    }

    @Override
    public AccountInfo loadInBackground() {
      Log.d(TAG, ".loadInBackground");
      Exchange exchange = application.getExchange();
      return exchange != null ? exchange.getPollingAccountService().getAccountInfo() : null;
    }
  }
}
