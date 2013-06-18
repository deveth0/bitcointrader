/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.actionbarsherlock.app.SherlockFragment;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;

/**
 *
 * @author deveth0
 */
public abstract class AbstractBitcoinTraderFragment extends SherlockFragment {

  private ExchangeService exchangeService;
  private AbstractBitcoinTraderActivity activity;
  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder binder) {
      exchangeService = ((ExchangeService.LocalBinder) binder).getService();
    }

    public void onServiceDisconnected(ComponentName className) {
      exchangeService = null;
    }
  };

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity) activity;
  }

  @Override
  public void onResume() {
    super.onResume();
    activity.bindService(new Intent(activity, ExchangeService.class), mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  public void onPause() {
    super.onPause();
    activity.unbindService(mConnection);
  }

  protected ExchangeService getExchangeService() {
    return exchangeService;
  }
}
