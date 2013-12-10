//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.exchanges;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;

/**
 * @author Alexander Muthmann
 */
public class BitstampSetupActivity extends AbstractExchangeConfigurationSetupActivity {

  private EditText nameEditText;
  private EditText manualSetupKeyEditText;
  private EditText manualSetupSecretKeyEditText;
  private EditText manualSetupUsernameEditText;

  protected String getHelpPageName() {
    return "setupBitstamp";
  }

  protected ExchangeConfiguration buildExchangeConfiguration() {
    String name = nameEditText.getText().toString();
    String key = manualSetupKeyEditText.getText().toString();
    String secretKey = manualSetupSecretKeyEditText.getText().toString();
    String username = manualSetupUsernameEditText.getText().toString();
    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(secretKey)) {
      return new ExchangeConfiguration(name, username, key, secretKey, ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.BITSTAMP);
    }
    return null;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.setup_bitstamp_activity);
    nameEditText = (EditText) findViewById(R.id.setup_bitstamp_activity_name);
    manualSetupKeyEditText = (EditText) findViewById(R.id.setup_bitstamp_activity_manual_key);
    manualSetupSecretKeyEditText = (EditText) findViewById(R.id.setup_bitstamp_activity_manual_secretkey);
    manualSetupUsernameEditText = (EditText) findViewById(R.id.setup_bitstamp_activity_username);
    super.onCreate(savedInstanceState);
  }

}
