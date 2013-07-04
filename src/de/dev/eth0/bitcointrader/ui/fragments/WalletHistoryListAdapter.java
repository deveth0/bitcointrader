//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import java.util.Collection;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.xeiam.xchange.currency.MoneyUtils;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistoryEntry;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.util.ArrayList;
import java.util.List;
import org.joda.money.BigMoney;

public class WalletHistoryListAdapter extends BaseAdapter {

  private final AbstractBitcoinTraderActivity activity;
  private final BitcoinTraderApplication application;
  private final LayoutInflater inflater;
  private final List<MtGoxWalletHistoryEntry> entries = new ArrayList<MtGoxWalletHistoryEntry>();
  private boolean showEmptyText = false;
  private static final int VIEW_TYPE_ORDER = 0;

  public WalletHistoryListAdapter(AbstractBitcoinTraderActivity activity) {
    this.activity = activity;
    this.application = activity.getBitcoinTraderApplication();
    inflater = LayoutInflater.from(activity);
  }

  public void clear() {
    entries.clear();
    notifyDataSetChanged();
  }

  public void replace(final Collection<MtGoxWalletHistoryEntry> orders) {
    this.entries.clear();
    this.entries.addAll(orders);
    showEmptyText = true;
    notifyDataSetChanged();
  }

  @Override
  public boolean isEmpty() {
    return showEmptyText && super.isEmpty();
  }

  @Override
  public int getCount() {
    return entries.size();
  }

  @Override
  public MtGoxWalletHistoryEntry getItem(final int position) {
    return entries.get(position);
  }

  @Override
  public long getItemId(final int position) {
    return entries.get(position).hashCode();
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public int getItemViewType(final int position) {
    return VIEW_TYPE_ORDER;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getView(final int position, View row, final ViewGroup parent) {
    final int type = getItemViewType(position);
    if (type == VIEW_TYPE_ORDER) {
      if (row == null) {
        row = inflater.inflate(R.layout.wallet_history_row_extended, null);
      }
      final MtGoxWalletHistoryEntry tx = getItem(position);
      bindView(row, tx);
    }
    else {
      throw new IllegalStateException("unknown type: " + type);
    }
    return row;
  }


  public void bindView(View row, final MtGoxWalletHistoryEntry entry) {
    // type (out, fee, earned)
    TextView rowType = (TextView)row.findViewById(R.id.wallet_history_row_type);
    if (entry.getType().equals("out")) {
      rowType.setText(R.string.wallet_history_out);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.red));
    }
    else if (entry.getType().equals("fee")) {
      rowType.setText(R.string.wallet_history_fee);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.orange));
    }
    else if (entry.getType().equals("in")) {
      rowType.setText(R.string.wallet_history_in);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
    }
    else if (entry.getType().equals("spent")) {
      rowType.setText(R.string.wallet_history_spent);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.red));
    }
    else if (entry.getType().equals("earned")) {
      rowType.setText(R.string.wallet_history_earned);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
    }
    else if (entry.getType().equals("withdraw")) {
      rowType.setText(R.string.wallet_history_withdraw);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
    }
    else if (entry.getType().equals("deposit")) {
      rowType.setText(R.string.wallet_history_deposit);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.green));
    }
    else {
      rowType.setText(entry.getType());
    }

    // date
    TextView rowDate = (TextView)row.findViewById(R.id.wallet_history_row_date);
    rowDate.setText(DateUtils.getRelativeDateTimeString(activity, Long.parseLong(entry.getDate()) * 1000, DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));

    // amount
    CurrencyTextView rowAmount = (CurrencyTextView)row.findViewById(R.id.wallet_history_row_amount);
    BigMoney amount = MoneyUtils.parse(entry.getValue().getCurrency() + " " + entry.getValue().getValue());
    rowAmount.setPrecision(Constants.PRECISION_BITCOIN);
    rowAmount.setAmount(amount);
    // balance

    CurrencyTextView rowBalance = (CurrencyTextView)row.findViewById(R.id.wallet_history_row_balance);
    BigMoney balance = MoneyUtils.parse(entry.getBalance().getCurrency() + " " + entry.getBalance().getValue());
    rowBalance.setPrecision(Constants.PRECISION_BITCOIN);
    rowBalance.setAmount(balance);

  }
}
