//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWallet;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistoryEntry;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WalletHistoryFragment extends SherlockListFragment {

  private static final String TAG = WalletHistoryFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private WalletHistoryListAdapter adapter;
  private ProgressDialog mDialog;
  private Spinner historyCurrencySpinner;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View layout = super.onCreateView(inflater, container,
            savedInstanceState);
    ListView lv = (ListView) layout.findViewById(android.R.id.list);
    ViewGroup parent = (ViewGroup) lv.getParent();

    // Remove ListView and add CustomView  in its place
    int lvIndex = parent.indexOfChild(lv);
    parent.removeViewAt(lvIndex);
    View view = inflater.inflate(
            R.layout.wallet_history_fragment, container, false);
    parent.addView(view, lvIndex, lv.getLayoutParams());

    historyCurrencySpinner = (Spinner) view.findViewById(R.id.wallet_history_currency_spinner);
    ExchangeService exchangeService = application.getExchangeService();
    Set<String> currencies = new HashSet<String>();
    if (exchangeService != null && exchangeService.getAccountInfo() != null) {
      for (MtGoxWallet wallet : exchangeService.getAccountInfo().getWallets().getMtGoxWallets()) {
        if (wallet != null && wallet.getBalance() != null && !TextUtils.isEmpty(wallet.getBalance().getCurrency())) {
          currencies.add(wallet.getBalance().getCurrency());
        }
      }
    }
    HistoryCurrencySpinnerAdapter spinneradapter = new HistoryCurrencySpinnerAdapter(activity,
            R.layout.spinner_item, currencies.toArray(new String[0]));
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

    return layout;
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.application = (BitcoinTraderApplication) activity.getApplication();
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
    setListAdapter(adapter);
    setHasOptionsMenu(true);
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    int text = R.string.wallet_history_empty_text;
    SpannableStringBuilder emptyText = new SpannableStringBuilder(
            getString(text));
    emptyText.setSpan(new StyleSpan(Typeface.BOLD), 0, emptyText.length(), SpannableStringBuilder.SPAN_POINT_MARK);
    setEmptyText(emptyText);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        updateView(true);
        break;
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
    GetMtGoxWalletHistoryTask walletTask = new GetMtGoxWalletHistoryTask();
    walletTask.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, forceUpdate);
  }

  protected void updateView(MtGoxWalletHistory history) {
    Log.d(TAG, ".updateView");
    List<MtGoxWalletHistoryEntry> entries = new ArrayList<MtGoxWalletHistoryEntry>();
    if (history != null) {
      entries.addAll(Arrays.asList(history.getMtGoxWalletHistoryEntries()));
      Log.d(TAG, "WalletHistory: " + history.getMtGoxWalletHistoryEntries().length);
    }
    Collections.sort(entries, new Comparator<MtGoxWalletHistoryEntry>() {
      public int compare(MtGoxWalletHistoryEntry lhs, MtGoxWalletHistoryEntry rhs) {
        return Long.valueOf(rhs.getDate()).compareTo(Long.valueOf(lhs.getDate()));
      }
    });

    adapter.replace(entries);
  }

  private class GetMtGoxWalletHistoryTask extends ICSAsyncTask<Boolean, Void, MtGoxWalletHistory> {

    @Override
    protected void onPreExecute() {
      if (mDialog == null) {
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(getString(R.string.loading_info));
        mDialog.setCancelable(false);
        mDialog.show();
      }
    }

    @Override
    protected void onPostExecute(MtGoxWalletHistory history) {
      if (mDialog != null && mDialog.isShowing()) {
        mDialog.dismiss();
        mDialog = null;
      }
      updateView(history);
    }

    @Override
    protected MtGoxWalletHistory doInBackground(Boolean... params) {

      ExchangeService exchangeService = application.getExchangeService();
      String currency = (String) historyCurrencySpinner.getSelectedItem();

      if (exchangeService != null) {
        HistoryCurrencySpinnerAdapter adapter = (HistoryCurrencySpinnerAdapter) historyCurrencySpinner.getAdapter();
        Map<String, MtGoxWalletHistory> histories = exchangeService.getMtGoxWalletHistory(adapter.getEntries(), params[0]);
        return histories.get(currency);
      }
      return null;
    }
  };

  private class HistoryCurrencySpinnerAdapter extends ArrayAdapter<String> {

    private String[] entries;

    public HistoryCurrencySpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
      super(context, textViewResourceId, objects);
      entries = objects;
    }

    public String[] getEntries() {
      return entries;
    }
  }
}
