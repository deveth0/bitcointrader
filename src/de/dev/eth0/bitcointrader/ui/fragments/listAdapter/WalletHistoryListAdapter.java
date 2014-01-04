//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments.listAdapter;

import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeWalletHistoryEntry;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;

/**
 * @author Alexander Muthmann
 */
public class WalletHistoryListAdapter extends AbstractExpandableListAdapter<ExchangeWalletHistoryEntry, ExchangeWalletHistoryEntry> {

  private static final int TYPE_WITH_PRICE = 0;
  private static final int TYPE_WITHOUT_PRICE = 1;

  public WalletHistoryListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public int getChildType(int groupPosition, int childPosition) {
    ExchangeWalletHistoryEntry entry = getChild(groupPosition, childPosition);
    if (entry.getType().equals(ExchangeWalletHistoryEntry.HISTORY_ENTRY_TYPE.OUT)
            || entry.getType().equals(ExchangeWalletHistoryEntry.HISTORY_ENTRY_TYPE.IN)
            || entry.getType().equals(ExchangeWalletHistoryEntry.HISTORY_ENTRY_TYPE.EARNED)
            || entry.getType().equals(ExchangeWalletHistoryEntry.HISTORY_ENTRY_TYPE.SPENT)) {
      return TYPE_WITH_PRICE;
    }
    return TYPE_WITHOUT_PRICE;
  }

  @Override
  public int getChildTypeCount() {
    return 2;
  }

  @Override
  public int getGroupLayout() {
    return R.layout.wallet_history_fragment_list_group;
  }

  @Override
  public int getChildLayout() {
    return R.layout.wallet_history_fragment_list_item;
  }

  public void bindGroupView(View group, ExchangeWalletHistoryEntry entry) {
    TextView headerType = (TextView)group.findViewById(R.id.wallet_history_list_group_type);
    addTypeToView(headerType, entry);

    TextView headerDate = (TextView)group.findViewById(R.id.wallet_history_list_group_date);
    addDateToView(headerDate, entry);

    CurrencyTextView headerAmount = (CurrencyTextView)group.findViewById(R.id.wallet_history_list_group_amount);
    addAmountToView(headerAmount, entry);
  }

  @Override
  public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View childView, ViewGroup parent) {
    int type = getChildType(groupPosition, childPosition);
    ViewHolder viewHolder;
    if (childView == null) {
      switch (type) {
        case TYPE_WITH_PRICE:
          childView = inflater.inflate(R.layout.wallet_history_fragment_list_price_item, null);
          viewHolder = new ViewHolder();
          viewHolder.priceView = (TextView)childView.findViewById(R.id.wallet_history_row_price);
          break;
        case TYPE_WITHOUT_PRICE:
        default:
          childView = inflater.inflate(R.layout.wallet_history_fragment_list_item, null);
          viewHolder = new ViewHolder();
          break;
      }
      viewHolder.balanceView = (CurrencyTextView)childView.findViewById(R.id.wallet_history_row_balance);
      viewHolder.infoView = (TextView)childView.findViewById(R.id.wallet_history_row_info);
      childView.setTag(viewHolder);
    }
    else {
      viewHolder = (ViewHolder)childView.getTag();
    }

    ExchangeWalletHistoryEntry entry = getChild(groupPosition, childPosition);
    if (type == TYPE_WITH_PRICE) {
      // price
      addPriceToView(viewHolder.priceView, entry);
    }
    // balance
    viewHolder.balanceView.setPrecision(8);
    viewHolder.balanceView.setAmount(entry.getBalance());

    
    String[] substrings = entry.getInfoText().split(" ");
    if (substrings.length >= 5) {
      if (entry.getInfoText().contains("bought")) {
        viewHolder.infoView.setText(activity.getString(R.string.wallet_history_info_bought, substrings[3], substrings[5]));
      }
      else if (entry.getInfoText().contains("sold")) {
        viewHolder.infoView.setText(activity.getString(R.string.wallet_history_info_sold, substrings[3], substrings[5]));
      }
      else {
        viewHolder.infoView.setText("");
      }
    }
    else {
      viewHolder.infoView.setText("");
    }
    return childView;
  }

  private void addPriceToView(TextView view, ExchangeWalletHistoryEntry entry) {
    String[] substrings = entry.getInfoText().split(" ");
    view.setText(substrings.length >= 5 ? substrings[5] : "");
  }

  private void addAmountToView(CurrencyTextView view, ExchangeWalletHistoryEntry entry) {
    view.setPrecision(8);
    view.setAmount(entry.getValue());
  }

  private void addDateToView(TextView view, ExchangeWalletHistoryEntry entry) {
    view.setText(DateUtils.getRelativeDateTimeString(activity, Long.parseLong(entry.getDate()) * 1000, DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));
  }

  private void addTypeToView(TextView view, ExchangeWalletHistoryEntry entry) {
    view.setText(entry.getType().getText());
    view.setBackgroundColor(activity.getResources().getColor(entry.getType().getColor()));
  }

  @Override
  public void bindChildView(View child, ExchangeWalletHistoryEntry entry) {
    // not required
  }

  private static class ViewHolder {

    protected CurrencyTextView balanceView;
    protected TextView priceView;
    protected TextView infoView;
  }
}
