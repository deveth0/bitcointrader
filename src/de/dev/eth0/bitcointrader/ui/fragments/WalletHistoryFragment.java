//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import de.dev.eth0.bitcointrader.ui.fragments.listAdapter.WalletHistoryListAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.dto.trade.Wallet;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.data.ExchangeWalletHistory;
import de.dev.eth0.bitcointrader.data.ExchangeWalletHistoryEntry;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Muthmann
 */
public class WalletHistoryFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = WalletHistoryFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private WalletHistoryListAdapter adapter;
  private ProgressDialog mDialog;
  private Spinner historyCurrencySpinner;
  private ExpandableListView expandableList;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View view = inflater.inflate(
            R.layout.wallet_history_fragment, container, false);
    expandableList = (ExpandableListView)view.findViewById(R.id.wallet_history_expandable_list);
    expandableList.setAdapter(adapter);
    historyCurrencySpinner = (Spinner) view.findViewById(R.id.wallet_history_currency_spinner);
    ExchangeService exchangeService = application.getExchangeService();
    Set<String> currencies = new LinkedHashSet<String>();
    int idxCurrentCurrency = Integer.MIN_VALUE;
    int counter = 0;
    if (exchangeService != null && exchangeService.getAccountInfo() != null) {
      for (Wallet wallet : exchangeService.getAccountInfo().getWallets()) {
        if (wallet != null && wallet.getBalance() != null && !TextUtils.isEmpty(wallet.getCurrency())) {
          if (exchangeService.getCurrency().equalsIgnoreCase(wallet.getCurrency())) {
            idxCurrentCurrency = counter;
          }
          currencies.add(wallet.getCurrency());
          counter++;
        }
      }
    }
    HistoryCurrencySpinnerAdapter spinneradapter = new HistoryCurrencySpinnerAdapter(activity,
            R.layout.spinner_item, currencies.toArray(new String[currencies.size()]));

    spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    historyCurrencySpinner.setAdapter(spinneradapter);
    historyCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, ".onItemSelected");
        updateView(false);
      }

      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
    if (idxCurrentCurrency != Integer.MIN_VALUE) {
      historyCurrencySpinner.setSelection(idxCurrentCurrency);
    }
    return view;
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.application = (BitcoinTraderApplication) activity.getApplication();
    this.mDialogLoadingString = activity.getString(R.string.loading_info);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mDialog != null && mDialog.isShowing()) {
      mDialog.dismiss();
    }
    mDialog = null;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    adapter = new WalletHistoryListAdapter(activity);
    setHasOptionsMenu(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        updateView(true);
        break;
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_wallet_history");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.wallethistory_options, menu);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView(true);
  }

  protected void updateView(boolean forceUpdate) {
    GetWalletHistoryTask walletTask = new GetWalletHistoryTask();
    walletTask.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, forceUpdate);
  }

  protected void updateView(ExchangeWalletHistory history) {
    Log.d(TAG, ".updateView");
    List<ExchangeWalletHistoryEntry> entries = new ArrayList<ExchangeWalletHistoryEntry>();
    if (history != null) {
      for (ExchangeWalletHistoryEntry entry : history.getWalletHistoryEntries()) {
        entries.addAll(Arrays.asList(entry));
      }
    }
    Collections.sort(entries, new Comparator<ExchangeWalletHistoryEntry>() {
      public int compare(ExchangeWalletHistoryEntry lhs, ExchangeWalletHistoryEntry rhs) {
        return Long.valueOf(rhs.getDate()).compareTo(Long.valueOf(lhs.getDate()));
      }
    });

    Map<ExchangeWalletHistoryEntry, List<ExchangeWalletHistoryEntry>> foo = new LinkedHashMap<ExchangeWalletHistoryEntry, List<ExchangeWalletHistoryEntry>>();
    for (ExchangeWalletHistoryEntry mgwhe : entries) {
      foo.put(mgwhe, Arrays.asList(mgwhe));
    }
    adapter.replace(foo);
  }

  private class GetWalletHistoryTask extends ICSAsyncTask<Boolean, Void, ExchangeWalletHistory> {

    @Override
    protected void onPreExecute() {
      if (mDialog == null) {
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(mDialogLoadingString);
        mDialog.setCancelable(false);
        mDialog.setOwnerActivity(activity);
        mDialog.show();
      }
    }

    @Override
    protected void onPostExecute(ExchangeWalletHistory history) {
      if (mDialog != null && mDialog.isShowing()) {
        mDialog.dismiss();
        mDialog = null;
      }
      updateView(history);
    }

    @Override
    protected ExchangeWalletHistory doInBackground(Boolean... params) {

      ExchangeService exchangeService = application.getExchangeService();
      String currency = (String) historyCurrencySpinner.getSelectedItem();

      if (exchangeService != null) {
        return exchangeService.getWalletHistory(currency, params[0]);
      }
      return null;
    }
  };

  public static class HistoryCurrencySpinnerAdapter extends ArrayAdapter<String> {

    private final String[] entries;

    public HistoryCurrencySpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
      super(context, textViewResourceId, objects);
      entries = objects;
    }

    public String[] getEntries() {
      return entries;
    }
  }
}
