//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;

import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.R;
/**
 * @author Alexander Muthmann
 */
public class PriceChartDetailActivity extends AbstractBitcoinTraderActivity {


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.price_chart_detail_activity);
  }

}
