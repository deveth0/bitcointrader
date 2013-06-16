package de.dev.eth0.bitcointrader.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.model.Order;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

public class OrderListFragment extends SherlockListFragment implements LoaderCallbacks<List<Order>> {

  private AbstractBitcoinTraderActivity activity;
  private LoaderManager loaderManager;
  private OrderListAdapter adapter;
  private Order.OrderType orderType;
  private static final String KEY_ORDERTYPE = "ordertype";

  public static OrderListFragment instance(Order.OrderType orderType) {
    final OrderListFragment fragment = new OrderListFragment();

    final Bundle args = new Bundle();
    args.putSerializable(KEY_ORDERTYPE, orderType);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity)activity;
    this.loaderManager = getLoaderManager();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRetainInstance(true);

    this.orderType = (Order.OrderType)getArguments().getSerializable(KEY_ORDERTYPE);

    adapter = new OrderListAdapter(activity);
    setListAdapter(adapter);
  }

  @Override
  public void onResume() {
    super.onResume();
    loaderManager.initLoader(0, null, this);
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    int text = R.string.bitcoin_order_fragment_empty_text_both;
    if (orderType == Order.OrderType.BID) {
      text = R.string.bitcoin_order_fragment_empty_text_bid;
    }
    else if (orderType == Order.OrderType.ASK) {
      text = R.string.bitcoin_order_fragment_empty_text_ask;
    }


    SpannableStringBuilder emptyText = new SpannableStringBuilder(
            getString(text));
    emptyText.setSpan(new StyleSpan(Typeface.BOLD), 0, emptyText.length(), SpannableStringBuilder.SPAN_POINT_MARK);
    setEmptyText(emptyText);
  }

  @Override
  public void onPause() {
    loaderManager.destroyLoader(0);
    super.onPause();
  }

  @Override
  public void onListItemClick(final ListView l, final View v, final int position, final long id) {
    Order order = adapter.getItem(position);

    if (order != null) {
      handleOrderClick(order);
    }
  }

  private void handleOrderClick(final Order order) {
    activity.startActionMode(new ActionMode.Callback() {
      public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        final MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.order_context, menu);
        return true;
      }

      public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        mode.setTitle(order.toString());
        //mode.setSubtitle(label != null ? prefix + label : WalletUtils.formatAddress(prefix, address, Constants.ADDRESS_FORMAT_GROUP_SIZE,                  Constants.ADDRESS_FORMAT_LINE_SIZE));
        return true;
      }

      public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        switch (item.getItemId()) {
          case R.id.order_context_delete:
            handleDeleteOrder(order);
            mode.finish();
            return true;
        }
        return false;
      }

      public void onDestroyActionMode(final ActionMode mode) {
      }

      private void handleDeleteOrder(final Order order) {
        Toast.makeText(activity, "delete: " + order.toString(), Toast.LENGTH_SHORT).show();
      }
    });
  }

  public Loader<List<Order>> onCreateLoader(int id, Bundle args) {
    return new OrdersLoader(activity, orderType);
  }

  public void onLoadFinished(Loader<List<Order>> loader, List<Order> orders) {
    adapter.replace(orders);
  }

  public void onLoaderReset(Loader<List<Order>> loader) {
    // don't clear the adapter, because it will confuse users
  }

  private static class OrdersLoader extends AsyncTaskLoader<List<Order>> {

    private final Order.OrderType orderType;

    private OrdersLoader(final Context context, final Order.OrderType orderType) {
      super(context);
      this.orderType = orderType;
    }

    @Override
    protected void onStartLoading() {
      super.onStartLoading();
      forceLoad();
    }

    @Override
    public List<Order> loadInBackground() {
      List<Order> orders = new ArrayList<Order>();
      Random rand = new Random();
      for (int i = 0; i < rand.nextInt(20); i++) {
        BigInteger price = BigInteger.valueOf(rand.nextInt(20000) * 100000L);
        BigInteger amount = BigInteger.valueOf(rand.nextInt(130000000) * 10L);
        Long time = Math.abs(rand.nextLong());
        Order order = new Order(price, amount, time);
        Log.d(OrderListFragment.class.getSimpleName(), order.toString());
        orders.add(order);
      }
      return orders;
    }
  }
}
