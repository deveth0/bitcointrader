//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.content.Intent;
import android.os.Bundle;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.fragments.PriceChartDetailFragment;
/**
 * @author Alexander Muthmann
 */
public class PriceChartDetailActivity extends AbstractBitcoinTraderActivity {

  public final static String INTENT_EXTRA_EXCHANGE = "exchange";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.price_chart_detail_activity);
    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    if (intent.hasExtra(INTENT_EXTRA_EXCHANGE)) {
      String exchange = intent.getStringExtra(INTENT_EXTRA_EXCHANGE);
      updatePriceChartDetailFragment(exchange);
    }
  }

  private void updatePriceChartDetailFragment(String exchange) {
    PriceChartDetailFragment priceChartDetailFragment = (PriceChartDetailFragment)getSupportFragmentManager().findFragmentById(R.id.price_chart_detail_fragment);
    priceChartDetailFragment.update(exchange);
  }
}
