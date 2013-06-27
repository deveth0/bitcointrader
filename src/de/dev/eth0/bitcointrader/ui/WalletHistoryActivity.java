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

public final class WalletHistoryActivity extends AbstractBitcoinTraderActivity {

  public static final String INTENT_EXTRA_TYPE = "type";

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.wallet_history_activity);
    final ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
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
}
