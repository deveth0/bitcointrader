package de.dev.eth0.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import de.dev.eth0.BitcoinTraderApplication;

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

  protected final void toast(final String text, final Object... formatArgs) {
    toast(text, 0, Toast.LENGTH_SHORT, formatArgs);
  }

  protected final void longToast(final String text, final Object... formatArgs) {
    toast(text, 0, Toast.LENGTH_LONG, formatArgs);
  }
}
