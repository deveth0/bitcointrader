package de.dev.eth0.bitcointrader.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;

public abstract class AbstractBitcoinTraderActivity extends SherlockFragmentActivity {

  private BitcoinTraderApplication application;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    application = (BitcoinTraderApplication) getApplication();
    super.onCreate(savedInstanceState);
  }

  protected BitcoinTraderApplication getBitcoinTraderApplication() {
    return application;
  }

}
