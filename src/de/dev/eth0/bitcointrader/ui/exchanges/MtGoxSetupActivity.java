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
public class MtGoxSetupActivity extends AbstractExchangeConfigurationSetupActivity {

  private EditText manualSetupKey;
  private EditText manualSetupSecretKey;

  protected String getHelpPageName() {
    return "setupMtGox";
  }

  protected ExchangeConfiguration buildExchangeConfiguration() {
    String key = manualSetupKey.getText().toString();
    String secretKey = manualSetupSecretKey.getText().toString();
    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(secretKey)) {
      return new ExchangeConfiguration(key, secretKey, ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.MTGOX);
    }
    return null;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.setup_mtgox_activity);
    manualSetupKey = (EditText) findViewById(R.id.setup_mtgox_activity_manual_key);
    manualSetupSecretKey = (EditText) findViewById(R.id.setup_mtgox_activity_manual_secretkey);
    super.onCreate(savedInstanceState);
  }

}
