package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setHasOptionsMenu(true);
  }

  @Override
  public void onResume() {
    super.onResume();
    loaderManager.initLoader(0, null, this);
  }

  @Override
  public void onPause() {
    loaderManager.destroyLoader(0);
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

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        loaderManager.restartLoader(0, null, this);
        Toast.makeText(activity, "Refresh on accountInfofragment", Toast.LENGTH_SHORT).show();
        return true;
    }
    return super.onOptionsItemSelected(item);
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
