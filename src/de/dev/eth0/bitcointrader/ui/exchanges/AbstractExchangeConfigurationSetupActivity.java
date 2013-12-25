//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.exchanges;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.Constants;
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
  private ExchangeConfiguration currentConfig;
  private Button submitButton;
  protected EditText nameEditText;

  protected abstract String getHelpPageName();

  /**
   * Sets all required parameter of a ExchangeConfig. If currentConfig is not null, this method has to use the same id as the one of currentConfig.
   *
   * @param currentConfig
   * @return
   */
  protected abstract ExchangeConfiguration buildExchangeConfiguration(ExchangeConfiguration currentConfig);

  protected abstract void setExchangeConfiguration(ExchangeConfiguration config);

  protected final void saveExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) {
    Intent returnIntent = new Intent();
    try {
      getExchangeConfigurationDAO().addExchangeConfiguration(exchangeConfiguration);
      setResult(RESULT_OK, returnIntent);
    }
    catch (ExchangeConfigurationException ex) {
      Log.e(TAG, Log.getStackTraceString(ex), ex);
      setResult(RESULT_CANCELED, returnIntent);
    }
    finish();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    submitButton = (Button)findViewById(R.id.add_exchange_configuration_submit_button);
    submitButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        ExchangeConfiguration config = buildExchangeConfiguration(currentConfig);
        if (config != null) {
          saveExchangeConfiguration(config);
        }
      }
    });
    String exchange = getIntent().getStringExtra(Constants.EXTRA_EXCHANGE);
    if (!TextUtils.isEmpty(exchange)) {
      currentConfig = getExchangeConfigurationDAO().getExchangeConfiguration(exchange);
      if (currentConfig != null) {
        setExchangeConfiguration(currentConfig);
      }
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    this.finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getSupportMenuInflater().inflate(R.menu.help_options, menu);
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
