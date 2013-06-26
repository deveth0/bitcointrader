//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;

import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.dto.Order;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.fragments.PlaceOrderFragment;

public final class PlaceOrderActivity extends AbstractBitcoinTraderActivity {

  public static final String INTENT_EXTRA_TYPE = "type";

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.place_order_content);
    final ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(final Intent intent) {
    handleIntent(intent);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;

    }
    return super.onOptionsItemSelected(item);
  }

  private void handleIntent(final Intent intent) {
    Order.OrderType ordertype = null;
    if (intent.hasExtra(INTENT_EXTRA_TYPE)) {
      ordertype = Order.OrderType.valueOf(intent.getStringExtra(INTENT_EXTRA_TYPE));
      updatePlaceOrderFragment(ordertype);
    }
  }

  private void updatePlaceOrderFragment(Order.OrderType ordertype) {
    final PlaceOrderFragment placeOrderFragment = (PlaceOrderFragment)getSupportFragmentManager().findFragmentById(R.id.place_order_fragment);
    placeOrderFragment.update(ordertype);
  }
}
