//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;

public class PriceChartFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = PriceChartFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private LinearLayout graphLayout;

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
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // draw sin curve
    int num = 150;
    GraphViewData[] data = new GraphViewData[num];
    double v = 0;
    for (int i = 0; i < num; i++) {
      v += 0.2;
      data[i] = new GraphViewData(i, Math.sin(v));
    }
    // graph with dynamically genereated horizontal and vertical labels
    GraphView graphView = new BarGraphView(activity, "GraphViewDemo");
    graphView.setBackgroundColor(R.color.green);
    // add data
    graphView.addSeries(new GraphViewSeries(data));
    graphView.setScrollable(true);
    
    graphLayout.addView(graphView);
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
  public void onResume() {
    super.onResume();
    updateView();
  }

  protected void updateView() {
  }
}
