package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.ComponentName;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
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
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
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
  private ExchangeService exchangeService;
  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder binder) {
      exchangeService = ((ExchangeService.LocalBinder) binder).getService();
    }

    public void onServiceDisconnected(ComponentName className) {
      exchangeService = null;
    }
  };

  public OrderListAdapter(final Context context) {
    this.context = context;
    inflater = LayoutInflater.from(context);
    final Resources resources = context.getResources();
    colorSignificant = resources.getColor(R.color.fg_significant);
    context.bindService(new Intent(context, ExchangeService.class), mConnection, Context.BIND_AUTO_CREATE);
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

  public void bindView(View row, final Order order) {

    int textColor = colorSignificant;
    // ask or bid
    TextView rowAskBid = (TextView) row.findViewById(R.id.order_row_askbid);
    rowAskBid.setText(order.getType().name());

    // date
    TextView rowDate = (TextView) row.findViewById(R.id.order_row_date);
    rowDate.setText(DateUtils.getRelativeDateTimeString(context, order.getTimestamp().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));
    rowDate.setTextColor(textColor);

    // amount
    AmountTextView rowAmount = (AmountTextView) row.findViewById(R.id.order_row_amount);
    rowAmount.setAmount(order.getTradableAmount());
    rowAmount.setTextColor(textColor);

    // value
    CurrencyTextView rowValue = (CurrencyTextView) row.findViewById(R.id.order_row_value);
    if (order instanceof LimitOrder) {
      LimitOrder lo = (LimitOrder) order;
      rowValue.setTextColor(textColor);
      rowValue.setAmount(lo.getLimitPrice());
      // total
      CurrencyTextView rowTotal = (CurrencyTextView) row.findViewById(R.id.order_row_total);
      rowTotal.setTextColor(textColor);
      //@TODO: multiply limitprice with amount
      rowTotal.setAmount(lo.getLimitPrice().multipliedBy(order.getTradableAmount()));
    }
    ImageButton deleteButton = (ImageButton) row.findViewById(R.id.order_row_delete);
    deleteButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        exchangeService.deleteOrder(order);
      }
    });
  }

  public void clearLabelCache() {
    labelCache.clear();

    notifyDataSetChanged();
  }
}
