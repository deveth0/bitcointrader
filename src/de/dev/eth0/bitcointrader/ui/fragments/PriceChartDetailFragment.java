//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.PriceChartActivity;
import de.schildbach.wallet.ui.HelpDialogFragment;

/**
 * @author Alexander Muthmann
 */
public class PriceChartDetailFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = PriceChartDetailFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity)activity;
    this.application = (BitcoinTraderApplication)activity.getApplication();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        updateView();
        break;
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_price_chart_detail");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.price_chart_detail_fragment, container);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // draw sin curve
    int num = 150;
    GraphView.GraphViewData[] data = new GraphView.GraphViewData[num];
    double v = 0;
    for (int i = 0; i < num; i++) {
      v += 0.2;
      data[i] = new GraphView.GraphViewData(i, Math.sin(v));
    }
    // graph with dynamically genereated horizontal and vertical labels
    GraphView graphView;
    graphView = new LineGraphView(activity, "GraphViewDemo");

    // add data
    graphView.addSeries(new GraphViewSeries(data));
    // set view port, start=2, size=40
    graphView.setViewPort(2, 40);
    graphView.setScrollable(true);
    LinearLayout layout = (LinearLayout)view.findViewById(R.id.price_chart_detail_graph);
    layout.addView(graphView);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    // only add menu if included in pricechartactivity
    if (activity instanceof PriceChartActivity) {
      inflater.inflate(R.menu.pricechart_options, menu);
    }
  }

  protected void updateView() {
    Log.d(TAG, ".updateView()");

  }
}
