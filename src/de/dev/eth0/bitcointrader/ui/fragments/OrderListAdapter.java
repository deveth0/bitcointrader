package de.dev.eth0.bitcointrader.ui.fragments;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.LimitOrder;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.util.ArrayList;
import java.util.List;

public class OrderListAdapter extends BaseAdapter {

  private final Context context;
  private final LayoutInflater inflater;
  private final List<Order> orders = new ArrayList<Order>();
  private boolean showEmptyText = false;
  private final int colorSignificant;
  private final Map<String, String> labelCache = new HashMap<String, String>();
  private static final int VIEW_TYPE_TRANSACTION = 0;

  public OrderListAdapter(final Context context) {
    this.context = context;
    inflater = LayoutInflater.from(context);
    final Resources resources = context.getResources();
    colorSignificant = resources.getColor(R.color.fg_significant);
  }

  public void clear() {
    orders.clear();

    notifyDataSetChanged();
  }

  public void replace(final Order tx) {
    orders.clear();
    orders.add(tx);

    notifyDataSetChanged();
  }

  public void replace(final Collection<Order> orders) {
    this.orders.clear();
    this.orders.addAll(orders);

    showEmptyText = true;

    notifyDataSetChanged();
  }

  @Override
  public boolean isEmpty() {
    return showEmptyText && super.isEmpty();
  }

  public int getCount() {
    return orders.size();
  }

  public Order getItem(final int position) {
    return orders.get(position);
  }

  public long getItemId(final int position) {
    return orders.get(position).hashCode();
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public int getItemViewType(final int position) {
    return VIEW_TYPE_TRANSACTION;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  public View getView(final int position, View row, final ViewGroup parent) {
    final int type = getItemViewType(position);

    if (type == VIEW_TYPE_TRANSACTION) {
      if (row == null) {
        row = inflater.inflate(R.layout.order_row_extended, null);
      }

      final Order tx = getItem(position);
      bindView(row, tx);
    } else {
      throw new IllegalStateException("unknown type: " + type);
    }

    return row;
  }

  public void bindView(final View row, final Order tx) {

    int textColor = colorSignificant;
    // ask or bid
    TextView rowFromTo = (TextView) row.findViewById(R.id.order_row_fromto);
    rowFromTo.setText(tx.getType().equals(Order.OrderType.BID) ? R.string.symbol_bid : R.string.symbol_ask);
    rowFromTo.setTextColor(textColor);

    // date
    TextView rowDate = (TextView) row.findViewById(R.id.order_row_date);
    rowDate.setText(DateUtils.getRelativeDateTimeString(context, tx.getTimestamp().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));
    rowDate.setTextColor(textColor);

    // amount
    AmountTextView rowAmount = (AmountTextView) row.findViewById(R.id.order_row_amount);
    rowAmount.setAmount(tx.getTradableAmount());
    rowAmount.setTextColor(textColor);

    // value
    CurrencyTextView rowValue = (CurrencyTextView) row.findViewById(R.id.order_row_value);
    if (tx instanceof LimitOrder) {
      LimitOrder order = (LimitOrder) tx;
      rowValue.setTextColor(textColor);
      rowValue.setAmount(order.getLimitPrice());
      // total
      CurrencyTextView rowTotal = (CurrencyTextView) row.findViewById(R.id.order_row_total);
      rowTotal.setTextColor(textColor);
      //@TODO: multiply limitprice with amount
      rowTotal.setAmount(order.getLimitPrice().multipliedBy(tx.getTradableAmount()));
    }
  }

  public void clearLabelCache() {
    labelCache.clear();

    notifyDataSetChanged();
  }
}
