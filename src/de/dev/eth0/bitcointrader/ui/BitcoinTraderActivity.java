//$URL: $
//$Id: $
package de.dev.eth0.bitcointrader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.schildbach.wallet.integration.android.BitcoinIntegration;

public final class BitcoinTraderActivity extends AbstractBitcoinTraderActivity {

  private LocalBroadcastManager broadcastManager;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.bitcointrader_content);
    broadcastManager = LocalBroadcastManager.getInstance(getBitcoinTraderApplication());
  }

  @Override
  protected void onResume() {
    super.onResume();
    getBitcoinTraderApplication().startExchangeService();
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    super.onCreateOptionsMenu(menu);
    getSupportMenuInflater().inflate(R.menu.bitcointrader_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_wallet_history:
        startActivity(new Intent(this, WalletHistoryActivity.class));
        return true;
      case R.id.bitcointrader_options_refresh:
        broadcastManager.sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
        break;
      case R.id.bitcointrader_options_about:
        startActivity(new Intent(this, AboutActivity.class));
        return true;
      case R.id.bitcointrader_options_preferences:
        startActivity(new Intent(this, PreferencesActivity.class));
        return true;
      case R.id.bitcointrader_options_donate:
        BitcoinIntegration.request(this, Constants.DONATION_ADDRESS);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
