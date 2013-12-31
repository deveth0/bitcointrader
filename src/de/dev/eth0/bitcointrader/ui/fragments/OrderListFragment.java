//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.xeiam.xchange.dto.trade.LimitOrder;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.fragments.listAdapter.OrderListAdapter;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Muthmann
 */
public class OrderListFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = OrderListFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private ExpandableListView expandableList;
  private OrderListAdapter adapter;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View view = inflater.inflate(
            R.layout.order_list_fragment, container, false);
    expandableList = (ExpandableListView)view.findViewById(R.id.order_list_expandable_list);
    expandableList.setAdapter(adapter);
    return view;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.application = (BitcoinTraderApplication) activity.getApplication();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    adapter = new OrderListAdapter(activity);
  }

  @Override
  public void onResume() {
    super.onResume();
    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, ".onReceive");
        updateView();
      }
    };
    broadcastManager = LocalBroadcastManager.getInstance(application);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_SUCCEDED));
    updateView();
  }

  @Override
  public void onPause() {
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
    super.onPause();
  }

  protected void updateView() {
    Log.d(TAG, ".updateView");
    Map<ExchangeConfiguration, List<LimitOrder>> map = new ArrayMap<ExchangeConfiguration, List<LimitOrder>>();
    if (application.getExchangeService() != null && application.getExchangeService().getOpenOrders() != null
            && !application.getExchangeService().getOpenOrders().isEmpty()) {
      map.put(application.getExchangeService().getExchangeConfig(), application.getExchangeService().getOpenOrders());
    }
    adapter.replace(map);
    // expand all groups by default
    for (int i = 0; i < adapter.getGroupCount(); i++) {
      expandableList.expandGroup(i);
    }
  }
}
