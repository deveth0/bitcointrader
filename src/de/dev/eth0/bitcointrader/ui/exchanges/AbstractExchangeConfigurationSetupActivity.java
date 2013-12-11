//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.exchanges;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.schildbach.wallet.ui.HelpDialogFragment;

/**
 * @author Alexander Muthmann
 */
public abstract class AbstractExchangeConfigurationSetupActivity extends AbstractBitcoinTraderActivity {

  private static final String TAG = AbstractExchangeConfigurationSetupActivity.class.getSimpleName();
  private Button submitButton;

  protected abstract String getHelpPageName();

  protected abstract ExchangeConfiguration buildExchangeConfiguration();


  protected final void saveExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) {
    Intent returnIntent = new Intent();
    try {
      getBitcoinTraderApplication().getExchangeConfigurationDAO().addExchangeConfiguration(exchangeConfiguration);
      setResult(RESULT_OK, returnIntent);
    } catch (ExchangeConfigurationException ex) {
      Log.e(TAG, Log.getStackTraceString(ex), ex);
      setResult(RESULT_CANCELED, returnIntent);
    }
    finish();
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    submitButton = (Button) findViewById(R.id.add_exchange_configuration_submit_button);
    submitButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        ExchangeConfiguration config = buildExchangeConfiguration();
        if (config != null) {
          saveExchangeConfiguration(config);
        }
      }
    });
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    this.finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getSupportMenuInflater().inflate(R.menu.addexchange_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(getSupportFragmentManager(), getHelpPageName());
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
