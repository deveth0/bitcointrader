//$URL: $
//$Id: $
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import com.actionbarsherlock.app.SherlockListFragment;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO;
import de.dev.eth0.bitcointrader.service.ExchangeService;

public abstract class AbstractBitcoinTraderListFragment extends SherlockListFragment {

  private BitcoinTraderApplication mApplication;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.mApplication = (BitcoinTraderApplication)activity.getApplication();
  }

  public ExchangeService getExchangeService() {
    return mApplication.getExchangeService();
  }

  public ExchangeConfigurationDAO getExchangeConfigurationDAO() {
    return mApplication.getExchangeService().getExchangeConfigurationDAO();
  }
}
