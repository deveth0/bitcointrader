//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import com.xeiam.xchange.currency.MoneyUtils;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeWalletHistoryEntry;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import org.joda.money.BigMoney;

/**
 * @author Alexander Muthmann
 */
public class WalletHistoryListAdapter extends AbstractListAdapter<ExchangeWalletHistoryEntry> {

  public WalletHistoryListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public int getRowLayout() {
    return R.layout.wallet_history_row_extended;
  }
  
  public void bindView(View row, ExchangeWalletHistoryEntry entry) {
    // type (out, fee, earned)
    TextView rowType = (TextView) row.findViewById(R.id.wallet_history_row_type);
    rowType.setText(entry.getType().getText());
    rowType.setBackgroundColor(activity.getResources().getColor(entry.getType().getColor()));

    // date
    TextView rowDate = (TextView) row.findViewById(R.id.wallet_history_row_date);
    rowDate.setText(DateUtils.getRelativeDateTimeString(activity, Long.parseLong(entry.getDate()) * 1000, DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));

    // amount
    CurrencyTextView rowAmount = (CurrencyTextView) row.findViewById(R.id.wallet_history_row_amount);
    rowAmount.setPrecision(8);
    rowAmount.setAmount(entry.getValue());
    // balance
    CurrencyTextView rowBalance = (CurrencyTextView) row.findViewById(R.id.wallet_history_row_balance);
    rowBalance.setPrecision(8);
    rowBalance.setAmount(entry.getBalance());

  }

}
