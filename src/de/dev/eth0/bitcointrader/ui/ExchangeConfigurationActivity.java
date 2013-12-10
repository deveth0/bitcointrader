//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;

import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.R;
/**
 *
 * @author Alexander Muthmann
 */
public final class ExchangeConfigurationActivity extends AbstractBitcoinTraderActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.exchange_configuration_activity);
    final ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}