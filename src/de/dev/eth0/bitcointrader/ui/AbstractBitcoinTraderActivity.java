//$URL: $
//$Id: $
package de.dev.eth0.bitcointrader.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;

public abstract class AbstractBitcoinTraderActivity extends SherlockFragmentActivity {

  private BitcoinTraderApplication application;
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    application = (BitcoinTraderApplication)getApplication();
    super.onCreate(savedInstanceState);
  }

  public BitcoinTraderApplication getBitcoinTraderApplication() {
    return application;
  }
}
