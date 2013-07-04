//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import java.util.ArrayList;
import java.util.List;

public class PriceChartFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = PriceChartFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private LinearLayout graphLayout;
  private GraphView graphView;
  private ProgressDialog mDialog;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.price_chart_fragment, container);
    graphLayout = (LinearLayout)view.findViewById(R.id.price_chart_graph);
    return view;
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity)activity;
    this.application = (BitcoinTraderApplication)activity.getApplication();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        updateView();
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
  public void onPause() {
    super.onPause();
    if (mDialog != null && mDialog.isShowing()) {
      mDialog.dismiss();
    }
    mDialog = null;
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView();
  }

  protected void updateView() {
    GetTradesTask tradesTask = new GetTradesTask();
    tradesTask.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR);
  }

  protected void updateView(List<Trade> tradesList) {
    Log.d(TAG, ".updateView");
    if (graphView == null) {
      graphView = graphView = new LineGraphView(activity, "foo");
      // add data
      graphView.setScrollable(true);
      graphLayout.addView(graphView);
    }


    float[] values = new float[tradesList.size()];
    long[] dates = new long[tradesList.size()];
    final GraphViewData[] data = new GraphViewData[values.length];

    float largest = Integer.MIN_VALUE;
    float smallest = Integer.MAX_VALUE;

    final int tradesListSize = tradesList.size();
    for (int i = 0; i < tradesListSize; i++) {
      final Trade trade = tradesList.get(i);
      values[i] = trade.getPrice().getAmount().floatValue();
      dates[i] = trade.getTimestamp().getTime();
      if (values[i] > largest) {
        largest = values[i];
      }
      if (values[i] < smallest) {
        smallest = values[i];
      }
    }

    for (int i = 0; i < tradesListSize; i++) {
      data[i] = new GraphViewData(dates[i], values[i]);
    }

    double windowSize = (dates[dates.length - 1] - dates[0]) / 2;
    // startValue enables graph window to be aligned with latest
    // trades
    final double startValue = dates[dates.length - 1] - windowSize;
    graphView.addSeries(new GraphViewSeries(data));
    graphView.setViewPort(startValue, windowSize);
    graphView.setScrollable(true);
    graphView.setScalable(true);

  }

  private class GetTradesTask extends ICSAsyncTask<Void, Void, List<Trade>> {

    @Override
    protected void onPreExecute() {
      if (mDialog == null) {
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(getString(R.string.loading_info));
        mDialog.setCancelable(false);
        mDialog.setOwnerActivity(activity);
        mDialog.show();
      }
    }

    @Override
    protected void onPostExecute(List<Trade> trades) {
      if (mDialog != null && mDialog.isShowing()) {
        mDialog.dismiss();
        mDialog = null;
      }
      updateView(trades);
    }

    @Override
    protected List<Trade> doInBackground(Void... params) {
      ExchangeService exchangeService = application.getExchangeService();
      Trades trades = exchangeService.getTrades();
      return trades != null ? trades.getTrades() : new ArrayList<Trade>();
    }
  };
}
