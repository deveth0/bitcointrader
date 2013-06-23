package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.AlertDialog;
import java.util.Collection;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.LimitOrder;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.util.ArrayList;
import java.util.List;

public class OrderListAdapter extends BaseAdapter {

  private final AbstractBitcoinTraderActivity activity;
  private final BitcoinTraderApplication application;
  private final LayoutInflater inflater;
  private final List<Order> orders = new ArrayList<Order>();
  private boolean showEmptyText = false;
  private static final int VIEW_TYPE_ORDER = 0;

  public OrderListAdapter(AbstractBitcoinTraderActivity activity) {
    this.activity = activity;
    this.application = activity.getBitcoinTraderApplication();
    inflater = LayoutInflater.from(activity);
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
    return VIEW_TYPE_ORDER;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  public View getView(final int position, View row, final ViewGroup parent) {
    final int type = getItemViewType(position);
    if (type == VIEW_TYPE_ORDER) {
      if (row == null) {
        row = inflater.inflate(R.layout.order_row_extended, null);
      }
      final Order tx = getItem(position);
      bindView(row, tx);
    }
    else {
      throw new IllegalStateException("unknown type: " + type);
    }
    return row;
  }

  public void bindView(View row, final Order order) {
    // ask or bid
    TextView rowAskBid = (TextView)row.findViewById(R.id.order_row_askbid);
    rowAskBid.setText(order.getType().name());

    // date
    TextView rowDate = (TextView)row.findViewById(R.id.order_row_date);
    rowDate.setText(DateUtils.getRelativeDateTimeString(activity, order.getTimestamp().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));

    // amount
    AmountTextView rowAmount = (AmountTextView)row.findViewById(R.id.order_row_amount);
    rowAmount.setAmount(order.getTradableAmount());
    rowAmount.setPrecision(Constants.PRECISION_BITCOIN);
    // value
    CurrencyTextView rowValue = (CurrencyTextView)row.findViewById(R.id.order_row_value);
    CurrencyTextView rowTotal = (CurrencyTextView)row.findViewById(R.id.order_row_total);
    rowValue.setPrecision(Constants.PRECISION_DOLLAR);
    rowTotal.setPrecision(Constants.PRECISION_DOLLAR);
    if (order instanceof LimitOrder) {
      LimitOrder lo = (LimitOrder)order;
      rowValue.setAmount(lo.getLimitPrice());
      rowTotal.setAmount(lo.getLimitPrice().multipliedBy(order.getTradableAmount()));
    }
    ImageButton deleteButton = (ImageButton)row.findViewById(R.id.order_row_delete);
    deleteButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setPositiveButton(R.string.button_delete_order_confirm, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            application.getExchangeService().deleteOrder(order);
          }
        });
        alertDialogBuilder.setNegativeButton(R.string.button_delete_order_cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        });
        alertDialogBuilder.setMessage(activity.getString(R.string.button_delete_order_text, order.getType().toString(), order.getTradableAmount()));
        alertDialogBuilder.create().show();
      }
    });
  }
}
