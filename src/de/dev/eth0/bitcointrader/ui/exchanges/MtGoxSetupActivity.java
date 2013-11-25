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

  private EditText nameEditText;
  private EditText manualSetupKeyEditText;
  private EditText manualSetupSecretKeyEditText;

  protected String getHelpPageName() {
    return "setupMtGox";
  }

  protected ExchangeConfiguration buildExchangeConfiguration() {
    String name = nameEditText.getText().toString();
    String key = manualSetupKeyEditText.getText().toString();
    String secretKey = manualSetupSecretKeyEditText.getText().toString();
    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(secretKey)) {
      return new ExchangeConfiguration(name, key, secretKey, ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.MTGOX);
    }
    return null;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.setup_mtgox_activity);
    nameEditText = (EditText)findViewById(R.id.setup_mtgox_activity_name);
    manualSetupKeyEditText = (EditText)findViewById(R.id.setup_mtgox_activity_manual_key);
    manualSetupSecretKeyEditText = (EditText)findViewById(R.id.setup_mtgox_activity_manual_secretkey);
    super.onCreate(savedInstanceState);
  }

}
